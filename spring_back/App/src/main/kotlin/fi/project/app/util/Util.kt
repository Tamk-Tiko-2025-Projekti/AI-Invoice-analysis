package fi.project.app.util

import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.time.Instant
import kotlin.random.Random
import java.io.IOException
import java.nio.file.Paths
import kotlin.io.path.Path
import kotlin.io.path.exists

/**
 * Converts a PDF file to an image using a Python script.
 * The script is executed as a subprocess, and the output is saved in the same directory as the PDF.
 * Throws a RuntimeException if the conversion fails.
 *
 * @param pdfFile the PDF file to be converted
 */
fun convertPDFToImage(pdfFile: File) {
    try {
        // Determine the Python command based on the operating system
        val pythonCommand = if (System.getProperty("os.name").contains("Windows", ignoreCase = true)) "python" else "python3"

        // Get the absolute path of the Python script and the output directory
        val scriptPath = File("src/main/kotlin/fi/project/app/util/convertpdf.py").absolutePath
        val outputDir = pdfFile.parentFile.absolutePath

        // Execute the Python script as a subprocess
        val process = ProcessBuilder(pythonCommand, scriptPath, pdfFile.absolutePath, outputDir)
            .redirectErrorStream(true)
            .start()

        // Capture the output and error streams
        val output = process.inputStream.bufferedReader().use { it.readText() }
        val error = process.errorStream.bufferedReader().use { it.readText() }
        val success = process.waitFor() == 0

        // Throw an exception if the process fails
        if (!success) {
            throw RuntimeException("Conversion failed:\n$output\n$error")
        }
    } catch (e: Exception) {
        throw RuntimeException("Failed to convert PDF: ${e.message}")
    }
}

/**
 * Saves a file to the specified path relative to the project root directory.
 * Ensures the directory exists before saving the file.
 *
 * @param file the file to save, as MultipartFile
 * @param path the relative path where the file should be saved
 * @return the saved File object
 */
fun saveFile(file: MultipartFile, path: String): File {
    // Construct the full path and normalize it for cross-platform compatibility
    var newPath = System.getProperty("user.dir") + path
    newPath = newPath.replace("\\", "/")
    println("New path: $newPath")

    // Create the directory if it doesn't exist
    val directory = File(newPath)
    if (!directory.exists()) {
        directory.mkdir()
    }

    // Save the file with a temporary name based on its extension
    val fileExtension = file.originalFilename?.substringAfterLast(".")
    val savedFile = File(newPath, "temp.$fileExtension")
    file.transferTo(savedFile)
    println("File saved to: ${savedFile.absolutePath}")

    return savedFile
}

/**
 * Creates a unique storage directory for a file and saves the file within it.
 * The directory name is generated using the current timestamp and a random letter.
 * Returns a StorageInfo object containing details about the stored file and directory.
 *
 * @param file the file to be stored, as MultipartFile
 * @param baseDir the base directory where the storage directory will be created (default is "temp")
 * @return a StorageInfo object with details about the stored file and directory
 * @throws IOException if the directory cannot be created or the file cannot be saved
 */
fun createStorage(file: MultipartFile, baseDir: String = "temp"): StorageInfo {
    // Generate a unique directory name
    val timeStamp = Instant.now().toEpochMilli()
    val randomLetter = ('a'..'z').random()
    val dirName = "$timeStamp-$randomLetter"

    // Resolve the base directory relative to the project root
    val projectRoot = Paths.get("").toAbsolutePath().toFile()
    val directory = File(projectRoot, "$baseDir/$dirName")

    // Create the directory if it doesn't exist
    if (!directory.exists()) {
        val created = directory.mkdirs()
        if (created) {
            println("Created storage directory: ${directory.absolutePath}")
        } else {
            throw IOException("Failed to create directory: ${directory.absolutePath}")
        }
    }

    // Determine the file's extension and construct its name
    val originalExt = file.originalFilename?.substringAfterLast('.', "")
    val filename = if (originalExt.isNullOrBlank()) dirName else "$dirName.$originalExt"

    val savedFile = File(directory, filename)

    // Save the file and handle exceptions
    try {
        file.transferTo(savedFile)
        println("Primary file saved as: ${savedFile.absolutePath}")
    } catch (e: Exception) {
        throw IOException("Failed to save file: ${savedFile.absolutePath}", e)
    }

    // Return storage information
    return StorageInfo(
        directoryPath = directory.absolutePath,
        filePath = savedFile.absolutePath,
        originalFilename = file.originalFilename,
        originalExtension = originalExt
    )
}

/**
 * Data class representing storage information for a file.
 * Provides functionality to manage files in a specific directory, including logging actions.
 *
 * @property directoryPath the path to the directory where the file is stored
 * @property filePath the path to the file itself
 * @property originalFilename the original filename of the uploaded file
 * @property originalExtension the original file extension, derived from the filename
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
        // Ensure the directory exists
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                throw IOException("Failed to create directory: ${directory.absolutePath}")
            }
        }

        // Create the new file and log the action
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
        // Ensure the directory exists
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                throw IOException("Failed to create directory for log: ${directory.absolutePath}")
            }
        }

        // Create the log file if it doesn't exist
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


/**
 * Verifies the barcode information from an image file.
 *
 * @param data The StorageInfo object containing the file to be verified.
 * @return A string indicating the result of the barcode verification.
 * @throws RuntimeException If the barcode verification fails.
 */
fun verifyBarCode(data: StorageInfo): String {
    // Check if the temporary file exists
    val path = data.directoryPath
    var file = File(path, "temp.webp")
    if (!file.exists()) {
        file = File(path, data.file.name)
    }
    // Determine the Python command based on the operating system
    val pythonCommand = if (System.getProperty("os.name").contains("Windows", ignoreCase = true)) "python" else "python3"

    data.appendToLogFile("Verifying barcode for file: ${data.file.name}")

    val ProcessBuilder = ProcessBuilder(
        pythonCommand,
        "src/main/kotlin/fi/project/app/util/readBarCode.py", // Path to the readBarCode.py script, dehardcode this later
        file.absolutePath // Path to the file to be verified
    )
        .redirectErrorStream(true)
        .start()
    val output = ProcessBuilder.inputStream.bufferedReader().use { it.readText() }
    val error = ProcessBuilder.errorStream.bufferedReader().use { it.readText() }
    val success = ProcessBuilder.waitFor() == 0
    if (!success) {
        throw RuntimeException("Barcode verification failed:\n$output\n$error")
    } else {
        data.appendToLogFile("Barcode verification successful:\n$output")
        println("Barcode verification output:\n$output")
        return output
    }
}
