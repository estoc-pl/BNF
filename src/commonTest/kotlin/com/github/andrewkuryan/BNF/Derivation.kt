package com.github.andrewkuryan.BNF

import com.github.andrewkuryan.BNF.Grammar.Companion.S
import com.github.andrewkuryan.BNF.ProductionKind.Recursion
import com.github.andrewkuryan.BNF.ProductionKind.Regular
import com.github.andrewkuryan.BNF.RecursionKind.*
import kotlin.test.Test
import kotlin.test.assertEquals

class DerivationTest {
    @Test
    fun `should return derivations for S → A ⏐ a；A → B ⏐ b；B → C ⏐ c；C → d`() {
        grammar {
            val A by nonterm()
            val B by nonterm()
            val C by nonterm()

            S /= A / 'a'
            A /= B / 'b'
            B /= C / 'c'
            C /= 'd'

            val grouped = getGroupedDerivations().mapValues { (_, value) ->
                value.mapValues { (_, nodes) -> nodes.map { it.getExpandedProduction() }.toSet() }
            }

            assertEquals(
                mapOf<Nonterminal, Map<ProductionKind, Set<Production<SyntaxNode>>>>(
                    C to mapOf(Regular to setOf(Production(listOf(Terminal('d'))))),
                    B to mapOf(Regular to ('c' / 'd').toSet()),
                    A to mapOf(Regular to ('b' / 'c' / 'd').toSet()),
                    S to mapOf(Regular to ('a' / 'b' / 'c' / 'd').toSet())
                ), grouped
            )
        }
    }

    @Test
    fun `should return derivations for S → aT；T → Sb ⏐ c`() {
        grammar {
            val T by nonterm()

            S /= 'a'..T
            T /= S..'b' / 'c'

            val grouped = getGroupedDerivations().mapValues { (_, value) ->
                value.mapValues { (_, nodes) -> nodes.map { it.getExpandedProduction() }.toSet() }
            }

            assertEquals(
                mapOf(
                    T to mapOf(
                        Recursion(setOf(CENTRAL)) to ('a'..T..'b').toSet(),
                        Regular to setOf(Production(listOf(Terminal('c'))))
                    ),
                    S to mapOf(
                        Recursion(setOf(CENTRAL)) to ('a'..S..'b').toSet(),
                        Regular to setOf(Production(listOf(Terminal('a'), Terminal('c'))))
                    )
                ), grouped
            )
        }
    }

    @Test
    fun `should return derivations for S → Aa ⏐ x；A → Bb ⏐ y；B → Cc ⏐ z；C → Ad ⏐ w`() {
        grammar {
            val A by nonterm()
            val B by nonterm()
            val C by nonterm()

            S /= A..'a' / 'x'
            A /= B..'b' / 'y'
            B /= C..'c' / 'z'
            C /= A..'d' / 'w'

            val grouped = getGroupedDerivations().mapValues { (_, value) ->
                value.mapValues { (_, nodes) -> nodes.map { it.getExpandedProduction() }.toSet() }
            }

            assertEquals(
                mapOf(
                    C to mapOf(
                        Recursion(setOf(LEFT)) to (C..'c'..'b'..'d').toSet(),
                        Regular to ('z'..'b'..'d' / 'y'..'d' / 'w').toSet()
                    ),
                    B to mapOf(
                        Recursion(setOf(LEFT)) to (B..'b'..'d'..'c').toSet(),
                        Regular to ('y'..'d'..'c' / 'w'..'c' / 'z').toSet()
                    ),
                    A to mapOf(
                        Recursion(setOf(LEFT)) to (A..'d'..'c'..'b').toSet(),
                        Regular to ('w'..'c'..'b' / 'z'..'b' / 'y').toSet(),
                    ),
                    S to mapOf(
                        Regular to (A..'d'..'c'..'b'..'a' / 'w'..'c'..'b'..'a' / 'z'..'b'..'a' / 'y'..'a' / 'x').toSet()
                    )
                ), grouped
            )
        }
    }

    @Test
    fun `should return derivations for S → ASB；A → Sa ⏐ x；B → bS ⏐ y`() {
        grammar {
            val A by nonterm()
            val B by nonterm()

            S /= A..S..B
            A /= S..'a' / 'x'
            B /= 'b'..S / 'y'

            val grouped = getGroupedDerivations().mapValues { (_, value) ->
                value.mapValues { (_, nodes) -> nodes.map { it.getExpandedProduction() }.toSet() }
            }

            assertEquals(
                mapOf(
                    A to mapOf(
                        Recursion(setOf(LEFT)) to (A..S..'b'..S..'a' / A..S..'y'..'a').toSet(),
                        Regular to setOf(Production(listOf(Terminal('x'))))
                    ),
                    B to mapOf(
                        Recursion(setOf(RIGHT)) to ('b'..S..'a'..S..B / 'b'..'x'..S..B).toSet(),
                        Regular to setOf(Production(listOf(Terminal('y'))))
                    ),
                    S to mapOf(
                        Recursion(setOf(LEFT, CENTRAL, RIGHT)) to (S..'a'..S..'b'..S).toSet(),
                        Recursion(setOf(LEFT, CENTRAL)) to (S..'a'..S..'y').toSet(),
                        Recursion(setOf(CENTRAL, RIGHT)) to ('x'..S..'b'..S).toSet(),
                        Recursion(setOf(CENTRAL)) to ('x'..S..'y').toSet()
                    )
                ), grouped
            )
        }
    }
}