package io.github.recrafter.nametag.extensions.interop.kotlin

fun KAnnotationBuilder.addStringArrayMember(name: String, strings: List<String>) {
    val format = buildString {
        if (strings.isNotEmpty()) {
            append(strings.joinToString { "%S" })
        }
    }
    addMember(format, *strings.toTypedArray())
}
