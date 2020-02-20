package dev.dnihze.revorate.common

inline fun <T: Any, I> T.applyEach(items: Iterable<I>, func: T.(I) -> Unit): T {
    items.forEach { item ->
        func(item)
    }
    return this
}