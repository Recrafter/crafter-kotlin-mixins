package io.github.recrafter.nametag.extensions.ksp

import com.google.devtools.ksp.symbol.KSValueArgument

fun KSValueArgument.requireName(): String =
    requireNotNull(name?.asString()) {
        "Unnamed parameter is not supported in this context: $this."
    }
