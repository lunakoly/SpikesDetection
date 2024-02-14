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

class MedianFitting(
    private val median: Double,
    deviation: Double,
    private val points: List<Point>,
) : Fitting(deviation) {
    override fun LayerCollectorContext.visualize(
        graphColor: Color,
        medianColor: Color,
        deviationColor: Color,
    ) {
        visualizeLine(points, graphColor)
        val pointsX = points.map { it.x }

        line {
            x(pointsX)
            y(List(pointsX.size) { median })
            color = medianColor
        }

        line {
            x(pointsX)
            y(List(pointsX.size) { median + deviation })
            color = deviationColor
        }

        line {
            x(pointsX)
            y(List(pointsX.size) { median - deviation })
            color = deviationColor
        }
    }

    override fun extractSpikes() = points.filter { abs(it.y - median) > deviation }
}

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
