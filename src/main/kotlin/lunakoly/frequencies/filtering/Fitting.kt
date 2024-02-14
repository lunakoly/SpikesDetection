package lunakoly.frequencies.filtering

import lunakoly.frequencies.data.Point
import org.jetbrains.kotlinx.kandy.dsl.internal.LayerCollectorContext
import org.jetbrains.kotlinx.kandy.util.color.Color

abstract class Fitting(val deviation: Double) {
    abstract fun LayerCollectorContext.visualize(graphColor: Color, medianColor: Color, deviationColor: Color)

    abstract fun extractSpikes(): List<Point>
}

inline fun <T> Iterable<T>.withEach(action: T.() -> Unit): Unit = forEach { with(it, action) }

fun LayerCollectorContext.visualize(
    fittedSegments: List<Fitting>,
    graphColor: Color,
    medianColor: Color,
    deviationColor: Color,
) {
    fittedSegments.withEach { visualize(graphColor, medianColor, deviationColor) }
}

fun List<Fitting>.extractSpikes(): List<Point> = flatMap { it.extractSpikes() }
