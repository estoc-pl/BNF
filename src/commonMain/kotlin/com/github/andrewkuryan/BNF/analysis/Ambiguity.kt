package com.github.andrewkuryan.BNF.analysis

import com.github.andrewkuryan.BNF.Derivations
import com.github.andrewkuryan.BNF.Nonterminal
import com.github.andrewkuryan.BNF.ProductionKind
import com.github.andrewkuryan.BNF.ProductionKind.Recursion
import com.github.andrewkuryan.BNF.RecursionKind.LEFT
import com.github.andrewkuryan.BNF.RecursionKind.RIGHT

data class LeftRightRecursion(val nonterms: Set<Nonterminal>) : Conclusion(
    ConclusionSeverity.WARNING,
    "Left-Right recursion",
    "Nonterminal${if (nonterms.size > 1) "s" else ""} ${nonterms.joinToString(", ")} ${if (nonterms.size > 1) "have" else "has"} left and right recursion at the same time",
    "Left-Right recursion leads to the ambiguity of a grammar",
)

data class AmbiguousDerivations(val nonterms: Set<Nonterminal>) : Conclusion(
    ConclusionSeverity.WARNING,
    "Ambiguous Derivations",
    "Nonterminal${if (nonterms.size > 1) "s" else ""} ${nonterms.joinToString(", ")} ${if (nonterms.size > 1) "have" else "has"} ambiguous derivations",
    "Ambiguous derivations lead to the ambiguity of a grammar",
)

fun hasLeftRightRecursions(derivations: Map<Nonterminal, Map<ProductionKind, Derivations>>): LeftRightRecursion? =
    derivations
        .filter { (_, value) -> value.any { (prodKind, _) -> prodKind is Recursion && LEFT in prodKind.kinds && RIGHT in prodKind.kinds } }
        .keys.takeIf { it.isNotEmpty() }?.let { LeftRightRecursion(it) }

fun hasAmbiguousDerivations(derivations: Map<Nonterminal, Map<ProductionKind, Derivations>>): AmbiguousDerivations? =
    derivations
        .filter { (_, value) ->
            value.values.flatten()
                .groupBy { node -> node.getExpandedProduction() }
                .any { it.value.size > 1 }
        }
        .keys.takeIf { it.isNotEmpty() }?.let { AmbiguousDerivations(it) }