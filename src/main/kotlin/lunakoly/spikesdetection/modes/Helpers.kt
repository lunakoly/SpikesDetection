package lunakoly.spikesdetection.modes

import lunakoly.spikesdetection.data.DataFile
import lunakoly.spikesdetection.data.parseDataFile
import lunakoly.spikesdetection.util.div
import org.jetbrains.kotlinx.kandy.dsl.internal.DataFramePlotContext
import org.jetbrains.kotlinx.kandy.dsl.plot
import org.jetbrains.kotlinx.kandy.letsplot.export.save
import org.jetbrains.kotlinx.kandy.letsplot.feature.layout
import java.io.File

fun parseDataFilesIn(files: List<File>): Map<File, DataFile> = files
    .flatMap {
        if (it.isDirectory) {
            it.listFiles()?.toList().orEmpty()
        } else {
            listOf(it)
        }
    }.associateWith {
        println("=> Parsing file ${it.path}")
        parseDataFile(it.readText())
    }

inline fun transformFilesToOutput(
    inputFiles: List<String>,
    outputFile: String,
    pathPrefix: String?,
    analyseGraph: DataFramePlotContext<*>.(DataFile) -> Unit,
) {
    val inputData = parseDataFilesIn(inputFiles.map(::File))
    val filesIndices = mutableMapOf<String, Int>()

    for ((file, data) in inputData) {
        val newFileName = file.nameWrtPrefix(pathPrefix)
        val index = filesIndices[newFileName]
        val suffix = index?.let { ".$index" } ?: ""

        val nextIndex = (index ?: 1) + 1
        filesIndices[newFileName] = nextIndex

        println("=> Rendering $newFileName")

        plot {
            analyseGraph(data)
            layout.size = 1920 to 1080
        }.save(outputFile / newFileName + "$suffix.png")
    }
}

fun File.nameWrtPrefix(pathPrefix: String?): String = pathPrefix
    ?.let { path.removePrefix(it) }
    ?.removePrefix(File.separator)
    ?.replace(File.separator, "-")
    ?: name
