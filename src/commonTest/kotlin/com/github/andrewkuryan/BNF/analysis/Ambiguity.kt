package com.github.andrewkuryan.BNF.analysis

import com.github.andrewkuryan.BNF.Grammar.Companion.S
import com.github.andrewkuryan.BNF.getGroupedDerivations
import com.github.andrewkuryan.BNF.grammar
import kotlin.test.Test
import kotlin.test.assertEquals

class AmbiguityTest {

    @Test
    fun `should find ambiguous derivations in S → Ac ⏐ Bc；A → ab；B → aC；C → b ⏐ d`() {
        grammar {
            val A by nonterm()
            val B by nonterm()
            val C by nonterm()

            S /= A..'c' / B..'c'
            A /= 'a'..'b'
            B /= 'a'..C
            C /= 'b' / 'd'

            val result = hasAmbiguousDerivations(getGroupedDerivations())

            assertEquals(AmbiguousDerivations(setOf(S)), result)
        }
    }

    @Test
    fun `should find ambiguous derivations in S → iAj；A → aBc ⏐ Dc；B → Sf；D → aiAjf`() {
        grammar {
            val A by nonterm()
            val B by nonterm()
            val C by nonterm()

            S /= 'i'..A..'j'
            A /= 'a'..B..'c' / C..'c'
            B /= S..'f'
            C /= 'a'..'i'..A..'j'..'f'

            val result = hasAmbiguousDerivations(getGroupedDerivations())

            assertEquals(AmbiguousDerivations(setOf(A)), result)
        }
    }
}