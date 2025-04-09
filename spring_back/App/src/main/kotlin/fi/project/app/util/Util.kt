package fi.project.app.util

import org.springframework.web.multipart.MultipartFile
import java.io.File

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
        directory.mkdirs()
    }

    /* The file is saved in the project root directory. */
//    val savedFile = File(newPath, file.originalFilename ?: "tempfile")
    val fileExtension = file.originalFilename?.substringAfterLast(".")
    val savedFile = File(newPath, "temp.$fileExtension")
    file.transferTo(savedFile)
    println("File saved to: ${savedFile.absolutePath}")

    return savedFile
}
