package io.github.recrafter.nametag.annotations.patches

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Patcher(val target: KClass<*>)
