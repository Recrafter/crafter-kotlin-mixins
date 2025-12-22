package io.github.recrafter.nametag

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.KModifier
import io.github.recrafter.nametag.annotations.aliases.Alias
import io.github.recrafter.nametag.annotations.aliases.FieldAlias
import io.github.recrafter.nametag.annotations.aliases.MethodAlias
import io.github.recrafter.nametag.annotations.patches.Hook
import io.github.recrafter.nametag.annotations.patches.Patcher
import io.github.recrafter.nametag.annotations.unlockers.UnlockConstructor
import io.github.recrafter.nametag.annotations.unlockers.UnlockField
import io.github.recrafter.nametag.annotations.unlockers.UnlockMethod
import io.github.recrafter.nametag.annotations.unlockers.Unlocker
import io.github.recrafter.nametag.api.patches.Patch
import io.github.recrafter.nametag.extensions.addIfNotNull
import io.github.recrafter.nametag.extensions.atName
import io.github.recrafter.nametag.extensions.capitalized
import io.github.recrafter.nametag.extensions.common.nullIfNot
import io.github.recrafter.nametag.extensions.interop.KJClassName
import io.github.recrafter.nametag.extensions.interop.KJTypeName
import io.github.recrafter.nametag.extensions.interop.java.*
import io.github.recrafter.nametag.extensions.interop.kotlin.*
import io.github.recrafter.nametag.extensions.ksp.*
import io.github.recrafter.nametag.utils.NonDeferringProcessor
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Mutable
import org.spongepowered.asm.mixin.Unique
import org.spongepowered.asm.mixin.gen.Accessor
import org.spongepowered.asm.mixin.gen.Invoker
import org.spongepowered.asm.mixin.injection.Inject
import javax.lang.model.element.Modifier
import kotlin.reflect.KClass

internal class NametagProcessor(
    arguments: Map<String, String>,
    private val generator: CodeGenerator,
    private val logger: KSPLogger,
) : NonDeferringProcessor() {

    private val modId: String = arguments["modId"]
        ?: error("Argument 'nametag.modId' was not provided.")

    private val unlockers: MutableMap<KJClassName, GeneratedUnlocker> = mutableMapOf()
    private val patches: MutableMap<KJClassName, GeneratedPatch> = mutableMapOf()

    private val extensions: MutableMap<KJClassName, GeneratedExtension> = mutableMapOf()
    private val factories: MutableMap<KJClassName, GeneratedFactory> = mutableMapOf()

    private val wideners: MutableSet<String> = mutableSetOf()

    override fun run(resolver: Resolver) {
        resolver.forEachSymbolsAnnotatedWith<Alias> { symbol, annotation, arguments ->
            logger.kspRequire(symbol is KSClassDeclaration && symbol.isInterface, symbol) {
                "Annotation ${Alias::class.atName} can only be applied to interfaces."
            }
            logger.kspRequire(symbol.parentDeclaration == null, symbol) {
                "Interface annotated with ${Alias::class.atName} must be root."
            }
            val targetTypeName = arguments.getValue(Alias::target.name)
            val targetClassName = targetTypeName.className
            logger.kspRequire(targetClassName != null, symbol) {
                "${Alias::class.atName} target must be a class reference."
            }
            val extensionProperties = mutableListOf<KProperty>()
            val extensionFunctions = mutableListOf<KFunction>()
            symbol.declarations.forEach { declaration ->
                when {
                    declaration is KSPropertyDeclaration -> {
                        val property = declaration
                        val fieldAliasAnnotation = property.getSingleAnnotationOrNull<FieldAlias>()
                        logger.kspRequire(fieldAliasAnnotation != null, property) {
                            "Properties inside ${Alias::class.atName} interfaces " +
                                    "must be annotated with ${FieldAlias::class.atName}."
                        }
                        logger.kspRequire(property.getter?.isAbstract == true, property) {
                            "Properties inside ${Alias::class.atName} interfaces must not declare a getter."
                        }
                        val propertyTypeName = property.type.asKJTypeName()
                        val originalName = fieldAliasAnnotation.originalName
                        val aliasName = property.name
                        extensionProperties += buildKotlinProperty(aliasName, propertyTypeName) {
                            setReceiverType(targetTypeName)
                            getter(buildKotlinGetter {
                                addModifiers(KModifier.INLINE)
                                addGetterStatement(property.name)
                            })
                            if (property.isMutable) {
                                mutable(true)
                                setter(buildKotlinSetter {
                                    addModifiers(KModifier.INLINE)
                                    setParameters(SETTER_ARGUMENT_NAME to propertyTypeName)
                                    addSetterStatement(originalName, SETTER_ARGUMENT_NAME)
                                })
                            }
                        }
                    }

                    declaration is KSFunctionDeclaration -> {
                        val function = declaration
                        val methodAliasAnnotation = function.getSingleAnnotationOrNull<MethodAlias>()
                        logger.kspRequire(methodAliasAnnotation != null, function) {
                            "Functions inside ${Alias::class.atName} interfaces " +
                                    "must be annotated with ${MethodAlias::class.atName}."
                        }
                        logger.kspRequire(function.isAbstract, function) {
                            "Functions inside ${Alias::class.atName} interfaces must not have a body."
                        }
                        val parameterList = function.parameters.asKJParameterList()
                        val returnType = function.getReturnTypeOrNull()
                        val originalName = methodAliasAnnotation.originalName
                        val aliasName = function.name
                        extensionFunctions += buildKotlinFunction(aliasName) {
                            addModifiers(KModifier.INLINE)
                            setReceiverType(targetTypeName)
                            setParameters(parameterList.kotlinVersion)
                            setReturnType(returnType)
                            addInvokeFunctionStatement(returnType != null, null, originalName, parameterList.names)
                        }
                    }

                    declaration is KSClassDeclaration && declaration.isInterface -> {
                        val nestedInterface = declaration
                        logger.kspRequire(nestedInterface.hasAnnotation<Alias>(), nestedInterface) {
                            "Nested interface '${nestedInterface.name}' must be annotated with ${Alias::class.atName}."
                        }
                    }

                    else -> logger.kspError(declaration) {
                        "Only properties, functions, and nested interfaces " +
                                "are allowed inside ${Alias::class.atName} interfaces."
                    }
                }
            }
            accumulateExtension(targetClassName) {
                symbols += symbol
                nullIfNot(annotation.typeAlias.isNotEmpty()) {
                    typeAliases.add(buildKotlinTypeAlias(annotation.typeAlias, targetTypeName))
                }
                properties += extensionProperties
                functions += extensionFunctions
            }
        }
        resolver.forEachSymbolsAnnotatedWith<Unlocker> { symbol, annotation, arguments ->
            logger.kspRequire(symbol is KSClassDeclaration && symbol.isInterface, symbol) {
                "Annotation ${Unlocker::class.atName} can only be applied to interfaces."
            }
            logger.kspRequire(symbol.superInterfaceTypes.isEmpty(), symbol) {
                "Interface annotated with ${Unlocker::class.atName} must not extends interfaces."
            }
            symbol.parentDeclaration?.let { parent ->
                logger.kspRequire(parent is KSClassDeclaration && parent.isInterface, symbol) {
                    "Interface annotated with ${Unlocker::class.atName} must be nested inside another interface."
                }
                val parentUnlockerAnnotation = parent.getSingleAnnotationOrNull<Unlocker>()
                logger.kspRequire(parentUnlockerAnnotation != null, symbol) {
                    "Outer interface '${parent.name}' must be annotated with ${Unlocker::class.atName} " +
                            "to contain nested ${Unlocker::class.atName} interfaces."
                }
                if (annotation.widener.isNotEmpty()) {
                    logger.kspRequire(parentUnlockerAnnotation.widener.isNotEmpty(), symbol) {
                        "Outer interface '${parent.name}' must be have non-empty ${Unlocker::widener.name} parameter " +
                                "to contain nested ${Unlocker::widener.name} parameter."
                    }
                }
            }
            val targetTypeName = arguments.getValue(Unlocker::target.name)
            val targetClassName = targetTypeName.className
            logger.kspRequire(targetClassName != null, symbol) {
                "${Unlocker::class.atName} target must be a class reference."
            }
            if (annotation.widener.isNotEmpty()) {
                wideners += if (symbol.parentDeclaration != null) {
                    generateSequence(symbol) { it.parentDeclaration as? KSClassDeclaration }
                        .toList()
                        .mapNotNull { it.getSingleAnnotationOrNull<Unlocker>() }
                        .asReversed()
                        .joinToString("$") { it.widener.removePrefix(".") }
                } else {
                    annotation.widener
                }
            }

            val mixinClassName = KJClassName(symbol.packageName.asString(), symbol.name + "_Mixin")
            val mixinCast = buildKotlinCast(to = mixinClassName)

            val factoryClassName = KJClassName(targetClassName.packageName, targetTypeName.name + "KFactory")

            val mixinMethods = mutableListOf<JMethod>()
            val topLevelFunctions = mutableListOf<KFunction>()
            val extensionProperties = mutableListOf<KProperty>()
            val extensionFunctions = mutableListOf<KFunction>()
            val factoryProperties = mutableListOf<KProperty>()
            val factoryFunctions = mutableListOf<KFunction>()

            symbol.declarations.forEach { declaration ->
                when {
                    declaration is KSPropertyDeclaration -> {
                        val property = declaration
                        val unlockFieldAnnotation = property.getSingleAnnotationOrNull<UnlockField>()
                        logger.kspRequire(unlockFieldAnnotation != null, property) {
                            "Properties inside ${Unlocker::class.atName} interfaces " +
                                    "must be annotated with ${UnlockField::class.atName}."
                        }
                        logger.kspRequire(property.getter?.isAbstract == true, property) {
                            "Properties inside ${Unlocker::class.atName} interfaces must not declare a getter."
                        }
                        val propertyTypeName = property.type.asKJTypeName()
                        val target = unlockFieldAnnotation.target.ifEmpty { property.name }
                        val accessorGetter = buildAccessorMethod(
                            UnlockerMethodType.GETTER,
                            target,
                            unlockFieldAnnotation.isStatic,
                            propertyTypeName,
                            property.name
                        )
                        mixinMethods += accessorGetter
                        val accessorSetter = nullIfNot(property.isMutable) {
                            buildAccessorMethod(
                                UnlockerMethodType.SETTER,
                                target,
                                unlockFieldAnnotation.isStatic,
                                propertyTypeName,
                                property.name,
                            )
                        }
                        mixinMethods.addIfNotNull(accessorSetter)
                        val factoryProperty = nullIfNot(unlockFieldAnnotation.isStatic) {
                            buildKotlinProperty(property.name, propertyTypeName) {
                                getter(buildKotlinGetter {
                                    addInvokeFunctionStatement(true, mixinClassName, accessorGetter.name())
                                })
                                if (accessorSetter != null) {
                                    mutable(true)
                                    setter(buildKotlinSetter {
                                        setParameters(SETTER_ARGUMENT_NAME to propertyTypeName)
                                        addInvokeFunctionStatement(
                                            false, mixinClassName, accessorSetter.name(), listOf(SETTER_ARGUMENT_NAME)
                                        )
                                    })
                                }
                            }
                        }
                        factoryProperties.addIfNotNull(factoryProperty)
                        extensionProperties += buildKotlinProperty(property.name, propertyTypeName) {
                            setReceiverType(targetTypeName)
                            getter(buildKotlinGetter {
                                addModifiers(KModifier.INLINE)
                                if (factoryProperty != null) {
                                    addGetterStatement(factoryClassName, factoryProperty.name)
                                } else {
                                    addInvokeFunctionStatement(true, mixinCast, accessorGetter.name())
                                }
                            })
                            if (accessorSetter != null) {
                                mutable(true)
                                setter(buildKotlinSetter {
                                    addModifiers(KModifier.INLINE)
                                    setParameters(SETTER_ARGUMENT_NAME to propertyTypeName)
                                    if (factoryProperty != null) {
                                        addSetterStatement(factoryClassName, factoryProperty.name, SETTER_ARGUMENT_NAME)
                                    } else {
                                        addInvokeFunctionStatement(
                                            true, mixinCast, accessorSetter.name(), listOf(SETTER_ARGUMENT_NAME)
                                        )
                                    }
                                })
                            }
                        }
                    }

                    declaration is KSFunctionDeclaration -> {
                        val function = declaration
                        logger.kspRequire(function.isAbstract, function) {
                            "Functions inside ${Unlocker::class.atName} interfaces must not have a body."
                        }
                        val parameterList = function.parameters.asKJParameterList()
                        val returnType = function.getReturnTypeOrNull()
                        val unlockMethodAnnotation = function.getSingleAnnotationOrNull<UnlockMethod>()
                        val openConstructorAnnotation = function.getSingleAnnotationOrNull<UnlockConstructor>()
                        when {
                            unlockMethodAnnotation != null && openConstructorAnnotation == null -> {
                                val invoker = buildInvokerMethod(
                                    unlockMethodAnnotation.target.ifEmpty { function.name },
                                    unlockMethodAnnotation.isStatic,
                                    function.getReturnTypeOrNull(),
                                    "invoke" + function.name.capitalized(),
                                    parameterList.javaVersion
                                )
                                mixinMethods += invoker

                                val factoryFunction = nullIfNot(unlockMethodAnnotation.isStatic) {
                                    buildKotlinFunction(function.name) {
                                        setParameters(parameterList.kotlinVersion)
                                        addInvokeFunctionStatement(
                                            returnType != null, mixinClassName, invoker.name(), parameterList.names
                                        )
                                        setReturnType(returnType)
                                    }
                                }
                                factoryFunctions.addIfNotNull(factoryFunction)

                                extensionFunctions += buildKotlinFunction(function.name) {
                                    addModifiers(KModifier.INLINE)
                                    setReceiverType(targetTypeName)
                                    setParameters(parameterList.kotlinVersion)
                                    if (factoryFunction != null) {
                                        addInvokeFunctionStatement(
                                            returnType != null,
                                            factoryClassName,
                                            factoryFunction.name,
                                            parameterList.names
                                        )
                                    } else {
                                        addInvokeFunctionStatement(
                                            returnType != null, mixinCast, invoker.name(), parameterList.names
                                        )
                                    }
                                    setReturnType(returnType)
                                }
                            }

                            openConstructorAnnotation != null && unlockMethodAnnotation == null -> {
                                logger.kspRequire(returnType == null, function) {
                                    "Functions annotated with ${UnlockConstructor::class.atName} " +
                                            "must not have a return type."
                                }
                                val invokerMethod = buildInvokerMethod(
                                    openConstructorAnnotation.target,
                                    true,
                                    targetTypeName,
                                    function.name,
                                    parameterList.javaVersion
                                )
                                mixinMethods += invokerMethod
                                val factoryFunction = buildKotlinFunction(function.name) {
                                    setParameters(parameterList.kotlinVersion)
                                    setReturnType(targetTypeName)
                                    addInvokeFunctionStatement(
                                        true, mixinClassName, invokerMethod.name(), parameterList.names
                                    )
                                }
                                factoryFunctions += factoryFunction
                                topLevelFunctions += buildKotlinFunction(targetTypeName.name) {
                                    addModifiers(KModifier.INLINE)
                                    setParameters(parameterList.kotlinVersion)
                                    setReturnType(targetTypeName)
                                    addInvokeFunctionStatement(
                                        true, factoryClassName, factoryFunction.name, parameterList.names
                                    )
                                }
                            }

                            else -> {
                                logger.kspError(declaration) {
                                    "Functions inside ${Unlocker::class.atName} interfaces " +
                                            "must be annotated with " +
                                            "${UnlockMethod::class.atName} or ${UnlockConstructor::class.atName}."
                                }
                            }
                        }
                    }

                    declaration is KSClassDeclaration && declaration.isInterface -> {
                        val nestedInterface = declaration
                        logger.kspRequire(nestedInterface.hasAnnotation<Unlocker>(), nestedInterface) {
                            "Nested interface '${nestedInterface.name}' " +
                                    "must be annotated with ${Unlocker::class.atName}."
                        }
                    }

                    else -> logger.kspError(declaration) {
                        "Only properties, functions, and nested interfaces " +
                                "are allowed inside ${Unlocker::class.atName} interfaces."
                    }
                }
            }
            accumulateUnlocker(mixinClassName, targetTypeName) {
                symbols += symbol
                methods += mixinMethods
            }
            accumulateExtension(targetClassName) {
                symbols += symbol
                this.topLevelFunctions += topLevelFunctions
                properties += extensionProperties
                functions += extensionFunctions
            }
            accumulateFactory(factoryClassName) {
                symbols += symbol
                properties += factoryProperties
                functions += factoryFunctions
            }
        }
        resolver.forEachSymbolsAnnotatedWith<Patcher> { symbol, _, arguments ->
            logger.kspRequire(symbol is KSClassDeclaration && symbol.isClass, symbol) {
                "Annotation ${Patcher::class.atName} can only be applied to class."
            }
            logger.kspRequire(symbol.isAbstract, symbol) {
                "Class annotated with ${Patcher::class.atName} must be abstract."
            }
            val targetTypeName = arguments.getValue(Patcher::target.name)
            val targetClassName = targetTypeName.className
            logger.kspRequire(targetClassName != null, symbol) {
                "${Patcher::class.atName} target must be a class reference."
            }
            val patchSuperType = symbol.getSuperClassTypeOrNull()
            logger.kspRequire(patchSuperType?.declaration?.isInstance<Patch<*>>() == true, symbol) {
                "Class annotated with ${Patcher::class.atName} must extend ${Patch::class.qualifiedName}."
            }
            logger.kspRequire(patchSuperType.genericTypes().singleOrNull() == targetTypeName, symbol) {
                "${Patch::class.qualifiedName} generic type does not match ${Patcher::class.atName} target."
            }

            val patchPackageName = symbol.packageName.asString()
            val implClassName = KJClassName(patchPackageName, symbol.name + "_Impl")
            val mixinClassName = KJClassName(patchPackageName, symbol.name + "_Mixin")
            val bridgeClassName = KJClassName(patchPackageName, symbol.name + "_Bridge")
            val bridgeCast = buildKotlinCast(to = bridgeClassName)

            val bridgeFunctions = mutableListOf<KFunction>()
            val mixinMethods = mutableListOf<JMethod>()
            val extensionProperties = mutableListOf<KProperty>()
            val extensionFunctions = mutableListOf<KFunction>()

            symbol.propertyDeclarations.filter { !it.isPrivate }.forEach { property ->
                val originalName = property.name
                val getterName = "get" + originalName.capitalized()
                val setterName = "set" + property.name.capitalized()
                val bridgeGetterName = modId + "_" + getterName
                val bridgeSetterName = modId + "_" + setterName
                val propertyTypeName = property.type.asKJTypeName()
                bridgeFunctions += buildKotlinFunction(bridgeGetterName) {
                    addModifiers(KModifier.ABSTRACT)
                    setReturnType(propertyTypeName)
                }
                mixinMethods += buildJavaMethod(bridgeGetterName) {
                    addAnnotation<Override>()
                    addModifiers(Modifier.PUBLIC)
                    setReturnType(propertyTypeName)
                    addInvokeFunctionStatement(true, JCodeBlock.of("getOrInitPatch()"), getterName)
                }
                if (property.isMutable) {
                    bridgeFunctions += buildKotlinFunction(bridgeSetterName) {
                        addModifiers(KModifier.ABSTRACT)
                        setParameters(SETTER_ARGUMENT_NAME to propertyTypeName)
                    }
                    mixinMethods += buildJavaMethod(bridgeSetterName) {
                        addAnnotation<Override>()
                        addModifiers(Modifier.PUBLIC)
                        setParameters(propertyTypeName to SETTER_ARGUMENT_NAME)
                        addInvokeFunctionStatement(
                            false,
                            JCodeBlock.of("getOrInitPatch()"),
                            setterName,
                            listOf(SETTER_ARGUMENT_NAME)
                        )
                    }
                }
                extensionProperties += buildKotlinProperty(originalName, propertyTypeName) {
                    setReceiverType(targetTypeName)
                    getter(buildKotlinGetter {
                        addModifiers(KModifier.INLINE)
                        addInvokeFunctionStatement(true, bridgeCast, bridgeGetterName)
                    })
                    if (property.isMutable) {
                        mutable(true)
                        setter(buildKotlinSetter {
                            addModifiers(KModifier.INLINE)
                            setParameters(SETTER_ARGUMENT_NAME to propertyTypeName)
                            addInvokeFunctionStatement(
                                false,
                                bridgeCast,
                                bridgeSetterName,
                                listOf(SETTER_ARGUMENT_NAME)
                            )
                        })
                    }
                }
            }
            symbol.functionDeclarations.filter { !it.isPrivate }.forEach { function ->
                val originalName = function.name
                val bridgeName = modId + "_" + originalName
                val parameterList = function.parameters.asKJParameterList()
                val returnType = function.getReturnTypeOrNull()
                bridgeFunctions += buildKotlinFunction(bridgeName) {
                    addModifiers(KModifier.ABSTRACT)
                    setParameters(parameterList.kotlinVersion)
                    setReturnType(returnType)
                }
                mixinMethods += buildJavaMethod(bridgeName) {
                    addAnnotation<Override>()
                    addModifiers(Modifier.PUBLIC)
                    setReturnType(returnType)
                    setParameters(parameterList.javaVersion)
                    addInvokeFunctionStatement(
                        returnType != null,
                        JCodeBlock.of("getOrInitPatch()"),
                        originalName,
                        parameterList.names
                    )
                }
                extensionFunctions += buildKotlinFunction(originalName) {
                    addModifiers(KModifier.INLINE)
                    setReceiverType(targetTypeName)
                    setParameters(parameterList.kotlinVersion)
                    setReturnType(returnType)
                    addInvokeFunctionStatement(returnType != null, bridgeCast, bridgeName, parameterList.names)
                }
            }
            buildKotlinInterface(bridgeClassName.name) {
                addFunctions(bridgeFunctions)
            }.toKotlinFile(bridgeClassName.packageName) {
                addAnnotation<Suppress> {
                    addStringArrayMember(
                        Suppress::names.name,
                        listOf(
                            "RedundantVisibilityModifier",
                            "ClassName",
                            "unused",
                        )
                    )
                }
            }.writeTo(generator, symbol.toDependencies())
            accumulatePatch(mixinClassName, targetTypeName, symbol.asKJTypeName(), implClassName, bridgeClassName) {
                symbols += symbol
                methods += mixinMethods
            }
            accumulateExtension(targetClassName) {
                symbols += symbol
                properties += extensionProperties
                functions += extensionFunctions
            }
        }
    }

    override fun finish() {
        unlockers.forEach { (className, unlocker) ->
            if (unlocker.isEmpty()) {
                return@forEach
            }
            buildJavaInterface(className.name) {
                addAnnotation<Mixin> {
                    addClassMember(DEFAULT_ANNOTATION_ELEMENT_NAME, unlocker.targetTypeName)
                }
                addModifiers(Modifier.PUBLIC)
                addMethods(unlocker.methods)
            }.toJavaFile(className.packageName).writeTo(generator, unlocker.symbols.toDependencies())
        }
        patches.forEach { (mixinClassName, patch) ->
            if (patch.isEmpty()) {
                return@forEach
            }
            buildKotlinClass(patch.implClassName.name) {
                val propertyName = Patch<*>::target.name
                setConstructor(propertyName to patch.targetTypeName)
                addProperty(buildKotlinProperty(propertyName, patch.targetTypeName) {
                    addModifiers(KModifier.OVERRIDE)
                    initializer(propertyName)
                })
                setSuperClassType(patch.patchTypeName)
            }.toKotlinFile(patch.implClassName.packageName) {

            }.writeTo(generator, patch.symbols.toDependencies())
            buildJavaClass(mixinClassName.name) {
                addAnnotation<Mixin> {
                    addClassArrayMember(DEFAULT_ANNOTATION_ELEMENT_NAME, patch.targetTypeName)
                }
                addAnnotation<SuppressWarnings> {
                    addStringMember(DEFAULT_ANNOTATION_ELEMENT_NAME, "NullableProblems")
                }
                addSuperinterface(patch.bridgeClassName.javaVersion)
                val implFieldName = "patch"
                val implGetterName = "getOrInit" + implFieldName.capitalized()
                addField(buildJavaField(patch.implClassName.javaVersion, implFieldName) {
                    addAnnotation<Unique>()
                    addModifiers(Modifier.PRIVATE)
                })
                addMethod(buildJavaMethod(implGetterName) {
                    addAnnotation<Unique>()
                    addModifiers(Modifier.PRIVATE)
                    setReturnType(patch.implClassName.javaVersion)
                    addIfStatement(JCodeBlock.of("$implFieldName == ${null.toString()}")) {
                        val javaObject = KJClassName("java.lang", "Object")
                        val objectCast = buildJavaCast(to = javaObject.typeName)
                        val targetCast = buildJavaCast(to = patch.targetTypeName, from = objectCast)
                        addStatement(
                            "\$L = new \$T(\$L)",
                            implFieldName,
                            patch.implClassName.javaVersion,
                            targetCast
                        )
                    }
                    addReturnStatement(implFieldName)
                })
                addMethods(patch.methods)
            }.toJavaFile(mixinClassName.packageName).writeTo(generator, patch.symbols.toDependencies())
        }
        extensions.forEach { (className, extension) ->
            if (extension.isEmpty()) {
                return@forEach
            }
            buildKotlinFile(className.packageName, className.name + "Ext") {
                addAnnotation<Suppress> {
                    addStringArrayMember(
                        Suppress::names.name,
                        listOf(
                            "CAST_NEVER_SUCCEEDS",
                            "NOTHING_TO_INLINE",
                            "UnusedImport",
                            "UnusedReceiverParameter",
                            "RedundantVisibilityModifier",
                            "unused",
                        )
                    )
                }
                extension.typeAliases.forEach {
                    addTypeAlias(it)
                }
                addFunctions(extension.topLevelFunctions)
                addProperties(extension.properties)
                addFunctions(extension.functions)
            }.writeTo(generator, extension.symbols.toDependencies())
        }
        factories.forEach { (className, factory) ->
            if (factory.isEmpty()) {
                return@forEach
            }
            buildKotlinObject(className.name) {
                addProperties(factory.properties)
                addFunctions(factory.functions)
            }.toKotlinFile(className.packageName) {
                addAnnotation<Suppress> {
                    addStringArrayMember(
                        Suppress::names.name,
                        listOf(
                            "CAST_NEVER_SUCCEEDS",
                            "UnusedImport",
                            "RedundantVisibilityModifier",
                            "unused",
                        )
                    )
                }
            }.writeTo(generator, factory.symbols.toDependencies())
        }
        generator.createResourceFile(
            path = "META-INF/nametag/wideners.txt",
            contents = wideners.joinToString("\n"),
            aggregating = true,
        )
        reset()
    }

    override fun onError() {
        reset()
    }

    private fun reset() {
        extensions.clear()
        factories.clear()
        unlockers.clear()
        patches.clear()

        wideners.clear()
    }

    private fun buildAccessorMethod(
        methodType: UnlockerMethodType,
        target: String,
        isStatic: Boolean,
        type: KJTypeName,
        name: String,
    ): JMethod =
        buildJavaMethod(methodType.buildMethodName(name)) {
            val isSetter = methodType == UnlockerMethodType.SETTER
            addAnnotation<Accessor> {
                addStringMember(DEFAULT_ANNOTATION_ELEMENT_NAME, target)
            }
            if (isSetter) {
                addAnnotation<Mutable>()
            }
            addModifiers(
                Modifier.PUBLIC,
                if (isStatic) Modifier.STATIC else Modifier.ABSTRACT
            )
            if (isSetter) {
                setParameters(type to name)
            }
            if (isStatic && !isSetter) {
                addStubStatement()
            }
            setReturnType(type.takeIf { !isSetter })
        }

    private fun buildInvokerMethod(
        target: String,
        isStatic: Boolean,
        returnType: KJTypeName?,
        name: String,
        parameters: List<JParameter>,
    ): JMethod =
        buildJavaMethod(name) {
            addAnnotation<Invoker> {
                addStringMember(DEFAULT_ANNOTATION_ELEMENT_NAME, target)
            }
            addModifiers(
                Modifier.PUBLIC,
                if (isStatic) Modifier.STATIC else Modifier.ABSTRACT
            )
            setParameters(parameters)
            if (isStatic) {
                addStubStatement()
            }
            setReturnType(returnType)
        }

    private fun accumulateExtension(targetClassName: KJClassName, block: GeneratedExtension.() -> Unit) {
        extensions
            .getOrPut(targetClassName) { GeneratedExtension() }
            .apply(block)
    }

    private fun accumulateFactory(targetClassName: KJClassName, block: GeneratedFactory.() -> Unit) {
        factories
            .getOrPut(targetClassName) { GeneratedFactory() }
            .apply(block)
    }

    private fun accumulateUnlocker(
        className: KJClassName,
        targetTypeName: KJTypeName,
        block: GeneratedUnlocker.() -> Unit
    ) {
        unlockers
            .getOrPut(className) { GeneratedUnlocker(targetTypeName) }
            .apply(block)
    }

    private fun accumulatePatch(
        className: KJClassName,
        targetTypeName: KJTypeName,
        patchTypeName: KJTypeName,
        implClassName: KJClassName,
        bridgeClassName: KJClassName,
        block: GeneratedPatch.() -> Unit
    ) {
        patches
            .getOrPut(className) {
                GeneratedPatch(
                    targetTypeName = targetTypeName,
                    patchTypeName = patchTypeName,
                    implClassName = implClassName,
                    bridgeClassName = bridgeClassName,
                )
            }
            .apply(block)
    }

    private enum class UnlockerMethodType(val namePrefix: String) {

        GETTER("get"),
        SETTER("set");

        fun buildMethodName(propertyName: String): String =
            namePrefix + propertyName.capitalized()
    }

    class GeneratedExtension(
        val symbols: MutableSet<KSAnnotated> = mutableSetOf(),
        val typeAliases: MutableList<KTypeAlias> = mutableListOf(),
        val topLevelFunctions: MutableList<KFunction> = mutableListOf(),
        val properties: MutableList<KProperty> = mutableListOf(),
        val functions: MutableList<KFunction> = mutableListOf(),
    ) {
        fun isEmpty(): Boolean =
            typeAliases.isEmpty() && topLevelFunctions.isEmpty() && properties.isEmpty() && functions.isEmpty()
    }

    class GeneratedFactory(
        val symbols: MutableSet<KSAnnotated> = mutableSetOf(),
        val properties: MutableList<KProperty> = mutableListOf(),
        val functions: MutableList<KFunction> = mutableListOf(),
    ) {
        fun isEmpty(): Boolean =
            properties.isEmpty() && functions.isEmpty()
    }

    class GeneratedUnlocker(
        val targetTypeName: KJTypeName,
        val symbols: MutableSet<KSAnnotated> = mutableSetOf(),
        val methods: MutableList<JMethod> = mutableListOf(),
    ) {
        fun isEmpty(): Boolean =
            methods.isEmpty()
    }

    class GeneratedPatch(
        val targetTypeName: KJTypeName,
        val patchTypeName: KJTypeName,
        val implClassName: KJClassName,
        val bridgeClassName: KJClassName,
        val symbols: MutableSet<KSAnnotated> = mutableSetOf(),
        val methods: MutableList<JMethod> = mutableListOf(),
    ) {
        fun isEmpty(): Boolean =
            methods.isEmpty()
    }

    companion object {
        private const val DEFAULT_ANNOTATION_ELEMENT_NAME: String = "value"
        private const val SETTER_ARGUMENT_NAME: String = "newValue"

        private val injectionAnnotations: Map<KClass<out Annotation>, KClass<out Annotation>> =
            mapOf(
                Hook::class to Inject::class,
                Hook::class to Inject::class,
            )
    }
}
