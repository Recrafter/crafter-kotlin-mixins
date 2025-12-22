package io.github.recrafter.nametag.annotations.unlockers

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class UnlockMethod(
    val target: String = "",
    val isStatic: Boolean = false,
)
