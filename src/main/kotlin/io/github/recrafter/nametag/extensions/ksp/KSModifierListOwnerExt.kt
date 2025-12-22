package io.github.recrafter.nametag.extensions.ksp

import com.google.devtools.ksp.symbol.KSModifierListOwner
import com.google.devtools.ksp.symbol.Modifier

val KSModifierListOwner.isPrivate: Boolean
    get() = modifiers.contains(Modifier.PRIVATE)

val KSModifierListOwner.isAbstract: Boolean
    get() = modifiers.contains(Modifier.ABSTRACT)
