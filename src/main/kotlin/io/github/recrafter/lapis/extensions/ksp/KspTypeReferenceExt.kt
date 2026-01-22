package io.github.recrafter.lapis.extensions.ksp

import io.github.recrafter.lapis.kj.KJTypeName

val KspTypeReference.qualifiedName: String
    get() = resolve().declaration.requireQualifiedName()

fun KspTypeReference.asKJTypeName(): KJTypeName =
    resolve().asKJTypeName()
