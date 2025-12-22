package io.github.recrafter.nametag.extensions.interop

import io.github.recrafter.nametag.extensions.common.unsafeLazy
import io.github.recrafter.nametag.extensions.interop.java.JParameter
import io.github.recrafter.nametag.extensions.interop.java.buildJavaParameter
import io.github.recrafter.nametag.extensions.interop.kotlin.KParameter
import io.github.recrafter.nametag.extensions.interop.kotlin.buildKotlinParameter

class KJParameterList(val parameters: List<KJParameter>) {

    val names: List<String> by unsafeLazy {
        parameters.map { it.name }
    }

    val kotlinVersion: List<KParameter> by unsafeLazy {
        parameters.map { buildKotlinParameter(it.name, it.type) }
    }

    val javaVersion: List<JParameter> by unsafeLazy {
        parameters.map {
            buildJavaParameter(it.type, it.name) {

            }
        }
    }
}

class KJParameter(val name: String, val type: KJTypeName)
