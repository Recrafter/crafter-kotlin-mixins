package io.github.recrafter.nametag.extensions.ksp

import com.google.devtools.ksp.containingFile
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSNode

fun KSNode.toDependencies(aggregating: Boolean = false): Dependencies =
    containingFile?.let { Dependencies(aggregating, it) } ?: Dependencies(aggregating)
