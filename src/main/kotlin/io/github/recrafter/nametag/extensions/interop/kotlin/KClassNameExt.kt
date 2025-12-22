package io.github.recrafter.nametag.extensions.interop.kotlin

import io.github.recrafter.nametag.extensions.interop.java.JClassName
import io.github.recrafter.nametag.extensions.interop.java.JTypeName
import io.github.recrafter.nametag.extensions.interop.java.boxIfPrimitive

fun KClassName.toJavaType(shouldBox: Boolean = false): JTypeName =
    when (copy(nullable = false)) {
        com.squareup.kotlinpoet.BOOLEAN -> JTypeName.BOOLEAN.boxIfPrimitive(shouldBox || isNullable)
        com.squareup.kotlinpoet.BYTE -> JTypeName.BYTE.boxIfPrimitive(shouldBox || isNullable)
        com.squareup.kotlinpoet.CHAR -> JTypeName.CHAR.boxIfPrimitive(shouldBox || isNullable)
        com.squareup.kotlinpoet.SHORT -> JTypeName.SHORT.boxIfPrimitive(shouldBox || isNullable)
        com.squareup.kotlinpoet.INT -> JTypeName.INT.boxIfPrimitive(shouldBox || isNullable)
        com.squareup.kotlinpoet.LONG -> JTypeName.LONG.boxIfPrimitive(shouldBox || isNullable)
        com.squareup.kotlinpoet.FLOAT -> JTypeName.FLOAT.boxIfPrimitive(shouldBox || isNullable)
        com.squareup.kotlinpoet.DOUBLE -> JTypeName.DOUBLE.boxIfPrimitive(shouldBox || isNullable)
        com.squareup.kotlinpoet.UNIT -> JTypeName.VOID

        com.squareup.kotlinpoet.STRING -> JClassName.get("java.lang", "String")
        com.squareup.kotlinpoet.ANY -> JClassName.get("java.lang", "Object")

        com.squareup.kotlinpoet.LIST -> JClassName.get("java.util", "List")
        com.squareup.kotlinpoet.SET -> JClassName.get("java.util", "Set")
        com.squareup.kotlinpoet.MAP -> JClassName.get("java.util", "Map")
        else -> {
            if (simpleNames.size == 1) {
                JClassName.get(packageName, simpleName)
            } else {
                JClassName.get(
                    packageName,
                    simpleNames.first(),
                    *simpleNames.drop(1).toTypedArray()
                )
            }
        }
    }
