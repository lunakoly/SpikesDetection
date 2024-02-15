package lunakoly.spikesdetection.fitting.median

import lunakoly.spikesdetection.data.Point
import lunakoly.spikesdetection.data.median

class ConstantFitting(private val median: Double) : MedianFitting() {
    override fun medianAt(x: Double) = median
}

fun List<Point>.fitConstant(): ConstantFitting = ConstantFitting(median().y)
