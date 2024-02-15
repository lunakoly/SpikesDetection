package lunakoly.frequencies.fitting.deviations

import lunakoly.frequencies.data.Point
import lunakoly.frequencies.fitting.median.MedianFitting
import kotlin.math.pow
import kotlin.math.sqrt

fun List<Point>.calculateFakeSigmaDeviation(medianFitting: MedianFitting, scalar: Double = 12.0): Double {
    // The following code attempts to treat a sample from a truncated normal distribution
    // as a normal distribution, thus using the usual formula for the sample
    // standard deviation. This is also why the coefficient bigger than just "4 sigma".
    // This was chosen just because it was simple and kinda seemed to work.
    val sortedSquaredDeviations = map { (x, y) -> (y - medianFitting.medianAt(x)).pow(2.0) }.sorted()
    val usefulDeviations = sortedSquaredDeviations.subList(0, sortedSquaredDeviations.size / 2)
    val deviation = sqrt(usefulDeviations.sum() / (usefulDeviations.size - 1))
    return deviation * scalar
}
