package io.github.recrafter.lapis.extensions.ksp

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
inline fun KspLogger.require(condition: Boolean, symbol: KspNode, crossinline message: () -> String) {
    contract {
        returns() implies condition
    }
    if (!condition) {
        error(symbol, message)
    }
}

inline fun KspLogger.error(symbol: KspNode, crossinline message: () -> String): Nothing {
    val message = message()
    error(message, symbol)
    throw IllegalStateException(message)
}
