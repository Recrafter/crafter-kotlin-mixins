package io.github.recrafter.nametag.extensions.ksp

import com.google.devtools.ksp.symbol.KSTypeReference
import io.github.recrafter.nametag.extensions.interop.KJTypeName

val KSTypeReference.qualifiedName: String
    get() = resolve().declaration.requireQualifiedName()

fun KSTypeReference.asKJTypeName(): KJTypeName =
    resolve().asKJTypeName()
