package io.github.recrafter.lapis.extensions.kp

import io.github.recrafter.lapis.extensions.ksp.KspCodeGenerator
import io.github.recrafter.lapis.extensions.ksp.KspDependencies

inline fun <reified A : Annotation> KPFileBuilder.addAnnotation(builder: KPAnnotationBuilder.() -> Unit = {}) {
    addAnnotation(buildKotlinAnnotation<A>(builder))
}

fun KPFile.writeTo(codeGenerator: KspCodeGenerator, dependencies: KspDependencies) {
    codeGenerator.createNewFile(dependencies, packageName, name).writer().use { writeTo(it) }
}
