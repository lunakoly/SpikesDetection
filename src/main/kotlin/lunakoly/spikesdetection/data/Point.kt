package lunakoly.spikesdetection.data

import lunakoly.spikesdetection.util.ColorName
import org.jetbrains.kotlinx.kandy.dsl.internal.LayerCollectorContext
import org.jetbrains.kotlinx.kandy.letsplot.layers.line
import org.jetbrains.kotlinx.kandy.letsplot.layers.points
import org.jetbrains.kotlinx.kandy.util.color.Color

data class Point(val x: Double, val y: Double)

fun List<Point>.median(): Point {
    require(isNotEmpty()) { "Empty list of points has no median" }
    return sortedBy { it.y }[size / 2]
}

fun LayerCollectorContext.visualizePoints(points: List<Point>, color: Color, name: ColorName? = null) {
    points {
        x(points.map { it.x })
        y(points.map { it.y })
        name?.configureFor(points, color, this) ?: run { this.color = color }
    }
}

fun LayerCollectorContext.visualizeLine(points: List<Point>, color: Color, name: ColorName? = null) {
    line {
        x(points.map { it.x })
        y(points.map { it.y })
        name?.configureFor(points, color, this) ?: run { this.color = color }
    }
}
