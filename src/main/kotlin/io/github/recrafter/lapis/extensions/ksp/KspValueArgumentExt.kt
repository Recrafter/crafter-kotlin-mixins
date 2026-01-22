package io.github.recrafter.lapis.extensions.ksp

fun KspValueArgument.requireName(): String =
    requireNotNull(name?.asString()) {
        "Unnamed parameter is not supported in this context: $this."
    }
