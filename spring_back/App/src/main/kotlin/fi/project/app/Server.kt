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
        try {
            val tempFile = saveFile(file, "/temp")
            println("Running Python script...")
            val output = PythonProcess.runScript(tempFile, testRun)
            println("Python script output: $output")
            //val jsonResponse = ObjectMapper().readTree(output)
            return ResponseEntity(output, HttpStatus.OK)
        } catch (e: Exception) {
            e.printStackTrace()
            //val message = ObjectMapper().createObjectNode()
            return ResponseEntity("error", HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @PostMapping("/pdf")
    fun postPDF(
        @RequestParam("pdf") file: MultipartFile,
        @RequestParam(name = "testRun", required = false, defaultValue = "false") testRun: Boolean,
    ): ResponseEntity<String> {
        println("Received file: ${file.originalFilename}")
        println("Test run: $testRun")
        try {
            val tempFile = saveFile(file, "/temp")
            println("Temporary file created: ${tempFile.absolutePath}")
            println("Running PDF to image conversion...")
            convertPDFToImage(tempFile)

            val convertedFile = File(System.getProperty("user.dir") + "/temp/temp.webp")
            println("Converted file: ${convertedFile.absolutePath}")
            if (!convertedFile.exists()) {
                println("Converted file does not exist: ${convertedFile.absolutePath}")
                tempFile.delete()
                return ResponseEntity("error", HttpStatus.INTERNAL_SERVER_ERROR)
            }
            println("Running Python script on $convertedFile at ${convertedFile.absolutePath}...")
            val output = PythonProcess.runScript(convertedFile, testRun)
            println("Python script output: $output")
            //val jsonResponse = ObjectMapper().readTree(output)
            return ResponseEntity(output, HttpStatus.OK)
        } catch (e: Exception) {
            e.printStackTrace()
            //val message = ObjectMapper().createObjectNode()
            return ResponseEntity("error", HttpStatus.INTERNAL_SERVER_ERROR)
        } finally {
            val tempPDF = File(System.getProperty("user.dir") + "/temp/temp.pdf")
            if (tempPDF.exists()) {
                println("Deleting temporary PDF file: ${tempPDF.absolutePath}")
                tempPDF.delete()
            } else {
                println("Temporary PDF file does not exist: ${tempPDF.absolutePath}")
            }
        }
    }
}
