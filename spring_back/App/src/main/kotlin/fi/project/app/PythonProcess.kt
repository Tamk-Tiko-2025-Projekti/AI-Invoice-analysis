package fi.project.app

import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.BufferedReader
import java.io.InputStreamReader

class PythonProcess {
    companion object {
        fun runScript(tempFile: File, testRun: Boolean): String {
//            val tempFile = saveFile(file)
//            println("Temporary file created: ${tempFile.absolutePath}")
            val isWindows = System.getProperty("os.name").lowercase().contains("windows")
            try {
                val pythonCommand = if (isWindows) {
                    "python"
                } else {
                    "python3"
                }
                val processBuilder =
                    ProcessBuilder(pythonCommand, "./prompt.py", tempFile.absolutePath, testRun.toString())
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

