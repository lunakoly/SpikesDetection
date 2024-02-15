package lunakoly.spikesdetection.data

import org.jetbrains.kotlinx.kandy.dsl.internal.LayerCollectorContext
import org.jetbrains.kotlinx.kandy.letsplot.layers.line
import org.jetbrains.kotlinx.kandy.letsplot.layers.points
import org.jetbrains.kotlinx.kandy.util.color.Color

data class Point(val x: Double, val y: Double)

fun List<Point>.median(): Point {
    require(isNotEmpty()) { "Empty list of points has no median" }
    return sortedBy { it.y }[size / 2]
}

fun LayerCollectorContext.visualizePoints(points: List<Point>, color: Color) {
    points {
        x(points.map { it.x })
        y(points.map { it.y })
        this.color = color
    }
}

fun LayerCollectorContext.visualizeLine(points: List<Point>, color: Color) {
    line {
        x(points.map { it.x })
        y(points.map { it.y })
        this.color = color
    }
}