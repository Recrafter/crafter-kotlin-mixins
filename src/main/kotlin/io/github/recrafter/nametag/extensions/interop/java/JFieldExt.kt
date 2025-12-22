package io.github.recrafter.nametag.extensions.interop.java

inline fun <reified A : Annotation> JFieldBuilder.addAnnotation(builder: JAnnotationBuilder.() -> Unit = {}) {
    addAnnotation(buildJavaAnnotation<A>(builder))
}
