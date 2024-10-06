package com.github.andrewkuryan.BNF.analysis

import com.github.andrewkuryan.BNF.Grammar
import com.github.andrewkuryan.BNF.getGroupedDerivations

enum class ConclusionSeverity { INFO, WARNING, ERROR }

enum class ConclusionFormatPattern { SHORT, DETAILED }

sealed class Conclusion(
    val severity: ConclusionSeverity,
    val title: String,
    val details: String,
    val description: String,
) {
    fun format(pattern: ConclusionFormatPattern = ConclusionFormatPattern.SHORT) = when (pattern) {
        ConclusionFormatPattern.SHORT -> "$severity: $title"
        ConclusionFormatPattern.DETAILED -> "$severity: $details. $description"
    }
}

fun analyze(grammar: Grammar<*>): List<Conclusion> = grammar.getGroupedDerivations()
    .let { derivations ->
        listOfNotNull(
            hasUnresolvedRecursions(derivations),
            hasLeftRecursions(derivations),
            hasLeftRightRecursions(derivations),
            hasAmbiguousDerivations(derivations),
            isRegular(derivations)
        )
    }