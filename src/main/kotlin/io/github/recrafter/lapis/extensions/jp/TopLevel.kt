package io.github.recrafter.lapis.extensions.jp

import io.github.recrafter.lapis.kj.KJTypeName

typealias JPAnnotationBuilder = com.palantir.javapoet.AnnotationSpec.Builder
typealias JPAnnotation = com.palantir.javapoet.AnnotationSpec

typealias JPFieldBuilder = com.palantir.javapoet.FieldSpec.Builder
typealias JPField = com.palantir.javapoet.FieldSpec

typealias JPParameterBuilder = com.palantir.javapoet.ParameterSpec.Builder
typealias JPParameter = com.palantir.javapoet.ParameterSpec

typealias JPMethodBuilder = com.palantir.javapoet.MethodSpec.Builder
typealias JPMethod = com.palantir.javapoet.MethodSpec

typealias JPTypeBuilder = com.palantir.javapoet.TypeSpec.Builder
typealias JPType = com.palantir.javapoet.TypeSpec

typealias JPFile = com.palantir.javapoet.JavaFile
typealias JPFileBuilder = com.palantir.javapoet.JavaFile.Builder

typealias JPTypeName = com.palantir.javapoet.TypeName
typealias JPClassName = com.palantir.javapoet.ClassName
typealias JPParameterizedTypeName = com.palantir.javapoet.ParameterizedTypeName

typealias JPCodeBlock = com.palantir.javapoet.CodeBlock
typealias JPCodeBlockBuilder = com.palantir.javapoet.CodeBlock.Builder

val JPObject: JPTypeName = JPClassName.OBJECT
val JPVoid: JPTypeName = JPClassName.VOID
val JPBoolean: JPTypeName = JPTypeName.BOOLEAN
val JPByte: JPTypeName = JPTypeName.BYTE
val JPShort: JPTypeName = JPTypeName.SHORT
val JPInt: JPTypeName = JPTypeName.INT
val JPLong: JPTypeName = JPTypeName.LONG
val JPChar: JPTypeName = JPTypeName.CHAR
val JPFloat: JPTypeName = JPTypeName.FLOAT
val JPDouble: JPTypeName = JPTypeName.DOUBLE
val JPString: JPTypeName = JPClassName.get("java.lang", "String")
val JPList: JPTypeName = JPClassName.get("java.util", "List")
val JPSet: JPTypeName = JPClassName.get("java.util", "Set")
val JPMap: JPTypeName = JPClassName.get("java.util", "Map")

fun buildJavaField(type: JPTypeName, name: String, builder: JPFieldBuilder.() -> Unit = {}): JPField =
    JPField.builder(type, name).apply(builder).build()

fun buildJavaClass(name: String, builder: JPTypeBuilder.() -> Unit = {}): JPType =
    JPType.classBuilder(name).apply(builder).build()

fun buildJavaInterface(name: String, builder: JPTypeBuilder.() -> Unit = {}): JPType =
    JPType.interfaceBuilder(name).apply(builder).build()

fun buildJavaMethod(name: String, builder: JPMethodBuilder.() -> Unit = {}): JPMethod =
    JPMethod.methodBuilder(name).apply(builder).build()

fun buildJavaParameter(type: JPTypeName, name: String, builder: JPParameterBuilder.() -> Unit = {}): JPParameter =
    JPParameter.builder(type, name).apply(builder).build()

fun buildJavaParameter(type: KJTypeName, name: String, builder: JPParameterBuilder.() -> Unit = {}): JPParameter =
    buildJavaParameter(type.javaVersion, name, builder)

inline fun <reified A : Annotation> buildJavaAnnotation(builder: JPAnnotationBuilder.() -> Unit = {}): JPAnnotation =
    JPAnnotation.builder(JPClassName.get(A::class.java)).apply(builder).build()

fun buildJavaCodeBlock(builder: JPCodeBlockBuilder.() -> Unit = {}): JPCodeBlock =
    JPCodeBlock.builder().apply(builder).build()

fun buildJavaCast(to: JPTypeName, from: JPCodeBlock = JPCodeBlock.of("this")): JPCodeBlock =
    buildJavaCodeBlock {
        add("((")
        add("\$T", to)
        add(") ")
        add(from)
        add(")")
    }
