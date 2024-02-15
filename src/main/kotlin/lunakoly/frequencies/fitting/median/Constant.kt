package lunakoly.frequencies.fitting.median

import lunakoly.frequencies.data.Point
import lunakoly.frequencies.data.median
import lunakoly.frequencies.fitting.NoiseFitting
import lunakoly.frequencies.fitting.fitNoise

class ConstantFitting(private val median: Double) : MedianFitting() {
    override fun medianAt(x: Double) = median
}

fun List<Point>.fitConstant(calculateDeviation: (MedianFitting) -> Double): NoiseFitting =
        fitNoise(calculateDeviation) { ConstantFitting(median().y) }
