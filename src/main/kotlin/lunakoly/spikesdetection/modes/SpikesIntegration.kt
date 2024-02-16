package lunakoly.spikesdetection.modes

import lunakoly.spikesdetection.InputOptions
import lunakoly.spikesdetection.data.Point
import lunakoly.spikesdetection.data.visualizePoints
import lunakoly.spikesdetection.fitting.NoiseFitting
import lunakoly.spikesdetection.fitting.deviations.calculateFakeSigmaDeviation
import lunakoly.spikesdetection.fitting.deviations.calculateSigmaViaBinarySearchDeviation
import lunakoly.spikesdetection.fitting.extractSpikes
import lunakoly.spikesdetection.fitting.fitBySegmentsDynamically
import lunakoly.spikesdetection.fitting.fitNoise
import lunakoly.spikesdetection.fitting.median.MedianFitting
import lunakoly.spikesdetection.fitting.median.fitConstant
import lunakoly.spikesdetection.fitting.median.fitLinear
import lunakoly.spikesdetection.util.NameToColorMapper
import lunakoly.spikesdetection.util.RandomColorProvider
import lunakoly.spikesdetection.util.div
import org.jetbrains.kotlinx.kandy.dsl.plot
import org.jetbrains.kotlinx.kandy.letsplot.export.save
import java.io.File

fun integrateSpikes(options: InputOptions) {
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

    val (inputDirectories, inputFiles) = options.inputFiles.map(::File).partition { it.isDirectory }

    if (inputFiles.isNotEmpty()) {
        return println("Error > ${inputFiles.joinToString(", ") { "`$it`" }} - are not directories")
    }

    val colorProvider = RandomColorProvider.optimizedFor(6)
    val mapper = NameToColorMapper()

    plot {
        val resultingTable = mutableListOf(mutableListOf<String>())

        for (folder in inputDirectories) {
            val graph = integrate(folder) { points ->
                points.fitNoise(fitting) { medianFitting  ->
                    deviation(points, medianFitting)
                }
            }

            val graphName = folder.nameWrtPrefix(options.pathPrefix)
            visualizePoints(graph, colorProvider.nextColor(), mapper.assign(graphName))

            val previousColumnsCount = resultingTable.first().size
            val newRowsCount = graph.size - (resultingTable.size - 1)

            for (it in 0 until newRowsCount) {
                resultingTable += MutableList(previousColumnsCount) { "" }
            }

            resultingTable.first() += listOf(graphName, "")

            for (it in graph.indices) {
                resultingTable[it + 1] += listOf(graph[it].x.toString(), graph[it].y.toString())
            }

            for (it in graph.size until resultingTable.size - 1) {
                resultingTable[it + 1] += listOf("", "")
            }
        }

        File(options.outputFile / "integration-data.csv").writeText(
            resultingTable.joinToString("\n") { it.joinToString(",") }
        )
    }.save(options.outputFile / "integration.png")
}

inline fun integrate(folder: File, fit: (List<Point>) -> NoiseFitting): List<Point> {
    val inputData = folder.listFiles()?.toList().orEmpty().let(::parseDataFilesIn)
    val graph = mutableListOf<Point>()

    for ((file, data) in inputData) {
        val fitting = data.points.fitBySegmentsDynamically(fit = fit)
        val spikes = fitting.extractSpikes(shift = true)
        val integral = spikes.integrate()

        val current = file.nameWithoutExtension.replace(",", ".").toDouble()
        graph.add(Point(current, integral))
    }

    return graph.sortedBy { it.x }
}

fun List<Point>.integrate(): Double {
    if (size < 2) {
        return 0.0
    }

    var result = 0.0
    var previous = first()

    for (it in 1 until size) {
        val next = get(it)
        result += (previous.y + next.y) / 2.0 * (next.x - previous.x)
        previous = next
    }

    return result
}
