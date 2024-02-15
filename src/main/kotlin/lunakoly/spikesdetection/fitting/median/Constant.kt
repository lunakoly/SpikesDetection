package lunakoly.spikesdetection.fitting.median

import lunakoly.spikesdetection.data.Point
import lunakoly.spikesdetection.data.median
import lunakoly.spikesdetection.fitting.NoiseFitting
import lunakoly.spikesdetection.fitting.fitNoise

class ConstantFitting(private val median: Double) : MedianFitting() {
    override fun medianAt(x: Double) = median
}

fun List<Point>.fitConstant(calculateDeviation: (MedianFitting) -> Double): NoiseFitting =
        fitNoise(calculateDeviation) { ConstantFitting(median().y) }
