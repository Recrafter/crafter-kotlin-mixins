package io.github.recrafter.nametag.extensions.ksp

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.containingFile
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSAnnotated

@OptIn(KspExperimental::class)
inline fun <reified A : Annotation> KSAnnotated.hasAnnotation(): Boolean =
    isAnnotationPresent(A::class)

@OptIn(KspExperimental::class)
inline fun <reified A : Annotation> KSAnnotated.getSingleAnnotationOrNull(): A? =
    getAnnotationsByType(A::class).singleOrNull()

fun Iterable<KSAnnotated>.toDependencies(aggregating: Boolean = false): Dependencies {
    val containingFiles = mapNotNull { it.containingFile }
    return if (containingFiles.isNotEmpty()) {
        Dependencies(aggregating, *containingFiles.toTypedArray())
    } else {
        Dependencies(aggregating)
    }
}
