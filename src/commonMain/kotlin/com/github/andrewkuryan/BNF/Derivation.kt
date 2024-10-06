package com.github.andrewkuryan.BNF

enum class RecursionKind { LEFT, RIGHT, CENTRAL }

sealed class ProductionKind {
    data class Recursion(val kinds: Set<RecursionKind>) : ProductionKind()
    data object Regular : ProductionKind()
}

data class DerivationNode<N : SyntaxNode>(val production: Production<N>, val children: Map<Int, DerivationNode<N>>) {
    override fun toString(): String = production.toString() +
            children.entries.joinToString("", "\n") { "${production.symbols[it.key]} => ${it.value}" }

    fun getExpandedProduction(): Production<N> =
        production.symbols.foldIndexed(Production(emptyList())) { index, acc, symbol ->
            if (index in children) acc + children.getValue(index).getExpandedProduction()
            else acc + symbol
        }
}

typealias Derivations<N> = Set<DerivationNode<N>>

fun <N : SyntaxNode> Grammar<N>.getDerivations(): Map<Nonterminal, Derivations<N>> =
    productions.mapValues { (nonterm, _) -> getNontermDerivations(nonterm) }

fun <N : SyntaxNode> Grammar<N>.getGroupedDerivations(): Map<Nonterminal, Map<ProductionKind, Derivations<N>>> =
    getDerivations().mapValues { (nonterm, derivations) -> groupNontermDerivations(nonterm, derivations) }

fun <N : SyntaxNode> Grammar<N>.getNontermDerivations(
    nonterm: Nonterminal,
    visited: Set<Nonterminal> = setOf(nonterm),
): Derivations<N> =
    productions.getValue(nonterm).fold(emptySet()) { prevNodes, production ->
        prevNodes + production.symbols.foldIndexed(mapOf<Int, Derivations<N>>()) { index, acc, symbol ->
            if (symbol is Nonterminal && symbol !in visited)
                acc + (index to getNontermDerivations(symbol, visited + symbol))
            else acc
        }.let { derivations ->
            derivations.entries.fold(listOf<Map<Int, DerivationNode<N>>>()) { acc, entry ->
                if (acc.isEmpty()) entry.value.map { mapOf(entry.key to it) }
                else acc.flatMap { prevEntry ->
                    entry.value.map { prevEntry + mapOf(entry.key to it) }.ifEmpty { listOf(prevEntry) }
                }
            }
        }.let { derivations ->
            if (derivations.isEmpty()) listOf(DerivationNode(production, emptyMap()))
            else derivations.map { DerivationNode(production, it) }
        }
    }

fun <N : SyntaxNode> groupNontermDerivations(
    nonterm: Nonterminal,
    derivations: Derivations<N>,
): Map<ProductionKind, Derivations<N>> =
    derivations.groupBy { node ->
        node.getExpandedProduction().let { production ->
            production.symbols.foldIndexed(emptySet<RecursionKind>()) { index, acc, it ->
                when (it) {
                    nonterm -> when (index) {
                        0 -> acc + RecursionKind.LEFT
                        production.size - 1 -> acc + RecursionKind.RIGHT
                        else -> acc + RecursionKind.CENTRAL
                    }

                    else -> acc
                }
            }
        }.let { if (it.isEmpty()) ProductionKind.Regular else ProductionKind.Recursion(it) }
    }.mapValues { (_, value) -> value.toSet() }
