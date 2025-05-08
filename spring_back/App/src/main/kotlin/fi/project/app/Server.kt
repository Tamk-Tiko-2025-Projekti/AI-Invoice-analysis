package fi.project.app

import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.http.ResponseEntity
import org.springframework.http.HttpStatus
import org.springframework.context.ApplicationContext
import java.io.File
import fi.project.app.util.pdfPreProcessing
import fi.project.app.util.createStorage
import fi.project.app.util.StorageInfo
import fi.project.app.util.verifyBarCode
import fi.project.app.util.compareBarCodeData
import kotlinx.coroutines.*
import com.fasterxml.jackson.databind.ObjectMapper
import kotlin.random.Random
import kotlin.system.exitProcess
import mu.KotlinLogging
import org.springframework.context.ConfigurableApplicationContext
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

private val logger = KotlinLogging.logger {}
private val shutdownStarted = AtomicBoolean(false)


@RestController
@RequestMapping("/")
class Server(private val context: ApplicationContext) {

    /**
     * Processes the uploaded file and then runs the Python scripts that handle sending the file to the LLM.
     * @param file The uploaded file (should an image, if the original file is a PDF, it should be converted to an image first).
     * @param testRun Flag to indicate if this is a test run.
     * @param preProcessing An optional function to preprocess the file before running the Python script.
     * This can be used to convert PDF files to images or perform other preprocessing tasks.
     * The function receives two arguments:
     * - File: The file to be processed.
     * - StorageInfo: An object containing information about the storage location of the file.
     * The function should return:
     * - File: The processed file to be used in the Python script.
     * @return A string containing the output from the LLM.
     */
    private suspend fun processFile(
        file: MultipartFile, // Uploaded file
        testRun: Boolean, // Flag to indicate if this is a test run
        preProcessing: ((File, StorageInfo) -> File)? = null // Optional preprocessing step
    ): String {
        logger.info{"Received file: ${file.originalFilename}"}
        logger.info{"Test run: $testRun"}

        try {
            // Create storage for the uploaded file
            val storageInfo = createStorage(file)
            storageInfo.appendToLogFile("Received file: ${file.originalFilename}")

            // Apply preprocessing if provided
            val processedFile = preProcessing?.invoke(storageInfo.file, storageInfo) ?: storageInfo.file

            logger.info{"Running Python script on: ${processedFile.absolutePath}"}

            // Prompt files
            //TODO:move to better place
            val devPromptFile = File("dev_prompt.txt")
            val userPromptFile = File("user_prompt.txt")
            val turnToJsonFile = File("turn_to_json.txt")

            var output = if (testRun) {
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

                if (firstOutput.isEmpty()) {
                    throw RuntimeException("First prompt output is empty")
                }
                if (firstOutput.contains("Error")) {
                    throw RuntimeException("First prompt output contains error: $firstOutput")
                }

                // Save intermediate output to a file
                val intermediateFile = File(storageInfo.directory, "Intermediate.txt")
                storageInfo.appendToLogFile("Writing first output to Intermediate.txt")
                intermediateFile.writeText(firstOutput)

                // Second prompt execution to generate the final JSON output
                PythonProcess.runScript(
                    imageFile = null,
                    devPromptFile = turnToJsonFile,
                    userPromptFile = intermediateFile,
                    testRun = false,
                    expectJson = true,
                )
            }

            storageInfo.appendToLogFile("JSON output: $output")

            if (output.isEmpty()) {
                throw RuntimeException("Second prompt output is empty")
            }
            if (output.contains("Error")) {
                throw RuntimeException("Second prompt output contains error: $output")
            }

            try {
                val barCodeOutput: String = verifyBarCode(storageInfo)
                // This method will not be called if the barcode verification fails
                output = compareBarCodeData(
                    output,
                    barCodeOutput,
                    storageInfo
                ) // Output validation-field is updated if the barcode data does not match
                logger.info{"Barcode verification process finished."}
            } catch (e: Exception) {
                logger.error{"Error verifying barcode for file: ${file.originalFilename}:\n${e.message}"}
                storageInfo.appendToLogFile("Error verifying barcode:\n${e.message}")
            }
            // Return the final output as a response
            return output
        } catch (e: Exception) {
            // Handle exceptions and return an error response
            logger.error{e}
            throw RuntimeException(e.message)
        }
    }


    /**
     * NOTE: This endpoint is not used in the current version of the application.
     * Endpoint to process image files.
     * This endpoint accepts an image file and sends it to the LLM for processing.
     * The endpoint supports test runs, where the file is otherwise processed, but it is not sent to the LLM.
     * @Param image Uploaded image file. Note: must be named "image" in the form-data.
     * @Param testRun Flag to indicate if this is a test run.
     * @return ResponseEntity with the result of the processing and an HTTP status code.
     * If the processing is successful, the result is a JSON string containing the output of the processing, and the status code is 200 OK.
     * In case of an error, the result is a string containing the error message, and the status code is 500 Internal Server Error.
     */
    /*
    @PostMapping("/image")
    fun postImage(
        @RequestParam("image") file: MultipartFile, // Uploaded image file
        @RequestParam(name = "testRun", required = false, defaultValue = "false") testRun: Boolean // Test run flag
    ): ResponseEntity<String> {
        try {
            return ResponseEntity(processFile(file, testRun), HttpStatus.OK)
        } catch (e: Exception) {
            println("Error processing image file: ${e.message}")
            return ResponseEntity(e.message, HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }
    */

    /**
     * NOTE: This endpoint is not used in the current version of the application.
     * Endpoint to process PDF files.
     * This endpoint accepts a PDF file, converts it to an image and then sends it to the LLM for processing.
     * The endpoint supports test runs, where the file is otherwise processed, but it is not sent to the LLM.
     * @Param pdf Uploaded PDF file. Note: must be named "pdf" in the form-data.
     * @Param testRun Flag to indicate if this is a test run.
     * @return ResponseEntity with the result of the processing and an HTTP status code.
     * If the processing is successful, the result is a JSON string containing the output of the processing, and the status code is 200 OK.
     * In case of an error, the result is a string containing the error message, and the status code is 500 Internal Server Error.
     */
    /*
    @PostMapping("/pdf")
    fun postPDF(
        @RequestParam("pdf") file: MultipartFile, // Uploaded PDF file
        @RequestParam(name = "testRun", required = false, defaultValue = "false") testRun: Boolean // Test run flag
    ): ResponseEntity<String> {
        try {
            return ResponseEntity(processFile(file, testRun) { pdfFile, storageInfo ->
                pdfPreProcessing(pdfFile, storageInfo)
            }, HttpStatus.OK)
        } catch (e: Exception) {
            println("Error processing PDF file: ${e.message}")
            return ResponseEntity(e.message, HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }
     */

    /**
     * Endpoint to process multiple files.
     * This endpoint accepts a list of PDF and/or image files and processes them concurrently, using coroutines.
     * The endpoint supports test runs, where the files are otherwise processed, but they are not sent to the LLM.
     * @Param files List of uploaded files. Note: must be named "files" in the form-data.
     * @Param testRun Flag to indicate if this is a test run.
     */
    @PostMapping("/files")
    suspend fun postMultipleFiles(
        @RequestParam("files") files: List<MultipartFile>, // List of uploaded files
        @RequestParam(name = "testRun", required = false, defaultValue = "false") testRun: Boolean // Test run flag
    ): ResponseEntity<List<Map<String, Any>>> {
        logger.info{"Received ${files.size} files"}
        // Process each file concurrently using coroutines
        val results = withContext(Dispatchers.IO) {
            files.map { file ->
                async {
                    try {
                        val fileName = file.originalFilename ?: "unknown"
                        val contentType = file.contentType ?: "unknown"
                        val output = when {
                            file.contentType == "application/pdf" -> {
                                // Process PDF files
                                processFile(file, testRun) { pdfFile, storageInfo ->
                                    pdfPreProcessing(pdfFile, storageInfo)
                                }
                            }

                            file.contentType?.startsWith("image/") == true -> {
                                // Process image files
                                processFile(file, testRun)
                            }

                            else -> {
                                throw IllegalArgumentException("Unsupported file type: ${file.contentType}")
                            }
                        }
                        val objectMapper = ObjectMapper()
                        val jsonOutput = objectMapper.readValue(output, Map::class.java) as Map<String, Any>
                        logger.info{"Processed file ${file.originalFilename}: $jsonOutput"}
                        jsonOutput
                    } catch (e: Exception) {
                        logger.error{"Error processing file ${file.originalFilename}: ${e.message}"}
                        mapOf(
                            "content" to emptyMap<String, Any>(),
                            "error" to mapOf(
                                "message" to "Error processing file: ${file.originalFilename}: ${e.message}",
                            )
                        )
                    }
                }
            }.awaitAll()
        }
        logger.info{"Processed ${files.size} files"}
        // Return the results as a response
        logger.info{"Results: $results"}
        return ResponseEntity(results, HttpStatus.OK)
    }

    @PostMapping("/shutdown")
    fun shutdown(): ResponseEntity<String> {

        if (shutdownStarted.get()) {
            logger.warn{"Additional shutdown request received while shutdown is already in progress."}
            return ResponseEntity("Shutdown already in progress", HttpStatus.TOO_MANY_REQUESTS)
        }
        shutdownStarted.set(true)
        logger.warn{"Server shutdown initiated."}

        // Clears temp folder
        try {
            logger.info{"Attempting to clear temp files"}
            val tempDir = File("./temp")
            if (tempDir.exists()) {
                tempDir.deleteRecursively()
                logger.info{"Temporary files cleared"}
            } else {
                logger.warn{"Temporary directory does not exist"}
            }
        } catch (e: Exception) {
            logger.error{"Error clearing temporary files: ${e.message}"}
        }

        //delays the server shutdown so that processes can finish
        val executor = Executors.newSingleThreadScheduledExecutor()
        executor.submit {
            try {
                logger.info{"Server is shutting down in 5 seconds"}
                Thread.sleep(5000)
                (context as ConfigurableApplicationContext).close()
                logger.info{"Server has shut down"}
            } catch (e: Exception) {
                logger.error{"Error during server shutdown: ${e.message}"}
            } finally {
                logger.info{"Exiting JVM"}
                executor.shutdown()
                exitProcess(0)
            }
        }

        return ResponseEntity("Server has shut down", HttpStatus.OK)

    }
}
