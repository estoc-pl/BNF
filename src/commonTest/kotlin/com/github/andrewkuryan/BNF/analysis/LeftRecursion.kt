package com.github.andrewkuryan.BNF.analysis

import com.github.andrewkuryan.BNF.Grammar.Companion.S
import com.github.andrewkuryan.BNF.grammar
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class LeftRecursionTest {

    @Test
    fun `should eliminate left rec in S → Sa ⏐ Sb ⏐ c ⏐ d`() {
        grammar {
            S /= S..'a' / S..'b' / 'c' / 'd'

            val result = eliminateLeftRec()

            val Ss = result.productions.keys.find { it.origin == S }
            assertNotNull(Ss)

            assertEquals(
                mapOf(
                    S to ('c' / 'd' / 'c'..Ss / 'd'..Ss).toSet(),
                    Ss to ('a' / 'b' / 'a'..Ss / 'b'..Ss).toSet()
                ), result.productions
            )
        }
    }

    @Test
    fun `should eliminate left rec in S → AB；A → BS ⏐ b；B → SS ⏐ a`() {
        grammar {
            val A by nonterm()
            val B by nonterm()

            S /= A..B
            A /= B..S / 'b'
            B /= S..S / 'a'

            val result = eliminateLeftRec()

            val Bs = result.productions.keys.find { it.origin == B }
            assertNotNull(Bs)

            assertEquals(
                mapOf(
                    S to (A..B).toSet(),
                    A to (B..S / 'b').toSet(),
                    B to ('b'..B..S / 'a' / 'b'..B..S..Bs / 'a'..Bs).toSet(),
                    Bs to (S..B..S / S..B..S..Bs).toSet()
                ), result.productions
            )
        }
    }

    @Test
    fun `should eliminate left rec in S → Aa ⏐ Bb；A → Si；B → Cj ⏐ x；C → Sk ⏐ y`() {
        grammar {
            val A by nonterm()
            val B by nonterm()
            val C by nonterm()

            S /= A..'a' / B..'b'
            A /= S..'i'
            B /= C..'j' / 'x'
            C /= S..'k' / 'y'

            val result = eliminateLeftRec()

            val As = result.productions.keys.find { it.origin == A }
            val Cs = result.productions.keys.find { it.origin == C }
            assertNotNull(As)
            assertNotNull(Cs)

            assertEquals(
                mapOf(
                    S to (A..'a' / B..'b').toSet(),
                    A to (B..'b'..'i' / B..'b'..'i'..As).toSet(),
                    As to ('a'..'i' / 'a'..'i'..As).toSet(),
                    B to (C..'j' / 'x').toSet(),
                    C to ('x'..'b'..'i'..'a'..'k' / 'x'..'b'..'i'..As..'a'..'k' /
                            'x'..'b'..'i'..'a'..'k'..Cs / 'x'..'b'..'i'..As..'a'..'k'..Cs /
                            'x'..'b'..'k' / 'x'..'b'..'k'..Cs / 'y' / 'y'..Cs).toSet(),
                    Cs to ('j'..'b'..'i'..'a'..'k' / 'j'..'b'..'i'..As..'a'..'k' /
                            'j'..'b'..'i'..'a'..'k'..Cs / 'j'..'b'..'i'..As..'a'..'k'..Cs /
                            'j'..'b'..'k' / 'j'..'b'..'k'..Cs).toSet()
                ), result.productions
            )
        }
    }
}