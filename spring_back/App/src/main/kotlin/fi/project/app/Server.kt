package fi.project.app

import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.http.ResponseEntity
import org.springframework.http.HttpStatus
import java.io.File
import java.io.BufferedReader
import java.io.InputStreamReader
import fi.project.app.util.saveFile
import fi.project.app.util.convertPDFToImage
import fi.project.app.util.createStorage
import fi.project.app.util.StorageInfo
import fi.project.app.util.createStorage

@RestController
@RequestMapping("/")
class Server {
    @GetMapping("/")
    fun hello(): String {
        return "Hello World!"
    }

    private fun processFile(
        file: MultipartFile,
        testRun: Boolean,
        preProcessing: ((File, StorageInfo) -> File)? = null
    ): ResponseEntity<String> {
        println("Received file: ${file.originalFilename}")
        println("Test run: $testRun")

        return try {
            val storageInfo = createStorage(file)
            storageInfo.appendToLogFile("Received file: ${file.originalFilename}")

            val processedFile = preProcessing?.invoke(storageInfo.file, storageInfo) ?: storageInfo.file

            println("Running Python script on: ${processedFile.absolutePath}")
            storageInfo.appendToLogFile("Running Python script on: ${processedFile.name}")
            val output = PythonProcess.runScript(processedFile, testRun)

            println("Python script output: $output")
            storageInfo.appendToLogFile("Python script output: $output")

            ResponseEntity(output, HttpStatus.OK)
        } catch (e: Exception) {
            e.printStackTrace()
            ResponseEntity("error", HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @PostMapping("/image")
    fun postImage(
        @RequestParam("image") file: MultipartFile,
        @RequestParam(name = "testRun", required = false, defaultValue = "false") testRun: Boolean
    ): ResponseEntity<String> {
        return processFile(file, testRun)
    }

    @PostMapping("/pdf")
    fun postPDF(
        @RequestParam("pdf") file: MultipartFile,
        @RequestParam(name = "testRun", required = false, defaultValue = "false") testRun: Boolean
    ): ResponseEntity<String> {
        return processFile(file, testRun) { pdfFile, storageInfo ->
            println("Running PDF to image conversion...")
            storageInfo.appendToLogFile("Running PDF to image conversion...")

            convertPDFToImage(pdfFile)

            val outputImage = File(storageInfo.directoryPath, "temp.webp")
            if (!outputImage.exists()) {
                storageInfo.appendToLogFile("Converted file does not exist: ${outputImage.absolutePath}")
                throw Exception("Converted file does not exist")
            }
            outputImage
        }
    }
}
