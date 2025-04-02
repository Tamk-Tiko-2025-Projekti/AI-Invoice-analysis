package fi.project.app.util

import org.springframework.web.multipart.MultipartFile
import java.io.File

fun convertToPDF() {
    val processBuilder = ProcessBuilder(
        "python",
        "./src/main/kotlin/fi/project/app/util/convertpdf.py",
        "./temp/temp.pdf",
    )
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
