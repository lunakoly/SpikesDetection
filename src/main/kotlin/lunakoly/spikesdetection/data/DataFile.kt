package lunakoly.spikesdetection.data

data class DataFile(
    val header: MutableMap<String, String>,
    val points: MutableList<Point>,
)

private fun String.getUnifyLineBreaks() = replace("\r", "")

private fun String.toNonBlankLines() = split("\n").filter { it.isNotBlank() }

fun parseDataFile(text: String): DataFile {
    val (headerLines, pointsLines) = text.getUnifyLineBreaks().split("\n\n\n\n").map { it.toNonBlankLines() }

    val header = headerLines.associateTo(mutableMapOf()) {
        val (name, value) = it.split(":").map { it.trim() }
        name to value
    }

    val points = pointsLines.mapTo(mutableListOf()) {
        val (name, value) = it.split("""\s+""".toRegex()).map { it.trim() }
        Point(name.toDouble(), value.toDouble())
    }

    return DataFile(header, points)
}

fun renderDataFile(data: DataFile): String {
    return buildString {
        data.header.forEach { (name, value) -> appendLine("$name: $value") }
        append("\n\n\n\n")
        data.points.forEach { (x, y) -> appendLine("$x $y") }
    }
}
