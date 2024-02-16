package lunakoly.spikesdetection.modes

import lunakoly.spikesdetection.data.DataFile
import lunakoly.spikesdetection.data.parseDataFile
import lunakoly.spikesdetection.util.div
import org.jetbrains.kotlinx.kandy.dsl.internal.DataFramePlotContext
import org.jetbrains.kotlinx.kandy.dsl.plot
import org.jetbrains.kotlinx.kandy.letsplot.export.save
import org.jetbrains.kotlinx.kandy.letsplot.feature.layout
import java.io.File

inline fun transformFilesToOutput(
    inputFiles: List<String>,
    outputFile: String,
    pathPrefix: String?,
    analyseGraph: DataFramePlotContext<*>.(DataFile) -> Unit,
) {
    val inputData = inputFiles.map(::File)
        .flatMap {
            if (it.isDirectory) {
                it.listFiles()?.toList().orEmpty()
            } else {
                listOf(it)
            }
        }
        .map {
            println("=> Parsing file ${it.path}")
            parseDataFile(it.readText()) to it
        }

    val filesIndices = mutableMapOf<String, Int>()

    for ((data, file) in inputData) {
        val newFileName = pathPrefix
            ?.let { file.path.removePrefix(it) }
            ?.removePrefix(File.separator)
            ?.replace(File.separator, "-")
            ?: ""

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
