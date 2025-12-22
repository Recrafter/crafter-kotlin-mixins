package io.github.recrafter.nametag.extensions.interop.kotlin

import io.github.recrafter.nametag.extensions.interop.KJTypeName

fun KType.requireName(): String =
    requireNotNull(name) {
        "Unnamed type is not supported in this context: $this."
    }

fun KType.toKotlinFile(packageName: String, builder: KFileBuilder.() -> Unit = {}): KFile =
    buildKotlinFile(packageName, requireName()) fileBuilder@{
        builder()
        addType(this@toKotlinFile)
    }

fun KTypeBuilder.setConstructor(vararg parameters: Pair<String, KJTypeName>) {
    primaryConstructor(buildKotlinConstructor {
        addParameters(*parameters)
    })
}

fun KTypeBuilder.setSuperClassType(type: KJTypeName) {
    superclass(type.kotlinVersion)
}
