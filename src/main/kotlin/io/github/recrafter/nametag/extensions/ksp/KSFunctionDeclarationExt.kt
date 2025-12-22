package io.github.recrafter.nametag.extensions.ksp

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.UNIT
import io.github.recrafter.nametag.extensions.interop.KJTypeName
import io.github.recrafter.nametag.extensions.interop.kotlin.asKJTypeName

val KSFunctionDeclaration.isConstructor: Boolean
    get() = name == "<init>"

fun KSFunctionDeclaration.getReturnTypeOrNull(): KJTypeName? =
    returnType?.asKJTypeName().takeIf { it != UNIT.asKJTypeName() }
