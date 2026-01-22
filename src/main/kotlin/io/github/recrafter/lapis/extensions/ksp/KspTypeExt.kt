package io.github.recrafter.lapis.extensions.ksp

import com.squareup.kotlinpoet.ksp.toTypeName
import io.github.recrafter.lapis.extensions.kp.asKJTypeName
import io.github.recrafter.lapis.kj.KJTypeName

fun KspType.genericTypes(): List<KspType> =
    arguments.map { requireNotNull(it.type).resolve() }

fun KspType.asKJTypeName(): KJTypeName =
    toTypeName().asKJTypeName()
