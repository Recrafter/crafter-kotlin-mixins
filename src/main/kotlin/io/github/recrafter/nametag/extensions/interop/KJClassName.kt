package io.github.recrafter.nametag.extensions.interop

import io.github.recrafter.nametag.extensions.common.unsafeLazy
import io.github.recrafter.nametag.extensions.interop.java.JClassName
import io.github.recrafter.nametag.extensions.interop.java.JCodeBlock
import io.github.recrafter.nametag.extensions.interop.kotlin.KClassName
import io.github.recrafter.nametag.extensions.interop.kotlin.KCodeBlock

class KJClassName(val packageName: String, val name: String) {

    val kotlinVersion: KClassName by unsafeLazy {
        KClassName(packageName, name)
    }

    val javaVersion: JClassName by unsafeLazy {
        JClassName.get(packageName, name)
    }

    val kotlinCodeBlock: KCodeBlock by unsafeLazy {
        KCodeBlock.of("%T", kotlinVersion)
    }

    val javaCodeBlock: JCodeBlock by unsafeLazy {
        JCodeBlock.of("\$T", kotlinVersion)
    }

    val typeName: KJTypeName by unsafeLazy {
        KJTypeName(kotlinVersion)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is KJClassName) {
            return false
        }
        return kotlinVersion == other.kotlinVersion
    }

    override fun hashCode(): Int =
        kotlinVersion.hashCode()
}
