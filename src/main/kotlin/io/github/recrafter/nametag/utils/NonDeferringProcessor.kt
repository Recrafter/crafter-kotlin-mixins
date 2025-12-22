package io.github.recrafter.nametag.utils

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated

abstract class NonDeferringProcessor : SymbolProcessor {

    abstract fun run(resolver: Resolver)

    final override fun process(resolver: Resolver): List<KSAnnotated> {
        run(resolver)
        return emptyList()
    }
}
