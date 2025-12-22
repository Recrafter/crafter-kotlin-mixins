package io.github.recrafter.nametag.extensions.ksp

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSType
import io.github.recrafter.nametag.extensions.interop.KJTypeName

inline fun <reified A : Annotation> Resolver.forEachSymbolsAnnotatedWith(
    crossinline action: (symbol: KSAnnotated, annotation: A, arguments: Map<String, KJTypeName>) -> Unit
) {
    getSymbolsWithAnnotation(requireNotNull(A::class.qualifiedName)).forEach { symbol ->
        val annotation = requireNotNull(symbol.getSingleAnnotationOrNull<A>())
        val arguments = symbol.annotations
            .single { it.annotationType.qualifiedName == A::class.qualifiedName }
            .arguments
            .mapNotNull { argument ->
                val ksType = argument.value as? KSType ?: return@mapNotNull null
                argument.requireName() to ksType.asKJTypeName()
            }.toMap()
        action(symbol, annotation, arguments)
    }
}
