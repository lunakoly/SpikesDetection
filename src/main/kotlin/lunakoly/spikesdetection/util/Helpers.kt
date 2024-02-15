package lunakoly.spikesdetection.util

inline fun <T> Iterable<T>.withEach(action: T.() -> Unit): Unit = forEach { with(it, action) }