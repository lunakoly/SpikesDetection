package lunakoly.frequencies.fitting

import lunakoly.frequencies.data.Point
import lunakoly.frequencies.data.visualizeLine
import lunakoly.frequencies.fitting.median.MedianFitting
import lunakoly.frequencies.util.withEach
import org.jetbrains.kotlinx.kandy.dsl.internal.LayerCollectorContext
import org.jetbrains.kotlinx.kandy.letsplot.layers.line
import org.jetbrains.kotlinx.kandy.util.color.Color
import kotlin.math.abs

class NoiseFitting(
    val medianFitting: MedianFitting,
    val deviation: Double,
    val points: List<Point>,
) {
    fun LayerCollectorContext.visualize(
        graphColor: Color,
        medianColor: Color,
        deviationColor: Color,
    ) {
        visualizeLine(points, graphColor)
        val pointsX = points.map { it.x }

        line {
            x(pointsX)
            y(pointsX.map { medianFitting.medianAt(it) })
            color = medianColor
        }

        line {
            x(pointsX)
            y(List(pointsX.size) { medianFitting.medianAt(pointsX[it]) + deviation })
            color = deviationColor
        }

        line {
            x(pointsX)
            y(List(pointsX.size) { medianFitting.medianAt(pointsX[it]) - deviation })
            color = deviationColor
        }
    }
}

fun NoiseFitting.extractSpikes() = points.filter { (x, y) -> abs(y - medianFitting.medianAt(x)) > deviation }

fun List<NoiseFitting>.extractSpikes(): List<Point> = flatMap { it.extractSpikes() }

fun LayerCollectorContext.visualize(
    fittedSegments: List<NoiseFitting>,
    graphColor: Color,
    medianColor: Color,
    deviationColor: Color,
) {
    fittedSegments.withEach { visualize(graphColor, medianColor, deviationColor) }
}
