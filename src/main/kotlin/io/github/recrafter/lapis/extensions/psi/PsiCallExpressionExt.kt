package io.github.recrafter.lapis.extensions.psi

val PsiCallExpression.calleeName: String
    get() = requireNotNull(calleeExpression).text
