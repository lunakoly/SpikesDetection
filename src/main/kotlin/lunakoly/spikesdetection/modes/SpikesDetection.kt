package lunakoly.spikesdetection.modes

import lunakoly.spikesdetection.InputOptions
import lunakoly.spikesdetection.data.*
import lunakoly.spikesdetection.fitting.*
import lunakoly.spikesdetection.fitting.deviations.calculateFakeSigmaDeviation
import lunakoly.spikesdetection.fitting.deviations.calculateSigmaViaBinarySearchDeviation
import lunakoly.spikesdetection.fitting.median.fitConstant
import lunakoly.spikesdetection.fitting.median.fitLinear
import lunakoly.spikesdetection.util.RandomColorProvider
import lunakoly.spikesdetection.util.div
import org.jetbrains.kotlinx.kandy.dsl.internal.DataFramePlotContext
import org.jetbrains.kotlinx.kandy.dsl.plot
import org.jetbrains.kotlinx.kandy.letsplot.export.save
import org.jetbrains.kotlinx.kandy.letsplot.feature.layout
import java.io.File

fun detectSpikes(options: InputOptions) {
    if (!File(options.outputFile).isDirectory) {
        return println("Error > Output file must be a directory > ${options.outputFile} is not")
    }

    val deviation = when (options.deviation) {
        InputOptions.Deviation.FAKE -> List<Point>::calculateFakeSigmaDeviation
        InputOptions.Deviation.BINARY -> List<Point>::calculateSigmaViaBinarySearchDeviation
    }

    val deviationScalar = options.deviationScalar?.toDouble() ?: when (options.deviation) {
        InputOptions.Deviation.FAKE -> when (options.fitting) {
            InputOptions.Fitting.CONSTANT -> 12.0
            InputOptions.Fitting.LINEAR -> 14.0
        }
        InputOptions.Deviation.BINARY -> 4.0
    }

    val fitting = when (options.fitting) {
        InputOptions.Fitting.CONSTANT -> List<Point>::fitConstant
        InputOptions.Fitting.LINEAR -> List<Point>::fitLinear
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
            fitAndVisualize(data) { points ->
                points.fitNoise(fitting) { medianFitting  ->
                    deviation(points, medianFitting, deviationScalar)
                }
            }

            layout.size = 1920 to 1080
        }.save(options.outputFile / file.name + "$suffix.png")
    }

    println("Done!")
}

inline fun DataFramePlotContext<*>.fitAndVisualize(
    data: DataFile,
    fit: (List<Point>) -> NoiseFitting,
) {
    val colorProvider = RandomColorProvider.optimizedFor(3)
    visualizeLine(data.points, colorProvider.nextColor())
    val fitting = data.points.fitBySegmentsDynamically(fit = fit)
    visualize(fitting, colorProvider.nextColor(), colorProvider.nextColor(), colorProvider.nextColor())
    val spikes = fitting.extractSpikes(shift = false)
    visualizePoints(spikes, colorProvider.nextColor())
}
