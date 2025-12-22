package io.github.recrafter.nametag.extensions.ksp

import com.google.devtools.ksp.symbol.KSValueParameter
import io.github.recrafter.nametag.extensions.interop.KJParameter
import io.github.recrafter.nametag.extensions.interop.KJParameterList

fun KSValueParameter.requireName(): String =
    requireNotNull(name?.asString()) {
        "Unnamed parameter is not supported in this context: $this."
    }

fun List<KSValueParameter>.asKJParameterList(): KJParameterList =
    KJParameterList(map { KJParameter(it.requireName(), it.type.asKJTypeName()) })
