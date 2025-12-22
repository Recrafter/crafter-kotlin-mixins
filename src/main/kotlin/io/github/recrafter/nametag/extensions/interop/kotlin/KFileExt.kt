package io.github.recrafter.nametag.extensions.interop.kotlin

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies

inline fun <reified A : Annotation> KFileBuilder.addAnnotation(builder: KAnnotationBuilder.() -> Unit = {}) {
    addAnnotation(buildKotlinAnnotation<A>(builder))
}

fun KFile.writeTo(codeGenerator: CodeGenerator, dependencies: Dependencies) {
    codeGenerator.createNewFile(dependencies, packageName, name).writer().use { writeTo(it) }
}
