package io.github.recrafter.nametag.extensions.ksp

import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.ksp.toTypeName
import io.github.recrafter.nametag.extensions.interop.KJTypeName
import io.github.recrafter.nametag.extensions.interop.kotlin.asKJTypeName

fun KSType.genericTypes(): List<KJTypeName?> =
    arguments.map { it.type?.asKJTypeName() }

fun KSType.asKJTypeName(): KJTypeName =
    toTypeName().asKJTypeName()
