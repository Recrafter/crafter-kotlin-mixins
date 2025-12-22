package io.github.recrafter.nametag.extensions.interop.java

import io.github.recrafter.nametag.extensions.interop.KJTypeName

typealias JAnnotationBuilder = com.palantir.javapoet.AnnotationSpec.Builder
typealias JAnnotation = com.palantir.javapoet.AnnotationSpec

typealias JFieldBuilder = com.palantir.javapoet.FieldSpec.Builder
typealias JField = com.palantir.javapoet.FieldSpec

typealias JParameterBuilder = com.palantir.javapoet.ParameterSpec.Builder
typealias JParameter = com.palantir.javapoet.ParameterSpec

typealias JMethodBuilder = com.palantir.javapoet.MethodSpec.Builder
typealias JMethod = com.palantir.javapoet.MethodSpec

typealias JTypeBuilder = com.palantir.javapoet.TypeSpec.Builder
typealias JType = com.palantir.javapoet.TypeSpec

typealias JFile = com.palantir.javapoet.JavaFile
typealias JFileBuilder = com.palantir.javapoet.JavaFile.Builder

typealias JTypeName = com.palantir.javapoet.TypeName
typealias JClassName = com.palantir.javapoet.ClassName

typealias JCodeBlock = com.palantir.javapoet.CodeBlock
typealias JCodeBlockBuilder = com.palantir.javapoet.CodeBlock.Builder

fun buildJavaField(type: JTypeName, name: String, builder: JFieldBuilder.() -> Unit = {}): JField =
    JField.builder(type, name).apply(builder).build()

fun buildJavaClass(name: String, builder: JTypeBuilder.() -> Unit = {}): JType =
    JType.classBuilder(name).apply(builder).build()

fun buildJavaInterface(name: String, builder: JTypeBuilder.() -> Unit = {}): JType =
    JType.interfaceBuilder(name).apply(builder).build()

fun buildJavaMethod(name: String, builder: JMethodBuilder.() -> Unit = {}): JMethod =
    JMethod.methodBuilder(name).apply(builder).build()

fun buildJavaParameter(type: KJTypeName, name: String, builder: JParameterBuilder.() -> Unit = {}): JParameter =
    JParameter.builder(type.javaVersion, name).apply(builder).build()

inline fun <reified A : Annotation> buildJavaAnnotation(builder: JAnnotationBuilder.() -> Unit = {}): JAnnotation =
    JAnnotation.builder(JClassName.get(A::class.java)).apply(builder).build()

fun buildJavaCodeBlock(builder: JCodeBlockBuilder.() -> Unit = {}): JCodeBlock =
    JCodeBlock.builder().apply(builder).build()

fun buildJavaCast(to: KJTypeName, from: JCodeBlock = JCodeBlock.of("this")): JCodeBlock =
    buildJavaCodeBlock {
        add("((")
        add("\$T", to.javaVersion)
        add(") ")
        add(from)
        add(")")
    }
