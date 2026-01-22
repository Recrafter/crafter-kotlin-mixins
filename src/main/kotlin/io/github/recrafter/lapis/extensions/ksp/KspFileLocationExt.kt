package io.github.recrafter.lapis.extensions.ksp

import java.io.File

val KspFileLocation.file: File
    get() = File(filePath)
