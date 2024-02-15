package lunakoly.frequencies.fitting.median

import lunakoly.frequencies.data.Point
import lunakoly.frequencies.data.median
import lunakoly.frequencies.fitting.NoiseFitting
import lunakoly.frequencies.fitting.fitNoise
import kotlin.math.abs

class LinearFitting(
    private val medianLineA: Double,
    private val medianLineB: Double,
) : MedianFitting() {
    override fun medianAt(x: Double) = medianLineA + medianLineB * x
}

fun List<Point>.fitLinear(calculateDeviation: (MedianFitting) -> Double): NoiseFitting =
    fitNoise(calculateDeviation) {
        // Source:
        // https://statproofbook.github.io/P/slr-ols.html

        val median = median()
        val sortedAbsoluteDeviations = map { (x, y) -> Point(x - median.x, y - median.y) }.sortedBy { abs(it.y) }
        val usefulAbsoluteDeviations = sortedAbsoluteDeviations.subList(0, sortedAbsoluteDeviations.size / 2)
        val medianLineB = usefulAbsoluteDeviations.sumOf { it.x * it.y } / usefulAbsoluteDeviations.sumOf { it.x * it.x }
        val medianLineA = median.y - medianLineB * median.x

        LinearFitting(medianLineA, medianLineB)
    }
