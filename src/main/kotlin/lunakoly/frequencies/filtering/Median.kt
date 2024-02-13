package lunakoly.frequencies.filtering

import lunakoly.frequencies.data.Point
import lunakoly.frequencies.RandomColorProvider
import lunakoly.frequencies.data.median
import lunakoly.frequencies.data.visualizeLine
import org.jetbrains.kotlinx.kandy.dsl.internal.LayerCollectorContext
import org.jetbrains.kotlinx.kandy.letsplot.layers.line
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

data class MedianFitting(
    val median: Double,
    val deviation: Double,
    val points: List<Point>,
)

fun List<Point>.fitMedian(): MedianFitting {
    require(size >= 2) { "The list contains too few points" }

    val median = median()
//    val deviation = sqrt(sumOf { (_, y) -> (y - median.y).pow(2.0) } / (size - 1))
    val deviation = sqrt(map { (_, y) -> (y - median.y).pow(2.0) }.median())

    return MedianFitting(median.y, deviation * 6, this)
}

fun LayerCollectorContext.visualize(fitting: MedianFitting, colorProvider: RandomColorProvider) {
    visualizeLine(fitting.points, colorProvider)
    val pointsX = fitting.points.map { it.x }

    line {
        x(pointsX)
        y(List(pointsX.size) { fitting.median })
        color = colorProvider.nextColor()
    }

    val deviationBarsColor = colorProvider.nextColor()

    line {
        x(pointsX)
        y(List(pointsX.size) { fitting.median + fitting.deviation })
        color = deviationBarsColor
    }

    line {
        x(pointsX)
        y(List(pointsX.size) { fitting.median - fitting.deviation })
        color = deviationBarsColor
    }
}

fun <T> List<T>.splitByEqualSegments(count: Int): List<List<T>> {
    val result = mutableListOf<MutableList<T>>()
    val segmentSize = size / count

    for (it in 0 until count) {
        val segment = subList(it * segmentSize, (it + 1) * segmentSize)
        result.add(segment.toMutableList())
    }

    if (segmentSize * count < size) {
        val segment = subList(segmentSize * count, size)
        result.last().addAll(segment)
    }

    return result
}

fun List<Point>.fitMedianBySegments(count: Int): List<MedianFitting> = splitByEqualSegments(count).map { it.fitMedian() }

fun LayerCollectorContext.visualizeFittings(fittedSegments: List<MedianFitting>, colorProvider: RandomColorProvider) {
    fittedSegments.forEach { visualize(it, colorProvider) }
}

fun List<MedianFitting>.averageDeviation(): Double {
    require(isNotEmpty()) { "The list contains too few points" }
    return sumOf { it.deviation } / size
}

fun List<Point>.fitMedianBySegmentsDynamically(threshold: Double = 0.05): List<MedianFitting> {
    var fittedSegments = fitMedianBySegments(1)
    var deviation = fittedSegments.averageDeviation()
    var count = 2

    while (count < 20) {
        val nextFittedSegments = fitMedianBySegments(count)
        val nextDeviation = nextFittedSegments.averageDeviation()

        if (deviation * (1 - threshold) < nextDeviation) {
            break
        }

        fittedSegments = nextFittedSegments
        deviation = nextDeviation
        count++
    }

    return fittedSegments
}

fun List<MedianFitting>.extractSpikes(): List<Point> {
    val result = mutableListOf<Point>()

    for (fitting in this) {
        result += fitting.points.filter { abs(it.y - fitting.median) > fitting.deviation }
    }

    return result
}
