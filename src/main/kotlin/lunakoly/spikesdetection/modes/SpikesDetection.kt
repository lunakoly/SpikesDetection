package lunakoly.spikesdetection.modes

import lunakoly.spikesdetection.InputOptions
import lunakoly.spikesdetection.data.*
import lunakoly.spikesdetection.fitting.*
import lunakoly.spikesdetection.fitting.deviations.calculateFakeSigmaDeviation
import lunakoly.spikesdetection.fitting.deviations.calculateSigmaViaBinarySearchDeviation
import lunakoly.spikesdetection.fitting.median.MedianFitting
import lunakoly.spikesdetection.fitting.median.fitConstant
import lunakoly.spikesdetection.fitting.median.fitLinear
import lunakoly.spikesdetection.util.RandomColorProvider
import org.jetbrains.kotlinx.kandy.dsl.internal.DataFramePlotContext
import java.io.File

fun detectSpikes(options: InputOptions) {
    if (!File(options.outputFile).isDirectory) {
        return println("Error > Output file must be a directory > ${options.outputFile} is not")
    }

    val deviationScalar = options.deviationScalar?.toDouble() ?: when (options.deviation) {
        InputOptions.Deviation.FAKE -> when (options.fitting) {
            InputOptions.Fitting.CONSTANT -> 12.0
            InputOptions.Fitting.LINEAR -> 14.0
        }
        InputOptions.Deviation.BINARY -> 4.0
    }

    val bellSigma = options.bellSigma.toDouble()

    val deviation: List<Point>.(MedianFitting) -> Double = when (options.deviation) {
        InputOptions.Deviation.FAKE -> { fitting -> calculateFakeSigmaDeviation(fitting, deviationScalar) }
        InputOptions.Deviation.BINARY -> { fitting -> calculateSigmaViaBinarySearchDeviation(fitting, deviationScalar, bellSigma) }
    }

    val fitting = when (options.fitting) {
        InputOptions.Fitting.CONSTANT -> List<Point>::fitConstant
        InputOptions.Fitting.LINEAR -> List<Point>::fitLinear
    }

    transformFilesToOutput(options.inputFiles, options.outputFile, options.pathPrefix) { data ->
        fitAndVisualize(data) { points ->
            points.fitNoise(fitting) { medianFitting  ->
                deviation(points, medianFitting)
            }
        }
    }
}

inline fun DataFramePlotContext<*>.fitAndVisualize(
    data: DataFile,
    fit: (List<Point>) -> NoiseFitting,
) {
    val colorProvider = RandomColorProvider.optimizedFor(5)
    visualizeLine(data.points, colorProvider.nextColor())
    val fitting = data.points.fitBySegmentsDynamically(fit = fit)
    visualize(fitting, colorProvider.nextColor(), colorProvider.nextColor(), colorProvider.nextColor())
    val spikes = fitting.extractSpikes(shift = false)
    visualizePoints(spikes, colorProvider.nextColor())
}
