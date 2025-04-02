package fi.project.app

import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.http.ResponseEntity
import org.springframework.http.HttpStatus
import java.io.File
import java.io.BufferedReader
import java.io.InputStreamReader
import fi.project.app.util.saveFile

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.JsonNode

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
            saveFile(file, "./temp")
            println("Running Python script...")
            val output = PythonProcess.runScript(file, testRun)
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
            saveFile(file, "./temp")
            println("Running Python script...")
            val output = PythonProcess.runScript(file, testRun)
            println("Python script output: $output")
            //val jsonResponse = ObjectMapper().readTree(output)
            return ResponseEntity(output, HttpStatus.OK)
        } catch (e: Exception) {
            e.printStackTrace()
            //val message = ObjectMapper().createObjectNode()
            return ResponseEntity("error", HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }
}
