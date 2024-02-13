package lunakoly.frequencies

import lunakoly.arrrgh.fillFrom
import lunakoly.frequencies.data.parseDataFile
import lunakoly.frequencies.data.visualizeLine
import lunakoly.frequencies.data.visualizePoints
import lunakoly.frequencies.filtering.extractSpikes
import lunakoly.frequencies.filtering.fitMedianBySegmentsDynamically
import lunakoly.frequencies.filtering.visualizeFittings
import org.jetbrains.kotlinx.kandy.dsl.plot
import org.jetbrains.kotlinx.kandy.letsplot.export.save
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

    val colorProvider = RandomColorProvider()

    for ((data, file) in inputData) {
        println("=> Analysing graph ${file.path}")
        plot {
            visualizeLine(data.points, colorProvider)
            val fitting = data.points.fitMedianBySegmentsDynamically()
            visualizeFittings(fitting, colorProvider)
            val spikes = fitting.extractSpikes()
            visualizePoints(spikes, colorProvider)
        }.save(options.outputFile / file.name + ".png")
    }

    println("Done!")
}
