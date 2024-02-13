package lunakoly.frequencies.data

import lunakoly.frequencies.RandomColorProvider
import org.jetbrains.kotlinx.kandy.dsl.internal.LayerCollectorContext
import org.jetbrains.kotlinx.kandy.letsplot.layers.line
import org.jetbrains.kotlinx.kandy.letsplot.layers.points

data class Point(val x: Double, val y: Double)

fun List<Point>.median(): Point {
    require(isNotEmpty()) { "Empty list of points has no median" }
    return sortedBy { it.y }[size / 2]
}

fun List<Double>.median(): Double {
    require(isNotEmpty()) { "Empty list of points has no median" }
    return sorted()[size / 2]
}

fun LayerCollectorContext.visualizePoints(points: List<Point>, colorProvider: RandomColorProvider) {
    points {
        x(points.map { it.x })
        y(points.map { it.y })
        color = colorProvider.nextColor()
    }
}

fun LayerCollectorContext.visualizeLine(points: List<Point>, colorProvider: RandomColorProvider) {
    line {
        x(points.map { it.x })
        y(points.map { it.y })
        color = colorProvider.nextColor()
    }
}