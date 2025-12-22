package io.github.recrafter.nametag.annotations.unlockers

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Unlocker(
    val target: KClass<*>,
    val widener: String = ""
)
