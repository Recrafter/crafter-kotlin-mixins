package io.github.recrafter.nametag.extensions.ksp

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import java.io.File

fun CodeGenerator.createResourceFile(
    path: String,
    contents: String,
    aggregating: Boolean = false,
) {
    val file = File(path)
    createNewFile(
        dependencies = Dependencies(aggregating),
        packageName = file.parentFile?.invariantSeparatorsPath?.replace(File.separatorChar, '.').orEmpty(),
        fileName = file.nameWithoutExtension,
        extensionName = file.extension
    ).writer().use { it.write(contents) }
}
