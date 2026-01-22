package io.github.recrafter.lapis.extensions.ksp

import com.google.devtools.ksp.symbol.ClassKind
import com.squareup.kotlinpoet.ksp.toClassName
import io.github.recrafter.lapis.extensions.kp.asKJClassName
import io.github.recrafter.lapis.kj.KJClassName

val KspClass.isClass: Boolean
    get() = classKind == ClassKind.CLASS

val KspClass.properties: List<KspProperty>
    get() = declarations.filterIsInstance<KspProperty>().toList()

val KspClass.functions: List<KspFunction>
    get() = declarations.filterIsInstance<KspFunction>().filter { !it.isConstructor }.toList()

fun KspClass.getSuperClassTypeOrNull(): KspType? =
    superTypes.map { it.resolve() }.firstOrNull { type ->
        (type.declaration as? KspClass)?.isClass == true
    }

fun KspClass.asKJClassName(): KJClassName =
    toClassName().asKJClassName()
