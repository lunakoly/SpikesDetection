package lunakoly.spikesdetection.modes

import lunakoly.spikesdetection.InputOptions
import lunakoly.spikesdetection.data.*
import lunakoly.spikesdetection.fitting.deviations.buildNoiseGraph
import lunakoly.spikesdetection.fitting.deviations.calculateFakeSigmaDeviation
import lunakoly.spikesdetection.fitting.deviations.calculateSigmaViaBinarySearchDeviation
import lunakoly.spikesdetection.fitting.median.*
import lunakoly.spikesdetection.util.NameToColorMapper
import lunakoly.spikesdetection.util.RandomColorProvider
import lunakoly.spikesdetection.util.div
import org.jetbrains.kotlinx.kandy.dsl.internal.DataFramePlotContext
import org.jetbrains.kotlinx.kandy.dsl.plot
import org.jetbrains.kotlinx.kandy.letsplot.export.save
import org.jetbrains.kotlinx.kandy.letsplot.feature.layout
import java.io.File
import kotlin.math.exp
import kotlin.math.pow
import kotlin.reflect.KFunction1

fun visualizeNoise(options: InputOptions) {
    if (!File(options.outputFile).isDirectory) {
        return println("Error > Output file must be a directory > ${options.outputFile} is not")
    }

    if (options.deviationScalar != null) {
        return println("Error > Specifying the deviation scalar is not supported in `${options.mode}` mode")
    }

    val bellSigma = options.bellSigma.toDouble()

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
            fitAndVisualizeNoise(data, fitting, bellSigma)
            layout.size = 1920 to 1080
        }.save(options.outputFile / file.name + "$suffix.png")
    }

    println("Done!")
}

fun DataFramePlotContext<*>.fitAndVisualizeNoise(
    data: DataFile,
    fitting: KFunction1<List<Point>, MedianFitting>,
    bellSigma: Double,
) {
    val colorProvider = RandomColorProvider.optimizedFor(4)
    val mapper = NameToColorMapper()

    val medianFitting = fitting(data.points)
    val noiseGraph = data.points.buildNoiseGraph(medianFitting, bellSigma = bellSigma)
    visualizeLine(noiseGraph, colorProvider.nextColor(), mapper.assign("Noise Approximation"))

    val realPoints = data.points.map { (x, y) -> Point(y - medianFitting.medianAt(x), 0.0) }
    visualizePoints(realPoints, colorProvider.nextColor(), mapper.assign("Real points"))

    val fakeSigma = data.points.calculateFakeSigmaDeviation(medianFitting, scalar = 4.0)
    val fakeSigmaGraph = buildNormalDistributionGraphOnTopOf(noiseGraph, fakeSigma)
    visualizeLine(fakeSigmaGraph, colorProvider.nextColor(), mapper.assign("Fake sigma"))

    val binarySigma = data.points.calculateSigmaViaBinarySearchDeviation(medianFitting, scalar = 1.0)
    val binarySigmaGraph = buildNormalDistributionGraphOnTopOf(noiseGraph, binarySigma)
    visualizeLine(binarySigmaGraph, colorProvider.nextColor(), mapper.assign("Sigma via binary search"))
}

fun buildNormalDistributionGraphOnTopOf(noiseGraph: List<Point>, sigma: Double): List<Point> {
    val minX = noiseGraph.minOf { it.x }
    val maxX = noiseGraph.maxOf { it.x }
    val maxY = noiseGraph.maxOf { it.y }
    return buildNormalDistributionGraph(0.0, sigma, minX, maxX, maxY, minY = 0.0, pointsCount = noiseGraph.size)
}

private fun buildNormalDistributionGraph(
    mean: Double, sigma: Double,
    minX: Double, maxX: Double,
    maxY: Double, minY: Double,
    pointsCount: Int = 500,
): List<Point> {
    val step = (maxX - minX) / pointsCount
    val stretchY = maxY - minY

    return List(pointsCount) {
        val x = minX + it * step
        Point(x, stretchY * exp(-(x - mean).pow(2) / sigma.pow(2)) + minY)
    }
}
