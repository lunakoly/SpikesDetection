package lunakoly.spikesdetection.util

import java.io.File

inline fun <T> Iterable<T>.withEach(action: T.() -> Unit): Unit = forEach { with(it, action) }

operator fun String.div(other: String) = "$this${File.separator}$other"
