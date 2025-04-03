package fi.project.app.util

import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.util.*

fun convertPDFToImage(file: File) {
    println("Converting PDF at ${file.absolutePath} to image...")
    var pythonCommand = "python3"
    if (System.getProperty("os.name").lowercase().contains("windows")) {
        pythonCommand = "python"
    }

    val processBuilder = ProcessBuilder(
        pythonCommand,
        System.getProperty("user.dir") + "/App/src/main/kotlin/fi/project/app/util/convertpdf.py",
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
