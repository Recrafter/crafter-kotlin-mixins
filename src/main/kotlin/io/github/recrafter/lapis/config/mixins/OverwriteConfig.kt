package io.github.recrafter.lapis.config.mixins

import kotlinx.serialization.Serializable

@Serializable
data class OverwriteConfig(
    val requireAnnotations: Boolean,
) {
    companion object {
        fun newInstance(): OverwriteConfig =
            OverwriteConfig(
                requireAnnotations = true,
            )
    }
}
