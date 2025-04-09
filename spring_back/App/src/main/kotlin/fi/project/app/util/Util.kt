package fi.project.app.util

import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.time.Instant
import kotlin.random.Random
import java.io.IOException
import java.nio.file.Paths

fun convertPDFToImage(file: File) {
    println("Converting PDF at ${file.absolutePath} to image...")
    var pythonCommand = "python3"
    if (System.getProperty("os.name").lowercase().contains("windows")) {
        pythonCommand = "python"
    }
    var pathToPythonScript = ""
    if (System.getProperty("os.name").lowercase().contains("windows")) {
        pathToPythonScript = "./src/main/kotlin/fi/project/app/util/convertpdf.py"
    } else {
        pathToPythonScript = System.getProperty("user.dir") + "/App/src/main/kotlin/fi/project/app/util/convertpdf.py"
    }

    val processBuilder = ProcessBuilder(
        pythonCommand,
        pathToPythonScript,
//        System.getProperty("user.dir") + "/App/src/main/kotlin/fi/project/app/util/convertpdf.py",
        file.absolutePath,
    )
    processBuilder.redirectErrorStream(true)
    val process = processBuilder.start()
    val output = process.inputStream.bufferedReader().readText()
    process.waitFor()
    println("Python script output: $output")
    if (process.exitValue() != 0) {
        println("Error converting PDF to image: $output")
        throw RuntimeException("Error converting PDF to image: $output")
    }
}

//TODO: make sure this works on linux also
/**
 * Saves a file to the specified path.
 * Takes a MultipartFile and a path suffix,
 * and appends it to the path to the project root directory to form the full path.
 * @param file the file to save, as MultipartFile
 * @param path the path to save the file to, relative to the project root directory
 */
fun saveFile(file: MultipartFile, path: String): File {
    // Get the current working directory and append the given path
    var newPath = System.getProperty("user.dir") + path
    newPath = newPath.replace("\\", "/")
    println("New path: $newPath")
    // Create the directory if it doesn't exist
    val directory = File(newPath)
    if (!directory.exists()) {
        directory.mkdir()
    }

    /* The file is saved in the project root directory. */
//    val savedFile = File(newPath, file.originalFilename ?: "tempfile")
    val fileExtension = file.originalFilename?.substringAfterLast(".")
    val savedFile = File(newPath, "temp.$fileExtension")
    file.transferTo(savedFile)
    println("File saved to: ${savedFile.absolutePath}")

    return savedFile
}

/**
 * Creates a storage directory for the given file and saves the file within it.
 * The directory is uniquely named using the current timestamp and a random letter.
 * The file is saved with its original extension, if available, or a default name otherwise.
 * Returns a `StorageInfo` object containing details about the stored file and directory.
 *
 * @param file the file to be stored, as a MultipartFile
 * @param baseDir the base directory where the storage directory will be created (default is "temp")
 * @return a `StorageInfo` object containing details about the stored file and directory
 * @throws IOException if the directory cannot be created or the file cannot be saved
 */
fun createStorage(file: MultipartFile, baseDir: String = "temp"): StorageInfo {
    val timeStamp = Instant.now().toEpochMilli()
    val randomLetter = ('a'..'z').random()
    val dirName = "$timeStamp-$randomLetter"

    //fixes baseDir to be relative to the project root
    val projectRoot = Paths.get("").toAbsolutePath().toFile()
    val directory = File(projectRoot, "$baseDir/$dirName")

    if (!directory.exists()) {
        val created = directory.mkdirs()
        if (created) {
            println("Created storage directory: ${directory.absolutePath}")
        } else {
            throw IOException("Failed to create directory: ${directory.absolutePath}")
        }
    }

    val originalExt = file.originalFilename?.substringAfterLast('.', "")
    val filename = if (originalExt.isNullOrBlank()) dirName else "$dirName.$originalExt"

    val savedFile = File(directory, filename)

    try {
        file.transferTo(savedFile)
        println("Primary file saved as: ${savedFile.absolutePath}")
    } catch (e: Exception) {
        throw IOException("Failed to save file: ${savedFile.absolutePath}", e)
    }

    return StorageInfo(
        directoryPath = directory.absolutePath,
        filePath = savedFile.absolutePath,
        originalFilename = file.originalFilename,
        originalExtension = originalExt
    )
}

/**
 * Data class representing storage information for a file.
 * This class provides functionality to manage files in a specific directory,
 * including creating new files and maintaining a log of actions performed in the directory.
 *
 * Upon initialization, a log file (`log.txt`) is created in the directory,
 * and the creation timestamp is recorded. Subsequent actions, such as creating new files,
 * are appended to the log file.
 *
 * @property directoryPath the path to the directory where the file is stored
 * @property filePath the path to the file itself
 * @property originalFilename the original filename of the uploaded file
 * @property originalExtension the original file extension, derived from the filename
 *
 * @constructor Initializes the `StorageInfo` object and ensures a log file is created in the directory.
 */
data class StorageInfo(
    val directoryPath: String,
    val filePath: String,
    val originalFilename: String?,
    val originalExtension: String? = originalFilename?.substringAfterLast('.', "")
) {
    init {
        createLogFile()
    }

    val directory: File get() = File(directoryPath)
    val file: File get() = File(filePath)

    /**
     * Creates a new file in the same directory as the stored file and logs the action.
     *
     * @param filename the name of the new file to create
     * @return the newly created File object
     * @throws IOException if the file cannot be created
     */
    fun createNewFile(filename: String): File {
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                throw IOException("Failed to create directory: ${directory.absolutePath}")
            }
        }

        val newFile = File(directory, filename)
        if (!newFile.exists()) {
            newFile.createNewFile()
            println("New file created: ${newFile.absolutePath}")
            appendToLogFile("New file created: $filename at ${Instant.now()}")
        } else {
            println("File already exists: ${newFile.absolutePath}")
            appendToLogFile("File already exists: $filename at ${Instant.now()}")
        }

        return newFile
    }

    /**
     * Creates a log file in the directory with the timestamp and "created" message.
     */
    private fun createLogFile() {
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                throw IOException("Failed to create directory for log: ${directory.absolutePath}")
            }
        }

        val logFile = File(directory, "log.txt")
        if (!logFile.exists()) {
            logFile.writeText("${Instant.now()} created\n")
        }
    }

    /**
     * Appends a message to the log file in the directory.
     *
     * @param message the message to append to the log file
     */
    @Synchronized
    fun appendToLogFile(message: String) {
        val logFile = File(directory, "log.txt")
        if (!logFile.exists()) {
            createLogFile()
        }
        logFile.appendText("$message\n")
    }
}
