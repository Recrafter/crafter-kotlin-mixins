package io.github.recrafter.nametag.extensions.ksp

import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.ksp.toClassName
import io.github.recrafter.nametag.extensions.interop.KJTypeName
import io.github.recrafter.nametag.extensions.interop.kotlin.asKJTypeName

val KSClassDeclaration.isInterface: Boolean
    get() = classKind == ClassKind.INTERFACE

val KSClassDeclaration.isClass: Boolean
    get() = classKind == ClassKind.CLASS

val KSClassDeclaration.superInterfaceTypes: List<KSType>
    get() = superTypes
        .map { it.resolve() }
        .filter { type ->
            (type.declaration as? KSClassDeclaration)?.isInterface == true
        }
        .toList()

val KSClassDeclaration.constructorDeclarations: List<KSFunctionDeclaration>
    get() = declarations.filterIsInstance<KSFunctionDeclaration>().filter { it.isConstructor }.toList()

val KSClassDeclaration.propertyDeclarations: List<KSPropertyDeclaration>
    get() = declarations.filterIsInstance<KSPropertyDeclaration>().toList()

val KSClassDeclaration.functionDeclarations: List<KSFunctionDeclaration>
    get() = declarations.filterIsInstance<KSFunctionDeclaration>().filter { !it.isConstructor }.toList()

fun KSClassDeclaration.getSuperClassTypeOrNull(): KSType? =
    superTypes.map { it.resolve() }.firstOrNull { type ->
        (type.declaration as? KSClassDeclaration)?.isClass == true
    }

fun KSClassDeclaration.asKJTypeName(): KJTypeName =
    toClassName().asKJTypeName()
