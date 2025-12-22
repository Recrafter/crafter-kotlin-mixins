package io.github.recrafter.nametag.annotations.aliases

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class FieldAlias(val originalName: String)
