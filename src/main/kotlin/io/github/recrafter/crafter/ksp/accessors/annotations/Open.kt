package io.github.recrafter.crafter.ksp.accessors.annotations

/**
 * Declares a property or function in a [KAccessor] interface as an accessor member.
 *
 * When used on a property, the KSP processor generates a Mixin `@Accessor`
 * for the specified [target] field.
 * When used on a function, the processor generates a Mixin `@Invoker`
 * for the specified [target] method.
 *
 * @property target The name of the target field or method in the target class.
 */
@Target(
    AnnotationTarget.PROPERTY,
    AnnotationTarget.FUNCTION,
)
@Retention(AnnotationRetention.BINARY)
annotation class Open(
    val target: String = "",
    val isStatic: Boolean = false,
)
