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
import fi.project.app.util.verifyBarCode

@RestController
@RequestMapping("/")
class Server {

    // Endpoint to check if the server is running
    @GetMapping("/")
    fun hello(): String {
        return "Hello World!"
    }

    // Core function to process uploaded files
    private fun processFile(
        file: MultipartFile, // Uploaded file
        testRun: Boolean, // Flag to indicate if this is a test run
        preProcessing: ((File, StorageInfo) -> File)? = null // Optional preprocessing step
    ): ResponseEntity<String> {
        println("Received file: ${file.originalFilename}")
        println("Test run: $testRun")

        return try {
            // Create storage for the uploaded file
            val storageInfo = createStorage(file)
            storageInfo.appendToLogFile("Received file: ${file.originalFilename}")

            // Apply preprocessing if provided
            val processedFile = preProcessing?.invoke(storageInfo.file, storageInfo) ?: storageInfo.file

            println("Running Python script on: ${processedFile.absolutePath}")
            storageInfo.appendToLogFile("Running Python script on: ${processedFile.name}")

            // Prompt files
            //TODO:move to better place
            val devPromptFile = File("dev_prompt.txt")
            val userPromptFile = File("user_prompt.txt")
            val turnToJsonFile = File("turn_to_json.txt")
            val venvPath = "${System.getProperty("user.dir")}/venv"

            val output = if (testRun) {
                // Run the Python script once during test runs
                PythonProcess.runScript(
                    imageFile = processedFile,
                    devPromptFile = devPromptFile,
                    userPromptFile = userPromptFile,
                    testRun = true,
                    expectJson = true,
                )
            } else {
                // First prompt execution
                val firstOutput = PythonProcess.runScript(
                    imageFile = processedFile,
                    devPromptFile = devPromptFile,
                    userPromptFile = userPromptFile,
                    testRun = false,
                    expectJson = false,
                )

                // Save intermediate output to a file
                val intermediateFile = File(storageInfo.directory, "Intermediate.txt")
                println("First part done, writing to Intermediate.txt")
                storageInfo.appendToLogFile("Writing first output to Intermediate.txt")
                intermediateFile.writeText(firstOutput)

                // Second prompt execution to generate final JSON output
                PythonProcess.runScript(
                    imageFile = null,
                    devPromptFile = turnToJsonFile,
                    userPromptFile = intermediateFile,
                    testRun = false,
                    expectJson = true,
                )
            }

            println("Second prompt output: $output")
            storageInfo.appendToLogFile("JSON output: $output")

            try {
                val output: String = verifyBarCode(storageInfo)
                // TODO: verify the AI output with the barcode data
            } catch (e: Exception) {
                println("Error verifying barcode:\n${e.message}")
                storageInfo.appendToLogFile("Error verifying barcode:\n${e.message}")
            }
            // Return the final output as a response
            ResponseEntity(output, HttpStatus.OK)
        } catch (e: Exception) {
            // Handle exceptions and return an error response
            e.printStackTrace()
            ResponseEntity("error", HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    // Endpoint to process image files
    @PostMapping("/image")
    fun postImage(
        @RequestParam("image") file: MultipartFile, // Uploaded image file
        @RequestParam(name = "testRun", required = false, defaultValue = "false") testRun: Boolean // Test run flag
    ): ResponseEntity<String> {
        return processFile(file, testRun)
    }

    // Endpoint to process PDF files
    @PostMapping("/pdf")
    fun postPDF(
        @RequestParam("pdf") file: MultipartFile, // Uploaded PDF file
        @RequestParam(name = "testRun", required = false, defaultValue = "false") testRun: Boolean // Test run flag
    ): ResponseEntity<String> {
        return processFile(file, testRun) { pdfFile, storageInfo ->
            println("Running PDF to image conversion...")
            storageInfo.appendToLogFile("Running PDF to image conversion...")

            // Convert the PDF to an image
            convertPDFToImage(pdfFile)

            // Check if the converted image exists
            val outputImage = File(storageInfo.directoryPath, "temp.webp")
            if (!outputImage.exists()) {
                storageInfo.appendToLogFile("Converted file does not exist: ${outputImage.absolutePath}")
                throw Exception("Converted file does not exist")
            }
            outputImage
        }
    }
}
