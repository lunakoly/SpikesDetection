package lunakoly.spikesdetection.fitting

import lunakoly.spikesdetection.data.Point
import lunakoly.spikesdetection.data.visualizeLine
import lunakoly.spikesdetection.fitting.median.MedianFitting
import lunakoly.spikesdetection.util.withEach
import org.jetbrains.kotlinx.kandy.dsl.internal.LayerCollectorContext
import org.jetbrains.kotlinx.kandy.letsplot.layers.line
import org.jetbrains.kotlinx.kandy.util.color.Color
import kotlin.math.abs
import kotlin.math.sign

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

fun NoiseFitting.extractSpikes(shift: Boolean = false): List<Point> {
    return points.map { point ->
        val median = medianFitting.medianAt(point.x)
        val delta = point.y - median

        if (abs(delta) <= deviation) {
            Point(point.x, 0.0)
        } else if (shift) {
            Point(point.x, point.y - median * delta.sign)
        } else {
            point
        }
    }
}

fun List<NoiseFitting>.extractSpikes(shift: Boolean = false): List<Point> = flatMap { it.extractSpikes(shift) }

fun List<Point>.removeZeros(): List<Point> = filter { it.y != 0.0 }

fun LayerCollectorContext.visualize(
    fittedSegments: List<NoiseFitting>,
    graphColor: Color,
    medianColor: Color,
    deviationColor: Color,
) {
    fittedSegments.withEach { visualize(graphColor, medianColor, deviationColor) }
}
