package io.github.recrafter.lapis.extensions.psi

import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.com.intellij.psi.PsiRecursiveElementWalkingVisitor

fun PsiFile.findPsiFunction(action: (PsiFunction) -> Boolean): PsiFunction? {
    var found: PsiFunction? = null
    accept(object : PsiRecursiveElementWalkingVisitor() {
        override fun visitElement(psiElement: PsiElement) {
            if (psiElement is PsiFunction && action(psiElement)) {
                found = psiElement
                stopWalking()
                return
            }
            super.visitElement(psiElement)
        }
    })
    return found
}
