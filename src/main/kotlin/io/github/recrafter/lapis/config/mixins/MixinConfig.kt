package io.github.recrafter.lapis.config.mixins

import io.github.recrafter.lapis.api.PatchSide
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MixinConfig(
    @SerialName("required")
    val isRequired: Boolean,

    val minVersion: String,

    @SerialName("mixinextras")
    val extrasConfig: MixinExtrasConfig,

    @SerialName("package")
    val packageName: String,

    @SerialName("compatibilityLevel")
    val jvmTargetVersion: String,

    @SerialName("injectors")
    val injectorConfig: InjectorConfig,

    @SerialName("overwrites")
    val overwriteConfig: OverwriteConfig,

    @SerialName("refmap")
    val refmapFileName: String,

    @SerialName("mixins")
    val commonMixins: List<String>? = null,

    @SerialName("client")
    val clientOnlyMixins: List<String>? = null,

    @SerialName("server")
    val dedicatedServerOnlyMixins: List<String>? = null,
) {
    companion object {
        fun of(
            packageName: String,
            javaVersion: Int,
            refmapFileName: String,
            mixinQualifiedNames: Map<PatchSide, List<String>>,
        ): MixinConfig =
            MixinConfig(
                isRequired = true,
                packageName = packageName,
                minVersion = "0.8.7",
                extrasConfig = MixinExtrasConfig(
                    minVersion = "0.5.3"
                ),
                jvmTargetVersion = "JAVA_$javaVersion",
                injectorConfig = InjectorConfig.newInstance(),
                overwriteConfig = OverwriteConfig.newInstance(),
                refmapFileName = refmapFileName,
                commonMixins = mixinQualifiedNames[PatchSide.Common]?.ifEmpty { null }?.map {
                    it.removePrefix("$packageName.")
                },
                clientOnlyMixins = mixinQualifiedNames[PatchSide.ClientOnly]?.ifEmpty { null }?.map {
                    it.removePrefix("$packageName.")
                },
                dedicatedServerOnlyMixins = mixinQualifiedNames[PatchSide.DedicatedServerOnly]?.ifEmpty { null }?.map {
                    it.removePrefix("$packageName.")
                },
            )
    }
}
