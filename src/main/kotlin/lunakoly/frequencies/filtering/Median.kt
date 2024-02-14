package lunakoly.frequencies.filtering

import lunakoly.frequencies.data.Point
import lunakoly.frequencies.data.median
import lunakoly.frequencies.data.visualizeLine
import org.jetbrains.kotlinx.kandy.dsl.internal.LayerCollectorContext
import org.jetbrains.kotlinx.kandy.letsplot.layers.line
import org.jetbrains.kotlinx.kandy.util.color.Color
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

data class MedianFitting(
    val median: Double,
    val deviation: Double,
    val points: List<Point>,
)

fun List<Point>.fitMedian(): MedianFitting {
    require(size >= 3) { "The list contains too few points" }

    val median = median()
    // The following code attempts to treat a sample from a truncated normal distribution
    // as a normal distribution, thus using the usual formula for the sample
    // standard deviation. This is also why the coefficient is 12 instead of "3 sigma".
    // This was chosen just because it was simple and kinda seemed to work.
    val sortedSquaredDeviations = map { (_, y) -> (y - median.y).pow(2.0) }.sorted()
    val usefulDeviations = sortedSquaredDeviations.subList(0, sortedSquaredDeviations.size / 2)
    val deviation = sqrt(usefulDeviations.sum() / (usefulDeviations.size - 1))

    return MedianFitting(median.y, deviation * 12, this)
}

fun LayerCollectorContext.visualize(
    fitting: MedianFitting,
    graphColor: Color,
    medianColor: Color,
    deviationColor: Color,
) {
    visualizeLine(fitting.points, graphColor)
    val pointsX = fitting.points.map { it.x }

    line {
        x(pointsX)
        y(List(pointsX.size) { fitting.median })
        color = medianColor
    }

    line {
        x(pointsX)
        y(List(pointsX.size) { fitting.median + fitting.deviation })
        color = deviationColor
    }

    line {
        x(pointsX)
        y(List(pointsX.size) { fitting.median - fitting.deviation })
        color = deviationColor
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

fun LayerCollectorContext.visualizeFittings(
    fittedSegments: List<MedianFitting>,
    graphColor: Color,
    medianColor: Color,
    deviationColor: Color,
) {
    fittedSegments.forEach { visualize(it, graphColor, medianColor, deviationColor) }
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
        val oldDeviation = deviation

        fittedSegments = fitMedianBySegments(count)
        deviation = fittedSegments.averageDeviation()

        if (oldDeviation * (1 - threshold) < deviation) {
            break
        }

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
