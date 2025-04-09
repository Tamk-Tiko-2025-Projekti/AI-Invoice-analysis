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

    @PostMapping("/image")
    fun postImage(
        @RequestParam("image") file: MultipartFile,
        @RequestParam(name = "testRun", required = false, defaultValue = "false") testRun: Boolean
    ): ResponseEntity<String> {
        println("Received file: ${file.originalFilename}")
        println("Test run: $testRun")

        return try {
            //uses StorageInfo from util
            val storageInfo = createStorage(file)

            storageInfo.appendToLogFile("Received file: ${file.originalFilename}")
            println("Running Python script")
            storageInfo.appendToLogFile("Running Python script")
            val output = PythonProcess.runScript(storageInfo.file, testRun)
            storageInfo.appendToLogFile("Python script output: $output")
            println("Python script output: $output")

            ResponseEntity(output, HttpStatus.OK)
        } catch (e: Exception) {
            e.printStackTrace()
            ResponseEntity("error", HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @PostMapping("/pdf")
    fun postPDF(
        @RequestParam("pdf") file: MultipartFile,
        @RequestParam(name = "testRun", required = false, defaultValue = "false") testRun: Boolean
    ): ResponseEntity<String> {
        println("Received file: ${file.originalFilename}")
        println("Test run: $testRun")
        return try {
            // Store the incoming PDF file in the temp directory
            val storageInfo = createStorage(file)
            storageInfo.appendToLogFile("Received PDF: ${file.originalFilename}")
            println("Running PDF to image conversion...")
            storageInfo.appendToLogFile("Running PDF to image conversion...")

            // Convert the saved PDF file
            convertPDFToImage(storageInfo.file)

            // Check for converted image
            val outputImage = File(storageInfo.directoryPath, "temp.webp")
            if (!outputImage.exists()) {
                storageInfo.appendToLogFile("Converted file does not exist: ${outputImage.absolutePath}")
                return ResponseEntity("error", HttpStatus.INTERNAL_SERVER_ERROR)
            }

            println("Running Python script on: ${outputImage.absolutePath}")
            storageInfo.appendToLogFile("Running Python script on: ${outputImage.name}")
            val output = PythonProcess.runScript(outputImage, testRun)

            println("Python script output: $output")
            storageInfo.appendToLogFile("Python script output: $output")

            ResponseEntity(output, HttpStatus.OK)
        } catch (e: Exception) {
            e.printStackTrace()
            ResponseEntity("error", HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }
}
