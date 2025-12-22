package io.github.recrafter.nametag.extensions.interop

import io.github.recrafter.nametag.extensions.common.unsafeLazy
import io.github.recrafter.nametag.extensions.interop.java.JTypeName
import io.github.recrafter.nametag.extensions.interop.kotlin.KClassName
import io.github.recrafter.nametag.extensions.interop.kotlin.KTypeName
import io.github.recrafter.nametag.extensions.interop.kotlin.toJavaType

class KJTypeName(val kotlinVersion: KTypeName) {

    val className: KJClassName? by lazy {
        when (kotlinVersion) {
            is KClassName -> KJClassName(kotlinVersion.packageName, kotlinVersion.simpleName)
            else -> null
        }
    }

    val name: String by unsafeLazy {
        when (kotlinVersion) {
            is KClassName -> kotlinVersion.simpleName
            else -> error("Unsupported type: $kotlinVersion")
        }
    }

    val javaVersion: JTypeName by unsafeLazy {
        when (kotlinVersion) {
            is KClassName -> kotlinVersion.toJavaType()
            else -> error("Unsupported type: $kotlinVersion")
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is KJTypeName) {
            return false
        }
        return kotlinVersion == other.kotlinVersion
    }

    override fun hashCode(): Int =
        kotlinVersion.hashCode()
}
