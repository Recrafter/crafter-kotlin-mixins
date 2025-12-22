package io.github.recrafter.nametag.extensions

fun <T> MutableCollection<T>.addIfNotNull(element: T?) {
    element?.let { add(it) }
}
