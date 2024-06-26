package lunakoly.spikesdetection.fitting.deviations

import lunakoly.spikesdetection.data.Point
import lunakoly.spikesdetection.fitting.median.MedianFitting
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.pow

fun List<Point>.buildNoiseGraph(
    medianFitting: MedianFitting,
    pointsCount: Int = 500,
    bellSigma: Double = 5.0,
): List<Point> {
    val absoluteDeviations = map { it.y - medianFitting.medianAt(it.x) }
    val minY = absoluteDeviations.min()
    val maxY = absoluteDeviations.max()
    val step = (maxY - minY) / pointsCount
    val result = MutableList(pointsCount) { Point(0.0, 0.0) }
    val bellSigmaSquared = bellSigma.pow(2)

    for (it in result.indices) {
        val y = minY + step * it

        for (bellPosition in absoluteDeviations) {
            result[it] = Point(y, result[it].y + exp(-(y - bellPosition).pow(2) / bellSigmaSquared))
        }
    }

    return result
}

fun List<Point>.calculateSigmaViaBinarySearchDeviation(
    medianFitting: MedianFitting,
    scalar: Double = 4.0,
    bellSigma: Double = 5.0,
): Double {
    // Originally this algorithm was supposed to be used to estimate std of
    // a truncated normal distribution received by throwing away the 50%
    // of the largest deviation points from the sample, thus making the
    // usual formula incorrect, but it turns out this algorithm performs
    // good even in the presence of spikes!

    val noise = buildNoiseGraph(medianFitting, bellSigma = bellSigma)
    val maximum = noise.maxOf { it.y }
    var sigmaUpperBound = maximum
    var sigmaLowerBound = 0.0
    var sigma = (sigmaUpperBound + sigmaLowerBound) / 2
    var oldError = Double.MAX_VALUE

    for (that in 0 until 100) {
        var error = 0.0

        for (it in noise.indices) {
            val predictedY = maximum * exp(-(noise[it].x).pow(2) / sigma.pow(2))
            val dataY = noise[it].y
            error += predictedY - dataY
        }

        when {
            error > 0 -> sigmaUpperBound = sigma
            error < 0 -> sigmaLowerBound = sigma
            else -> break
        }

        sigma = (sigmaUpperBound + sigmaLowerBound) / 2

        if (abs(error - oldError) < 0.001) {
            break
        }

        oldError = error
    }

    return sigma * scalar
}
