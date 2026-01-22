package io.github.recrafter.lapis.utils

import io.github.recrafter.lapis.extensions.ksp.*
import io.github.recrafter.lapis.extensions.psi.PsiFactory
import io.github.recrafter.lapis.extensions.psi.PsiFile
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.com.intellij.openapi.Disposable
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.utils.addToStdlib.UnsafeCastFunction
import org.jetbrains.kotlin.utils.addToStdlib.safeAs

class PsiCompanion(val logger: KspLogger) {

    private val factory: PsiFactory by lazy {
        val environment = KotlinCoreEnvironment.createForTests(
            disposable,
            CompilerConfiguration(),
            EnvironmentConfigFiles.JVM_CONFIG_FILES
        )
        PsiFactory(environment.project)
    }

    private val disposable: Disposable = Disposer.newDisposable()
    private val cache: MutableMap<String, PsiFile> = mutableMapOf()

    @OptIn(UnsafeCastFunction::class)
    fun loadPsiFile(node: KspNode): PsiFile {
        val file = node.location.safeAs<KspFileLocation>()?.file ?: resolvingError(node)
        if (!file.exists()) {
            resolvingError(node)
        }
        return cache.getOrPut(file.canonicalPath) {
            val contents = file.readText()
            if (contents.trim().isEmpty()) {
                resolvingError(node)
            }
            factory.createFile(file.name, contents)
        }
    }

    fun destroy() {
        Disposer.dispose(disposable)
        cache.clear()
    }

    fun resolvingError(node: KspNode): Nothing =
        logger.error(node) { "Unable to resolve KSP node in PSI model." }
}
