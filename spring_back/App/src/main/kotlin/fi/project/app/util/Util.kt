package fi.project.app.util

import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.time.Instant
import java.io.IOException
import java.nio.file.Paths

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import java.io.FileNotFoundException


/**
 * Converts a PDF file to an image using a Python script.
 * The script is executed as a subprocess, and the output is saved in the same directory as the PDF.
 * Throws a RuntimeException if the conversion fails.
 *
 * @param pdfFile the PDF file to be converted
 */
fun convertPDFToImage(pdfFile: File) {
    try {
        // Get the absolute path of the Python script and the output directory
        val scriptPath = File("src/main/kotlin/fi/project/app/util/convertpdf.py").absolutePath
        val outputDir = pdfFile.parentFile.absolutePath

        // Execute the Python script as a subprocess
        val process = ProcessBuilder(getPythonInterpreter(), scriptPath, pdfFile.absolutePath, outputDir)
            .redirectErrorStream(true)
            .start()

        // Capture the output and error streams
        val output = process.inputStream.bufferedReader().use { it.readText() }
        val error = process.errorStream.bufferedReader().use { it.readText() }
        val success = process.waitFor() == 0

        // Throw an exception if the process fails
        if (!success) {
            throw RuntimeException(output + "\n" + error)
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

    data.appendToLogFile("Verifying barcode for file: ${data.file.name}")

    val processBuilder = ProcessBuilder(
        getPythonInterpreter(),
        "src/main/kotlin/fi/project/app/util/readBarCode.py", // Path to the readBarCode.py script, de-hardcode this later
        file.absolutePath // Path to the file to be verified
    )
        .redirectErrorStream(true)
        .start()
    val output = processBuilder.inputStream.bufferedReader().use { it.readText() }
    val error = processBuilder.errorStream.bufferedReader().use { it.readText() }
    val success = processBuilder.waitFor() == 0
    if (!success) {
        throw RuntimeException("Barcode verification failed:\n$output\n$error")
    } else {
        data.appendToLogFile("Barcode verification successful:\n$output")
        println("Barcode verification output:\n$output")
        return output
    }
}

fun compareBarCodeData(data: String, barcodeData: String, storage: StorageInfo): String {
    val trimmedBarCode = barcodeData.trimIndent()

    // Attempt to parse the barcode data as JSON
    val mapper = ObjectMapper().registerKotlinModule()
    val barcodeNode = try {
        mapper.readTree(trimmedBarCode)
    } catch (e: Exception) {
        storage.appendToLogFile("Invalid JSON format in barcode data: ${e.message}")
        return data
    }
    val dataNode = mapper.readTree(data)
    val contentNode = dataNode.get("content")
    if (contentNode == null) {
        storage.appendToLogFile("No content field found in data.")
        return data
    }
    // Takes the common fields from both JSON objects
    val commonFields = barcodeNode.fieldNames().asSequence().toSet()
        .intersect(contentNode.fieldNames().asSequence().toSet())

    var differenceArray = mutableListOf<String>()
    for (field in commonFields) {
        val barcodeValue = barcodeNode.get(field).asText().replace(" ", "")
        val dataValue = contentNode.get(field).asText().replace(" ", "")
        if (barcodeValue != dataValue) {
            differenceArray.add(field)
        }
    }
    if (differenceArray.isEmpty()) {
        storage.appendToLogFile("No differences found between barcode and data.")
        return data
    } else {
        if (dataNode is ObjectNode) {
            val validationArray: ArrayNode = mapper.createArrayNode()
            for (diff in differenceArray) {
                validationArray.add(diff)
            }
            dataNode.set<ArrayNode>("validation", validationArray)

            val modifiedData = mapper.writeValueAsString(dataNode)
            storage.appendToLogFile("Differences found in barcode validation:\n${differenceArray.joinToString("\n")}")
            storage.appendToLogFile("Modified data with validation field:\n$modifiedData")
            return modifiedData
        } else {
            storage.appendToLogFile("Data is not an ObjectNode.")
            return data
        }
    }
}


/**
 * Returns the path to the python interpreter in the virtual environment.
 * The exact path is determined based on the operating system.
 *
 * @return The path to the python interpreter as a String.
 */
fun getPythonInterpreter(): String {
    val venvDir = findVenvDirectory()
    try {
        val pythonInterpreter = if (System.getProperty("os.name").lowercase().contains("windows")) {
            File(venvDir, "Scripts/python.exe").absolutePath
        } else {
            File(venvDir, "bin/python3").absolutePath
        }
        println("Python interpreter path: $pythonInterpreter")
        return pythonInterpreter
    } catch (e: Exception) {
        throw RuntimeException("Failed to find Python interpreter in virtual environment: ${e.message}")
    }
}

/**
 * Attempts to find the python virtual environment directory.
 * Checks several possible locations:
 * - The current working directory.
 * - The parent directory of the current working directory.
 * - A subdirectory named "spring_back" in the current working directory.
 * If none of these locations contains a "venv" directory, it defaults to the current working directory.
 *
 * @return The File object representing the venv directory.
 */
fun findVenvDirectory(): File {
    val projectRoot = File(System.getProperty("user.dir"))
    val possibleLocations = listOf(
        projectRoot,
        projectRoot.parentFile,
        File(projectRoot, "spring_back")
    )
    for (location in possibleLocations) {
        val venvDir = File(location, "venv")
        if (venvDir.exists() && venvDir.isDirectory) {
            return venvDir
        }
    }
    // If no venv directory is found, return the default location
    return File(projectRoot, "venv")
}

fun pdfPreProcessing(pdfFile: File, storageInfo: StorageInfo): File {
    println("Running PDF to image conversion...")
    storageInfo.appendToLogFile("Running PDF to image conversion...")

    // Convert the PDF to an image
    convertPDFToImage(pdfFile)

    // Check if the converted image exists
    val outputImage = File(storageInfo.directoryPath, "temp.webp")
    if (!outputImage.exists()) {
        storageInfo.appendToLogFile("Converted file does not exist: ${outputImage.absolutePath}")
        throw FileNotFoundException("Could not find image converted from PDF file")
    }
    return outputImage
}
