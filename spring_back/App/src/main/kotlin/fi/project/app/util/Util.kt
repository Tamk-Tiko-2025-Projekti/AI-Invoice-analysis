package fi.project.app.util

fun convertToPDF() {
    val processBuilder = ProcessBuilder(
        "python",
        "./src/main/kotlin/fi/project/app/util/convertpdf.py",
        "./temp/temp.pdf",
    )
}
