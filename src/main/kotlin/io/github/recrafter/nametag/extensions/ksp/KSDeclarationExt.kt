package io.github.recrafter.nametag.extensions.ksp

import com.google.devtools.ksp.symbol.KSDeclaration

val KSDeclaration.name: String
    get() = simpleName.asString()

fun KSDeclaration.requireQualifiedName(): String =
    requireNotNull(qualifiedName).asString()

inline fun <reified T> KSDeclaration.isInstance(): Boolean =
    requireQualifiedName() == T::class.qualifiedName
