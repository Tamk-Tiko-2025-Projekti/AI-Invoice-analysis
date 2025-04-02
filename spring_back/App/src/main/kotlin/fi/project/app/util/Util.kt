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
    // Create the directory if it doesn't exist
    val directory = File(path)
    if (!directory.exists()) {
        directory.mkdir()
    }

    /* The file is saved in the project root directory. */
    val savedFile = File(path, file.originalFilename ?: "tempfile")
    file.transferTo(savedFile)

    return savedFile
}
