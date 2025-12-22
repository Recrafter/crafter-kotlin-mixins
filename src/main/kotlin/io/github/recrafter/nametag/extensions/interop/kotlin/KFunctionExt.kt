package io.github.recrafter.nametag.extensions.interop.kotlin

import com.squareup.kotlinpoet.UNIT
import io.github.recrafter.nametag.extensions.interop.KJClassName
import io.github.recrafter.nametag.extensions.interop.KJTypeName

fun KFunctionBuilder.addInvokeFunctionStatement(
    isReturn: Boolean,
    receiver: KCodeBlock? = null,
    functionName: String,
    parameterNames: List<String> = emptyList(),
) {
    val args = mutableListOf<Any>()
    val format = buildString {
        if (isReturn) {
            append("return ")
        }
        if (receiver != null) {
            append("%L.")
            args += receiver
        }
        append("%L(")
        args += functionName

        append(parameterNames.joinToString())
        append(")")
    }
    addStatement(format, *args.toTypedArray())
}

fun KFunctionBuilder.addInvokeFunctionStatement(
    isReturn: Boolean,
    receiver: KJClassName,
    functionName: String,
    parameterNames: List<String> = emptyList(),
) {
    addInvokeFunctionStatement(isReturn, receiver.kotlinCodeBlock, functionName, parameterNames)
}

fun KFunctionBuilder.addGetterStatement(propertyName: String, receiver: KCodeBlock = KCodeBlock.of("this")) {
    addStatement("return %L.%L", receiver, propertyName)
}

fun KFunctionBuilder.addGetterStatement(receiver: KJClassName, propertyName: String) {
    addGetterStatement(propertyName, receiver.kotlinCodeBlock)
}

fun KFunctionBuilder.addSetterStatement(
    propertyName: String,
    propertyValue: String,
    receiver: KCodeBlock = KCodeBlock.of("this"),
) {
    addStatement("%L.%L = %L", receiver, propertyName, propertyValue)
}

fun KFunctionBuilder.addSetterStatement(receiver: KJClassName, propertyName: String, propertyValue: String) {
    addSetterStatement(propertyName, propertyValue, receiver.kotlinCodeBlock)
}

fun KFunctionBuilder.setReturnType(type: KJTypeName?) {
    returns(type?.kotlinVersion ?: UNIT)
}

fun KFunctionBuilder.setReceiverType(type: KJTypeName) {
    receiver(type.kotlinVersion)
}

fun KFunctionBuilder.setParameters(vararg parameters: Pair<String, KJTypeName>) {
    clearParameters()
    addParameters(*parameters)
}

fun KFunctionBuilder.addParameters(vararg parameters: Pair<String, KJTypeName>) {
    parameters.forEach { (name, type) ->
        addParameter(name, type.kotlinVersion)
    }
}

fun KFunctionBuilder.setParameters(parameters: List<KParameter>) {
    clearParameters()
    addParameters(parameters)
}

private fun KFunctionBuilder.clearParameters() {
    parameters.clear()
}
