package io.github.recrafter.nametag.annotations.unlockers

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class UnlockConstructor(val target: String = "<init>")
