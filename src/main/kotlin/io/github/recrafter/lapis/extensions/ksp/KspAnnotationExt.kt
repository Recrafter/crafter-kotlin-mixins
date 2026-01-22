package io.github.recrafter.lapis.extensions.ksp

import org.jetbrains.kotlin.utils.addToStdlib.UnsafeCastFunction
import org.jetbrains.kotlin.utils.addToStdlib.safeAs

@OptIn(UnsafeCastFunction::class)
fun KspAnnotation.getKspClassArgumentOrNull(name: String): KspClass? =
    arguments
        .firstOrNull { it.requireName() == name }
        ?.value
        ?.safeAs<KspType>()
        ?.declaration
        ?.safeAs<KspClass>()

fun KspAnnotation.getKspClassArgument(name: String): KspClass =
    requireNotNull(getKspClassArgumentOrNull(name)) {
        "Class declaration argument '$name' not found"
    }
