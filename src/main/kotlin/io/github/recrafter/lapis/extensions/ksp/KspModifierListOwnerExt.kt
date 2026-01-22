package io.github.recrafter.lapis.extensions.ksp

import com.google.devtools.ksp.symbol.Modifier

val KspModifierListOwner.isPublic: Boolean
    get() = !isPrivate && !isProtected && !isInternal

val KspModifierListOwner.isPrivate: Boolean
    get() = modifiers.contains(Modifier.PRIVATE)

val KspModifierListOwner.isInternal: Boolean
    get() = modifiers.contains(Modifier.INTERNAL)

val KspModifierListOwner.isProtected: Boolean
    get() = modifiers.contains(Modifier.PROTECTED)

val KspModifierListOwner.isAbstract: Boolean
    get() = modifiers.contains(Modifier.ABSTRACT)
