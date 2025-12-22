package io.github.recrafter.nametag.extensions.interop.kotlin

import io.github.recrafter.nametag.extensions.interop.KJClassName
import io.github.recrafter.nametag.extensions.interop.KJTypeName

typealias KAnnotation = com.squareup.kotlinpoet.AnnotationSpec
typealias KAnnotationBuilder = com.squareup.kotlinpoet.AnnotationSpec.Builder

typealias KProperty = com.squareup.kotlinpoet.PropertySpec
typealias KPropertyBuilder = com.squareup.kotlinpoet.PropertySpec.Builder

typealias KParameter = com.squareup.kotlinpoet.ParameterSpec
typealias KParameterBuilder = com.squareup.kotlinpoet.ParameterSpec.Builder

typealias KFunction = com.squareup.kotlinpoet.FunSpec
typealias KFunctionBuilder = com.squareup.kotlinpoet.FunSpec.Builder

typealias KTypeAlias = com.squareup.kotlinpoet.TypeAliasSpec
typealias KTypeAliasBuilder = com.squareup.kotlinpoet.TypeAliasSpec.Builder

typealias KType = com.squareup.kotlinpoet.TypeSpec
typealias KTypeBuilder = com.squareup.kotlinpoet.TypeSpec.Builder

typealias KFile = com.squareup.kotlinpoet.FileSpec
typealias KFileBuilder = com.squareup.kotlinpoet.FileSpec.Builder

typealias KTypeName = com.squareup.kotlinpoet.TypeName
typealias KClassName = com.squareup.kotlinpoet.ClassName

typealias KCodeBlock = com.squareup.kotlinpoet.CodeBlock

fun buildKotlinConstructor(builder: KFunctionBuilder.() -> Unit = {}): KFunction =
    KFunction.constructorBuilder().apply(builder).build()

fun buildKotlinProperty(name: String, type: KJTypeName, builder: KPropertyBuilder.() -> Unit = {}): KProperty =
    KProperty.builder(name, type.kotlinVersion).apply(builder).build()

fun buildKotlinGetter(builder: KFunctionBuilder.() -> Unit = {}): KFunction =
    KFunction.getterBuilder().apply(builder).build()

fun buildKotlinSetter(builder: KFunctionBuilder.() -> Unit = {}): KFunction =
    KFunction.setterBuilder().apply(builder).build()

fun buildKotlinTypeAlias(
    name: String,
    type: KJTypeName,
    builder: KTypeAliasBuilder.() -> Unit = {}
): KTypeAlias =
    KTypeAlias.builder(name, type.kotlinVersion).apply(builder).build()

fun buildKotlinParameter(
    name: String,
    type: KJTypeName,
    builder: KParameterBuilder.() -> Unit = {}
): KParameter =
    KParameter.builder(name, type.kotlinVersion).apply(builder).build()

fun buildKotlinFunction(name: String, builder: KFunctionBuilder.() -> Unit = {}): KFunction =
    KFunction.builder(name).apply(builder).build()

fun buildKotlinInterface(name: String, builder: KTypeBuilder.() -> Unit = {}): KType =
    KType.interfaceBuilder(name).apply(builder).build()

fun buildKotlinClass(name: String, builder: KTypeBuilder.() -> Unit = {}): KType =
    KType.classBuilder(name).apply(builder).build()

fun buildKotlinObject(name: String, builder: KTypeBuilder.() -> Unit = {}): KType =
    KType.objectBuilder(name).apply(builder).build()

fun buildKotlinFile(packageName: String, name: String, builder: KFileBuilder.() -> Unit = {}): KFile =
    KFile.builder(packageName, name).apply(builder).indent("    ").build()

inline fun <reified A : Annotation> buildKotlinAnnotation(builder: KAnnotationBuilder.() -> Unit = {}): KAnnotation =
    KAnnotation.builder(A::class).apply(builder).build()

fun buildKotlinCast(from: KCodeBlock = KCodeBlock.of("this"), to: KJClassName): KCodeBlock =
    KCodeBlock.of("(%L as %T)", from, to.kotlinVersion)
