package io.github.recrafter.lapis.extensions.ksp

import io.github.recrafter.lapis.kj.KJParameter
import io.github.recrafter.lapis.kj.KJParameterList

fun KspValueParameter.requireName(): String =
    requireNotNull(name?.asString()) {
        "Unnamed parameter is not supported in this context: $this."
    }

fun List<KspValueParameter>.asKJParameterList(): KJParameterList =
    KJParameterList(map { KJParameter(it.requireName(), it.type.asKJTypeName()) })
