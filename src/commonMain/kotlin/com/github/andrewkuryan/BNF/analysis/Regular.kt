package com.github.andrewkuryan.BNF.analysis

import com.github.andrewkuryan.BNF.Derivations
import com.github.andrewkuryan.BNF.Nonterminal
import com.github.andrewkuryan.BNF.ProductionKind
import com.github.andrewkuryan.BNF.RecursionKind.LEFT
import com.github.andrewkuryan.BNF.RecursionKind.RIGHT

data object Regular : Conclusion(
    ConclusionSeverity.INFO,
    "Regular grammar",
    "The grammar is regular",
    "Regular grammars can be represented using regular expressions. You probably don't need to use BNF for them"
)

fun isRegular(derivations: Map<Nonterminal, Map<ProductionKind, Derivations>>): Regular? =
    derivations.all { (_, value) ->
        value.all {
            it.key == ProductionKind.Regular ||
                    it.key == ProductionKind.Recursion(setOf(LEFT)) ||
                    it.key == ProductionKind.Recursion(setOf(RIGHT))
        }
    }.takeIf { it }?.let { Regular }