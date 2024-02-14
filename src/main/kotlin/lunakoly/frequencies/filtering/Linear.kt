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

class LinearFitting(
    private val medianLineA: Double,
    private val medianLineB: Double,
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
        val pointsY = List(pointsX.size) { medianAt(pointsX[it]) }

        line {
            x(pointsX)
            y(pointsY)
            color = medianColor
        }

        line {
            x(pointsX)
            y(pointsY.map { it + deviation })
            color = deviationColor
        }

        line {
            x(pointsX)
            y(pointsY.map { it - deviation })
            color = deviationColor
        }
    }

    override fun extractSpikes() = points.filter { (x, y) -> abs(y - medianAt(x)) > deviation }

    private fun LinearFitting.medianAt(x: Double) = medianLineA + medianLineB * x
}

fun List<Point>.fitLinear(): LinearFitting {
    require(size >= 3) { "The list contains too few points" }

    // Source:
    // https://statproofbook.github.io/P/slr-ols.html

    val median = median()
    val sortedAbsoluteDeviations = map { (x, y) -> Point(x - median.x, y - median.y) }.sortedBy { abs(it.y) }
    val usefulAbsoluteDeviations = sortedAbsoluteDeviations.subList(0, sortedAbsoluteDeviations.size / 2)
    val medianLineB = usefulAbsoluteDeviations.sumOf { it.x * it.y } / usefulAbsoluteDeviations.sumOf { it.x * it.x }
    val medianLineA = median.y - medianLineB * median.x

    // The following code attempts to treat a sample from a truncated normal distribution
    // as a normal distribution, thus using the usual formula for the sample
    // standard deviation. This is also why the coefficient is 12 instead of "3 sigma".
    // This was chosen just because it was simple and kinda seemed to work.
    val sortedSquaredDeviations = map { (x, y) -> (y - medianLineA - medianLineB * x).pow(2.0) }.sorted()
    val usefulDeviations = sortedSquaredDeviations.subList(0, sortedSquaredDeviations.size / 2)
    val deviation = sqrt(usefulDeviations.sum() / (usefulDeviations.size - 1))

    return LinearFitting(medianLineA, medianLineB, deviation * 12, this)
}
