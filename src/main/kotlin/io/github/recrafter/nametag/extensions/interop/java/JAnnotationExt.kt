package io.github.recrafter.nametag.extensions.interop.java

import io.github.recrafter.nametag.extensions.interop.KJTypeName

fun JAnnotationBuilder.addStringMember(name: String, value: String) {
    addMember(name, "\$S", value)
}

fun JAnnotationBuilder.addClassMember(name: String, type: KJTypeName) {
    addMember(name, "\$T.class", type.javaVersion)
}

fun JAnnotationBuilder.addClassArrayMember(name: String, vararg types: KJTypeName) {
    val javaTypes = types.map { it.javaVersion }.toTypedArray()
    val format = buildString {
        append("{")
        if (javaTypes.isNotEmpty()) {
            append(javaTypes.joinToString { "\$T.class" })
        }
        append("}")
    }
    addMember(name, format, *javaTypes)
}
