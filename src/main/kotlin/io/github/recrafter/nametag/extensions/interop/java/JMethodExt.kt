package io.github.recrafter.nametag.extensions.interop.java

import io.github.recrafter.nametag.extensions.interop.KJClassName
import io.github.recrafter.nametag.extensions.interop.KJTypeName

inline fun <reified A : Annotation> JMethodBuilder.addAnnotation(builder: JAnnotationBuilder.() -> Unit = {}) {
    addAnnotation(buildJavaAnnotation<A>(builder))
}

fun JMethodBuilder.addStubStatement() {
    addStatement("throw new ${AssertionError::class.simpleName}()")
}

fun JMethodBuilder.addIfStatement(condition: JCodeBlock, body: JMethodBuilder.() -> Unit) {
    withControlFlow(
        buildJavaCodeBlock {
            add("if (")
            add(condition)
            add(")")
        },
        body
    )
}

fun JMethodBuilder.withControlFlow(controlFlow: JCodeBlock, body: JMethodBuilder.() -> Unit) {
    beginControlFlow(controlFlow)
    apply(body)
    endControlFlow()
}

fun JMethodBuilder.addReturnStatement(value: String) {
    addStatement("return $value")
}

fun JMethodBuilder.addInvokeFunctionStatement(
    isReturn: Boolean,
    receiver: JCodeBlock? = null,
    functionName: String,
    parameterNames: List<String> = emptyList(),
) {
    val args = mutableListOf<Any>()
    val format = buildString {
        if (isReturn) {
            append("return ")
        }
        if (receiver != null) {
            append("\$L.")
            args += receiver
        }
        append("\$L")
        args += functionName

        append("(")
        append(parameterNames.joinToString())
        append(")")
    }
    addStatement(format, *args.toTypedArray())
}

fun JMethodBuilder.addInvokeFunctionStatement(
    isReturn: Boolean,
    receiver: KJClassName,
    functionName: String,
    parameterNames: List<String> = emptyList(),
) {
    addInvokeFunctionStatement(isReturn, receiver.javaCodeBlock, functionName, parameterNames)
}

fun JMethodBuilder.setReturnType(type: KJTypeName?) {
    setReturnType(type?.javaVersion)
}

fun JMethodBuilder.setReturnType(type: JTypeName?) {
    returns(type ?: JTypeName.VOID)
}

fun JMethodBuilder.setParameters(parameters: List<JParameter>) {
    require(build().parameters().isEmpty()) {
        "Parameters are already set. Use setParameters() only once."
    }
    addParameters(parameters)
}

fun JMethodBuilder.setParameters(vararg parameters: Pair<KJTypeName, String>) {
    require(build().parameters().isEmpty()) {
        "Parameters are already set. Use setParameters() only once."
    }
    parameters.forEach { (type, name) ->
        addParameter(type.javaVersion, name)
    }
}
