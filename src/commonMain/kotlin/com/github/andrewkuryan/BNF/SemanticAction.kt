package com.github.andrewkuryan.BNF

open class SyntaxNode

data class SemanticAction<N : SyntaxNode>(val name: String, val action: (head: N, body: List<N>) -> Unit)
