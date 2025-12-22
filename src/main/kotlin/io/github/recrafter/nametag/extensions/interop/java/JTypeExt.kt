package io.github.recrafter.nametag.extensions.interop.java

inline fun <reified A : Annotation> JTypeBuilder.addAnnotation(builder: JAnnotationBuilder.() -> Unit = {}) {
    addAnnotation(buildJavaAnnotation<A>(builder))
}

fun JType.toJavaFile(packageName: String, builder: JFileBuilder.() -> Unit = {}): JFile =
    JFile.builder(packageName, this).apply(builder).indent("    ").skipJavaLangImports(true).build()
