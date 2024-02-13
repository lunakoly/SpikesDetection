package lunakoly.frequencies

import org.jetbrains.kotlinx.kandy.util.color.Color
import kotlin.math.PI
import kotlin.math.abs

private data class Vec3<T>(val x: T, val y: T, val z: T) {
    constructor(value: T) : this(value, value, value)
}

private operator fun Vec3<Double>.plus(other: Vec3<Double>) = Vec3(x + other.x, y + other.y, z + other.z)
private operator fun Vec3<Double>.times(scalar: Double) = Vec3(x * scalar, y * scalar, z * scalar)

private fun Vec3<Double>.toInt() = Vec3(x.toInt(), y.toInt(), z.toInt())

private fun hslToRgb(h: Double, s: Double, l: Double): Vec3<Int> {
    // Source:
    // https://www.rapidtables.com/convert/color/hsl-to-rgb.html

    val c = (1 - abs(2 * l - 1)) * s
    val x = c * (1 - abs((h / 60.0) % 2 - 1))
    val m = l - c / 2
    val nonNormalized = when {
        0 <= h && h < 60 -> Vec3(c, x, 0.0)
        60 <= h && h < 120 -> Vec3(x, c, 0.0)
        120 <= h && h < 180 -> Vec3(0.0, c, x)
        180 <= h && h < 240 -> Vec3(0.0, x, c)
        240 <= h && h < 300 -> Vec3(x, 0.0, c)
        300 <= h && h < 360 -> Vec3(c, 0.0, x)
        else -> error("Incorrect hue value in: ($h, $s, $l)")
    }
    return ((nonNormalized + Vec3(m)) * 255.0).toInt()
}

private fun hsl(h: Double, s: Double = 1.0, l: Double = 0.5): Color {
    return hslToRgb(h, s, l).let { (r, g, b) -> Color.rgb(r, g, b) }
}

class RandomColorProvider {
    private var index = 0
    private val diameterStepAngle = 2 / PI * 180

    fun nextColor() = hsl((diameterStepAngle * index++).mod(360.0))
}
