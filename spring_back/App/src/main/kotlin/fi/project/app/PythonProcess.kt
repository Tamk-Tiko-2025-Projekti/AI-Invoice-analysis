import com.fasterxml.jackson.module.kotlin.kotlinModule
import fi.project.app.util.getPythonInterpreter
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

// This class is responsible for executing a Python script with specified arguments.
class PythonProcess {
    companion object {
        /**
         * Executes a Python script with the provided arguments.
         *
         * @param imageFile An optional image file to be processed by the script. If null, a placeholder is used.
         * @param devPromptFile A file containing the developer's prompt.
         * @param userPromptFile A file containing the user's prompt.
         * @param testRun A flag indicating whether this is a test run.
         * @param expectJson A flag indicating whether the script is expected to return JSON output.
         * @return The output of the Python script as a String.
         * @throws RuntimeException If an error occurs during script execution.
         */
        fun runScript(
            imageFile: File?,
            devPromptFile: File,
            userPromptFile: File,
            testRun: Boolean,
            expectJson: Boolean,
        ): String {
            try {
                //TODO: move to smarter place
                val scriptPath = File("prompt.py").absolutePath

                // Prepare the list of arguments to pass to the Python script.
                // The first argument is the path to the Python interpreter in the virtual environment.
                // The second argument is the path to the python script that is going to be run.
                val args = mutableListOf(getPythonInterpreter(), scriptPath)

                // Add the image file path or a placeholder if no image is provided.
                args.add(imageFile?.absolutePath ?: "-")

                // Add the file paths for the developer's and user's prompts.
                args.add(devPromptFile.absolutePath)
                args.add(userPromptFile.absolutePath)

                // Add flags for test run and JSON output expectation.
                args.add(testRun.toString())
                args.add(expectJson.toString())

                // Configure the process builder to execute the Python script.
                val processBuilder = ProcessBuilder(args)
                processBuilder.redirectErrorStream(true) // Redirect error stream to the output stream.
                val process = processBuilder.start() // Start the process.

                // Read the output of the Python script.
                val output = BufferedReader(InputStreamReader(process.inputStream)).readText()
                process.waitFor() // Wait for the process to complete.

                return output // Return the script's output.
            } catch (e: Exception) {
                // Print the stack trace and throw a runtime exception if an error occurs.
                e.printStackTrace()
                throw RuntimeException("Error running Python script: ${e.message}")
            } finally {
                // Cleanup logic for temporary files can be added here if needed.
//                imageFile?.let {
//                    println("Deleting temporary image file: ${it.absolutePath}")
//                    it.delete()
//                }
            }
        }
    }
}
