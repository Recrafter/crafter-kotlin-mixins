package io.github.recrafter.nametag.extensions.interop.java

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

fun JFile.writeTo(codeGenerator: CodeGenerator, dependencies: Dependencies) {
    val file = codeGenerator.createNewFile(dependencies, packageName(), typeSpec().name(), "java")
    OutputStreamWriter(file, StandardCharsets.UTF_8).use { writeTo(it) }
}
