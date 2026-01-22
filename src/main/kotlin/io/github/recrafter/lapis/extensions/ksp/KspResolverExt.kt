package io.github.recrafter.lapis.extensions.ksp

inline fun <reified A : Annotation> KspResolver.forEachSymbolsAnnotatedWith(
    crossinline action: (
        symbol: KspAnnotated,
        annotation: A,
        ksAnnotation: KspAnnotation
    ) -> Unit
) {
    getSymbolsWithAnnotation(requireNotNull(A::class.qualifiedName)).forEach { symbol ->
        val annotation = requireNotNull(symbol.getSingleAnnotationOrNull<A>())
        val ksAnnotation = symbol.annotations.single { it.annotationType.qualifiedName == A::class.qualifiedName }
        action(symbol, annotation, ksAnnotation)
    }
}
