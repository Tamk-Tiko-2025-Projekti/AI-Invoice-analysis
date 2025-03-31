package fi.project.app

import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.BufferedReader
import java.io.InputStreamReader

class PythonProcess {
    companion object {
        fun runScript(file: MultipartFile, testRun: Boolean): String {
            val tempFile = saveFile(file)
            println("Temporary file created: ${tempFile.absolutePath}")
            try {
                val processBuilder = ProcessBuilder("python", "./prompt.py", tempFile.absolutePath, testRun.toString())
                processBuilder.redirectErrorStream(true)
                val process = processBuilder.start()

                val output = BufferedReader(InputStreamReader(process.inputStream)).readText()
                process.waitFor()

                return output
            } catch (e: Exception) {
                e.printStackTrace()
                throw RuntimeException("Error running Python script: ${e.message}")
            } finally {
                println("Deleting temporary file: ${tempFile.absolutePath}")
                tempFile.delete()
            }
        }
    }
}

fun saveFile(file: MultipartFile): File {
    val path = File(System.getProperty("user.dir"))
    /* The file is saved in the project root directory. */
    val savedFile = File(path, file.originalFilename ?: "tempfile")
    file.transferTo(savedFile)

    return savedFile
}
