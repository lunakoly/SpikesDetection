package lunakoly.frequencies

import lunakoly.arrrgh.fillFrom
import lunakoly.frequencies.RandomColorProvider.Companion.DIAMETER_STEP_ANGLE
import lunakoly.frequencies.data.parseDataFile
import lunakoly.frequencies.data.visualizeLine
import lunakoly.frequencies.data.visualizePoints
import lunakoly.frequencies.filtering.extractSpikes
import lunakoly.frequencies.filtering.fitMedianBySegmentsDynamically
import lunakoly.frequencies.filtering.visualizeFittings
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
        val colorProvider = RandomColorProvider(3.0 / 5 * DIAMETER_STEP_ANGLE)

        plot {
            visualizeLine(data.points, colorProvider.nextColor())
            val fitting = data.points.fitMedianBySegmentsDynamically()
            visualizeFittings(fitting, colorProvider.nextColor(), colorProvider.nextColor(), colorProvider.nextColor())
            val spikes = fitting.extractSpikes()
            visualizePoints(spikes, colorProvider.nextColor())
            layout.size = 1920 to 1080
        }.save(options.outputFile / file.name + "$suffix.png")
    }

    println("Done!")
}
