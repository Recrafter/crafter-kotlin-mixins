package io.github.recrafter.nametag.extensions.interop.java

fun JTypeName.boxIfPrimitive(extraCondition: Boolean = true): JTypeName =
    if (extraCondition && isPrimitive && !isBoxedPrimitive) box()
    else this
