package com.github.andrewkuryan.BNF.analysis

import com.github.andrewkuryan.BNF.Derivations
import com.github.andrewkuryan.BNF.Nonterminal
import com.github.andrewkuryan.BNF.ProductionKind
import com.github.andrewkuryan.BNF.Terminal

data class UnresolvedRecursion(val nonterms: Set<Nonterminal>) : Conclusion(
    ConclusionSeverity.ERROR,
    "Unresolved recursion",
    "Nonterminal${if (nonterms.size > 1) "s" else ""} ${nonterms.joinToString(", ")} ${if (nonterms.size > 1) "have" else "has"} unresolved recursion",
    "Grammar with unresolved recursions cannot be matched with strings"
)

fun hasUnresolvedRecursion(
    nonterm: Nonterminal,
    derivations: Map<Nonterminal, Map<ProductionKind, Derivations<*>>>,
    result: Map<Nonterminal, Boolean>,
): Pair<Boolean, Map<Nonterminal, Boolean>> =
    derivations.getValue(nonterm)[ProductionKind.Regular]?.fold(true to result) { acc, derivation ->
        derivation.getExpandedProduction().symbols.fold(false to acc.second) { (isResolved, curResult), symbol ->
            when (symbol) {
                is Nonterminal -> when (symbol) {
                    in curResult -> (isResolved || curResult.getValue(symbol)) to curResult
                    else -> hasUnresolvedRecursion(symbol, derivations, curResult)
                        .let { (isResolved || it.first) to (curResult + it.second) }
                }

                is Terminal -> isResolved to curResult
            }
        }.let { (it.first && acc.first) to (acc.second + it.second) }
    }?.let { it.first to (it.second + (nonterm to it.first)) } ?: (true to (result + (nonterm to true)))

fun hasUnresolvedRecursions(derivations: Map<Nonterminal, Map<ProductionKind, Derivations<*>>>): UnresolvedRecursion? =
    derivations.entries
        .fold(emptyMap<Nonterminal, Boolean>()) { acc, (nonterm, _) ->
            if (nonterm in acc) acc else acc + hasUnresolvedRecursion(nonterm, derivations, acc).second
        }
        .filter { it.value }.keys.takeIf { it.isNotEmpty() }?.let { UnresolvedRecursion(it) }