package io.github.recrafter.nametag.extensions.common

fun <T> unsafeLazy(initializer: () -> T): Lazy<T> =
    lazy(LazyThreadSafetyMode.NONE, initializer)

inline fun <R> nullIfNot(condition: Boolean, block: () -> R): R? =
    if (condition) block()
    else null
