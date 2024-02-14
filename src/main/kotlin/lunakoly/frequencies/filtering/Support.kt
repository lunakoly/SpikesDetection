package lunakoly.frequencies.filtering

import lunakoly.frequencies.data.Point

fun <T> List<T>.splitByEqualSegments(count: Int): List<List<T>> {
    val result = mutableListOf<MutableList<T>>()
    val segmentSize = size / count

    for (it in 0 until count) {
        val segment = subList(it * segmentSize, (it + 1) * segmentSize)
        result.add(segment.toMutableList())
    }

    if (segmentSize * count < size) {
        val segment = subList(segmentSize * count, size)
        result.last().addAll(segment)
    }

    return result
}

fun List<Fitting>.averageDeviation(): Double {
    require(isNotEmpty()) { "The list contains too few points" }
    return sumOf { it.deviation } / size
}

inline fun <F : Fitting> List<Point>.fitBySegmentsDynamically(
    threshold: Double = 0.05,
    fit: (List<Point>) -> F,
): List<F> {
    var fittedSegments = splitByEqualSegments(1).map(fit)
    var deviation = fittedSegments.averageDeviation()
    var count = 2

    while (count < 20) {
        val oldDeviation = deviation

        fittedSegments = splitByEqualSegments(count).map(fit)
        deviation = fittedSegments.averageDeviation()

        if (oldDeviation * (1 - threshold) < deviation) {
            break
        }

        count++
    }

    return fittedSegments
}
