package io.github.recrafter.nametag.extensions.interop.kotlin

import io.github.recrafter.nametag.extensions.interop.KJTypeName

fun KPropertyBuilder.setReceiverType(type: KJTypeName) {
    receiver(type.kotlinVersion)
}
