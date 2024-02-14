package lunakoly.frequencies

import lunakoly.arrrgh.fillFrom
import lunakoly.frequencies.RandomColorProvider.Companion.DIAMETER_STEP_ANGLE
import lunakoly.frequencies.data.*
import lunakoly.frequencies.filtering.*
import org.jetbrains.kotlinx.kandy.dsl.internal.DataFramePlotContext
import org.jetbrains.kotlinx.kandy.dsl.plot
import org.jetbrains.kotlinx.kandy.letsplot.export.save
import org.jetbrains.kotlinx.kandy.letsplot.feature.layout
import java.io.File

operator fun String.div(other: String) = "$this${File.separator}$other"

fun main(args: Array<String>) {
    val options = InputOptions().fillFrom(args) ?: return

    if (!File(options.outputFile).isDirectory) {
        println("Error > Output file must be a directory > ${options.outputFile} is not")
        return
    }

    val fitting = when (options.fitting) {
        "median" -> List<Point>::fitMedian
        "linear" -> List<Point>::fitLinear
        else -> return println("Error > `${options.fitting}` is not a supported fitting method")
    }

    val inputData = options.inputFiles.map(::File)
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
        val index = filesIndices[file.name]
        val suffix = index?.let { ".$index" } ?: ""
        val nextIndex = (index ?: 1) + 1
        filesIndices[file.name] = nextIndex

        println("=> Analysing graph ${file.path}")

        plot {
            fitAndVisualize(data, fitting)
            layout.size = 1920 to 1080
        }.save(options.outputFile / file.name + "$suffix.png")
    }

    println("Done!")
}

inline fun <F : Fitting> DataFramePlotContext<*>.fitAndVisualize(
    data: DataFile,
    fit: (List<Point>) -> F,
) {
    val colorProvider = RandomColorProvider(3.0 / 5 * DIAMETER_STEP_ANGLE)
    visualizeLine(data.points, colorProvider.nextColor())
    val fitting = data.points.fitBySegmentsDynamically(fit = fit)
    visualize(fitting, colorProvider.nextColor(), colorProvider.nextColor(), colorProvider.nextColor())
    val spikes = fitting.extractSpikes()
    visualizePoints(spikes, colorProvider.nextColor())
}
