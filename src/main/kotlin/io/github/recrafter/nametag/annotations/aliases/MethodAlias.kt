package io.github.recrafter.nametag.annotations.aliases

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class MethodAlias(val originalName: String)
