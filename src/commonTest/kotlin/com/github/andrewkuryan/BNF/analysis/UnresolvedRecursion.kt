package com.github.andrewkuryan.BNF.analysis

import com.github.andrewkuryan.BNF.Grammar.Companion.S
import com.github.andrewkuryan.BNF.getGroupedDerivations
import com.github.andrewkuryan.BNF.grammar
import kotlin.test.Test
import kotlin.test.assertEquals

class UnresolvedRecursionTest {
    @Test
    fun `should find unresolved recursion in S → aA；A → bB；B → cC；C → dS`() {
        grammar {
            val A by nonterm()
            val B by nonterm()
            val C by nonterm()

            S /= 'a'..A
            A /= 'b'..B
            B /= 'c'..C
            C /= 'd'..S

            val result = hasUnresolvedRecursions(getGroupedDerivations())

            assertEquals(UnresolvedRecursion(setOf(S, A, B, C)), result)
        }
    }

    @Test
    fun `should find unresolved recursions in S → aA ⏐ c；A → bB；B → iAj`() {
        grammar {
            val A by nonterm()
            val B by nonterm()

            S /= 'a'..A / 'c'
            A /= 'b'..B
            B /= 'i'..A..'j'

            val result = hasUnresolvedRecursions(getGroupedDerivations())

            assertEquals(UnresolvedRecursion(setOf(A, B)), result)
        }
    }

    @Test
    fun `should not find unresolved recursions in S → aA ⏐ c；A → bB ⏐ x；B → iAj`() {
        grammar {
            val A by nonterm()
            val B by nonterm()

            S /= 'a'..A / 'c'
            A /= 'b'..B / 'x'
            B /= 'i'..A..'j'

            val result = hasUnresolvedRecursions(getGroupedDerivations())

            assertEquals(null, result)
        }
    }

    @Test
    fun `should find unresolved recursions in S → A ⏐ x；A → aB；B → bA`() {
        grammar {
            val A by nonterm()
            val B by nonterm()

            S /= A / 'x'
            A /= 'a'..B
            B /= 'b'..A

            val result = hasUnresolvedRecursions(getGroupedDerivations())

            assertEquals(UnresolvedRecursion(setOf(A, B)), result)
        }
    }

    @Test
    fun `should find unresolved recursions in S → Aa；A → Sb ⏐ Ac`() {
        grammar {
            val A by nonterm()

            S /= A..'a'
            A /= S..'b' / A..'c'

            val result = hasUnresolvedRecursions(getGroupedDerivations())

            assertEquals(UnresolvedRecursion(setOf(A, S)), result)
        }
    }
}