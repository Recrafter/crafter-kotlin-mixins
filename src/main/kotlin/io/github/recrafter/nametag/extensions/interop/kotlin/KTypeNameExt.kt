package io.github.recrafter.nametag.extensions.interop.kotlin

import io.github.recrafter.nametag.extensions.interop.KJTypeName

fun KTypeName.asKJTypeName(): KJTypeName =
    KJTypeName(this)
