package fi.project.app

import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.http.ResponseEntity
import org.springframework.http.HttpStatus
import java.io.File
import fi.project.app.util.convertPDFToImage
import fi.project.app.util.pdfPreProcessing
import fi.project.app.util.createStorage
import fi.project.app.util.StorageInfo
import fi.project.app.util.verifyBarCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking


@RestController
@RequestMapping("/")
class Server {

    // Endpoint to check if the server is running
//    @GetMapping("/")
//    fun hello(): String {
//        return "Hello World!"
//    }

    // Core function to process uploaded files
    private fun processFile(
        file: MultipartFile, // Uploaded file
        testRun: Boolean, // Flag to indicate if this is a test run
        preProcessing: ((File, StorageInfo) -> File)? = null // Optional preprocessing step
    ): String {
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
//            ResponseEntity(output, HttpStatus.OK)
            output
        } catch (e: Exception) {
            // Handle exceptions and return an error response
            e.printStackTrace()
//            ResponseEntity("error", HttpStatus.INTERNAL_SERVER_ERROR)
            throw RuntimeException("Error processing file: ${e.message}")
        }
    }

    // Endpoint to process image files
    @PostMapping("/image")
    fun postImage(
        @RequestParam("image") file: MultipartFile, // Uploaded image file
        @RequestParam(name = "testRun", required = false, defaultValue = "false") testRun: Boolean // Test run flag
    ): ResponseEntity<String> {
//        return processFile(file, testRun)
        try {
            return ResponseEntity(processFile(file, testRun), HttpStatus.OK)
        } catch (e: Exception) {
            println("Error processing image file: ${e.message}")
            return ResponseEntity(e.message, HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    // Endpoint to process PDF files
    @PostMapping("/pdf")
    fun postPDF(
        @RequestParam("pdf") file: MultipartFile, // Uploaded PDF file
        @RequestParam(name = "testRun", required = false, defaultValue = "false") testRun: Boolean // Test run flag
    ): ResponseEntity<String> {
//        return processFile(file, testRun) { pdfFile, storageInfo ->
//            pdfPreProcessing(pdfFile, storageInfo)
//        }
        try {
            return ResponseEntity(processFile(file, testRun) { pdfFile, storageInfo ->
                pdfPreProcessing(pdfFile, storageInfo)
            }, HttpStatus.OK)
        } catch (e: Exception) {
            println("Error processing PDF file: ${e.message}")
            return ResponseEntity(e.message, HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @PostMapping("/files")
    fun postMultipleFiles(
        @RequestParam("files") files: List<MultipartFile>, // List of uploaded files
        @RequestParam(name = "testRun", required = false, defaultValue = "false") testRun: Boolean // Test run flag
    ): ResponseEntity<List<String>> {
        println("Received ${files.size} files")
        try {
//            processFiles(files)
            val results = runBlocking {
                files.map { file ->
                    async(Dispatchers.IO) {
                        try {
                            when {
                                file.contentType == "application/pdf" -> {
                                    // Process PDF files
                                    //TODO: un-hardcode the testRun
                                    processFile(file, testRun = true) { pdfFile, storageInfo ->
                                        pdfPreProcessing(pdfFile, storageInfo)
                                    }
                                }
                                file.contentType?.startsWith("image/") == true -> {
                                    // Process image files
                                    //TODO: un-hardcode the testRun
                                    processFile(file, testRun = true)
                                }
                                else -> {
                                    throw IllegalArgumentException("Unsupported file type: ${file.contentType}")
                                }
                            }
//                            if (file.contentType == "application/pdf") {
//                                // Process PDF files
//                                processFile(file, testRun) { pdfFile, storageInfo ->
//                                    pdfPreProcessing(pdfFile, storageInfo)
//                                }
//                            } else {
//                                // Process image files
//                                processFile(file, testRun)
//                            }
                        } catch (e: Exception) {
                            println("Error processing file ${file.originalFilename}: ${e.message}")
                            "Error processing file ${file.originalFilename}: ${e.message}"
                        }
                    }
                }.awaitAll()
            }
            println("Processed ${files.size} files")
            // Return the results as a response
            println("Results: $results")
            return ResponseEntity(results, HttpStatus.OK)
//            return ResponseEntity(results.joinToString("\n"), HttpStatus.OK)
        } catch (e: Exception) {
            println("Error processing files: ${e.message}")
//            return ResponseEntity(e.message, HttpStatus.INTERNAL_SERVER_ERROR)
            return ResponseEntity(listOf("Error processing files: ${e.message}"), HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }
}
