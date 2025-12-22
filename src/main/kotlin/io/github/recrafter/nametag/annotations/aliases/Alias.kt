package io.github.recrafter.nametag.annotations.aliases

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Alias(
    val target: KClass<*>,
    val typeAlias: String = ""
)
