package io.github.recrafter.nametag.annotations.unlockers

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class UnlockField(
    val target: String = "",
    val isStatic: Boolean = false,
)
