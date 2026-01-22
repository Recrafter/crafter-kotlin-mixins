package io.github.recrafter.lapis.extensions.common

import org.jetbrains.kotlin.utils.addToStdlib.UnsafeCastFunction
import org.jetbrains.kotlin.utils.addToStdlib.safeAs
import kotlin.reflect.KCallable

fun <T> unsafeLazy(initializer: () -> T): Lazy<T> =
    lazy(LazyThreadSafetyMode.NONE, initializer)

inline fun <R> nullIfNot(condition: Boolean, block: () -> R): R? =
    if (condition) block()
    else null

@OptIn(UnsafeCastFunction::class)
inline fun <reified F : Function<*>> nameOfCallable(function: () -> F): String =
    function().safeAs<KCallable<*>>()?.name ?: error("Expected a callable reference.")
