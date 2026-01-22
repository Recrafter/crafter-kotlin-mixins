package io.github.recrafter.lapis.utils

import com.google.devtools.ksp.processing.SymbolProcessor
import io.github.recrafter.lapis.extensions.ksp.KspAnnotated
import io.github.recrafter.lapis.extensions.ksp.KspResolver

abstract class NonDeferringProcessor : SymbolProcessor {

    abstract fun run(resolver: KspResolver)

    final override fun process(resolver: KspResolver): List<KspAnnotated> {
        run(resolver)
        return emptyList()
    }
}
