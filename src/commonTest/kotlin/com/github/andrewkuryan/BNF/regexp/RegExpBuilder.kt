package com.github.andrewkuryan.BNF.regexp

import com.github.andrewkuryan.BNF.RegExp.Symbol
import com.github.andrewkuryan.BNF.RegExp.Row
import com.github.andrewkuryan.BNF.RegExp.Range
import com.github.andrewkuryan.BNF.RegExp.OneOrMore
import com.github.andrewkuryan.BNF.RegExp.Or
import com.github.andrewkuryan.BNF.RegExp.Maybe
import com.github.andrewkuryan.BNF.RegExp.ε
import com.github.andrewkuryan.BNF.RegExp.Not
import com.github.andrewkuryan.BNF.regexp
import kotlin.test.Test
import kotlin.test.assertEquals

class OneOrMoreTest {
    @Test
    fun `should apply oneOrMore to ε`() {
        assertEquals(
            ε,
            regexp { ε.oneOrMore() }
        )
    }

    @Test
    fun `should apply oneOrMore to Row`() {
        assertEquals(
            OneOrMore(Row("abc")),
            regexp { Row("abc").oneOrMore() }
        )
    }

    @Test
    fun `should apply oneOrMore to OneOrMore❨Symbol❩`() {
        assertEquals(
            OneOrMore(Symbol('s')),
            regexp { OneOrMore(Symbol('s')).oneOrMore() }
        )
    }

    @Test
    fun `should apply oneOrMore to Maybe❨Range❩`() {
        assertEquals(
            Maybe(OneOrMore(Range('a'..'y'))),
            regexp { Maybe(Range('a'..'y')).oneOrMore() }
        )
    }

    @Test
    fun `should apply oneOrMore to Maybe❨OneOrMore❨Row❩❩`() {
        assertEquals(
            Maybe(OneOrMore(Row("smth"))),
            regexp { Maybe(OneOrMore(Row("smth"))).oneOrMore() }
        )
    }

    @Test
    fun `should apply oneOrMore to Maybe❨Or❨Symbol，OneOrMore❨Row❩❩❩`() {
        assertEquals(
            Maybe(OneOrMore(Or(Symbol('k'), OneOrMore(Row("ghi"))))),
            regexp { Maybe(Or(Symbol('k'), OneOrMore(Row("ghi")))).oneOrMore() }
        )
    }
}

class DivTest {
    @Test
    fun `⌈BaseRegexp／BaseRegexp⌋ should apply div to Row and Or❨Symbol，Range❩`() {
        assertEquals(
            Or(Row("abc"), Symbol('f'), listOf(Range('x'..'z'))),
            regexp { Row("abc") / Or(Symbol('f'), Range('x'..'z')) }
        )
    }

    @Test
    fun `⌈BaseRegexp／BaseRegexp⌋ should apply div to Or❨Row，Row❩ and OneOrMore❨Symbol❩`() {
        assertEquals(
            Or(Row("abc"), Row("ghi"), listOf(OneOrMore(Symbol('k')))),
            regexp { Or(Row("abc"), Row("ghi")) / OneOrMore(Symbol('k')) }
        )
    }

    @Test
    fun `⌈BaseRegexp／BaseRegexp⌋ should apply div to Or❨Symbol，Row，OneOrMore❨Symbol❩❩ and Or❨OneOrMore❨Row❩，Range❩`() {
        assertEquals(
            Or(
                Symbol('s'),
                Row("row"),
                listOf(OneOrMore(Symbol('t')), OneOrMore(Row("second row")), Range('0'..'5'))
            ),
            regexp {
                Or(Symbol('s'), Row("row"), listOf(OneOrMore(Symbol('t')))) /
                        Or(OneOrMore(Row("second row")), Range('0'..'5'))
            }
        )
    }

    @Test
    fun `⌈BaseRegexp／BaseRegexp⌋ should apply div to Not❨Symbol❩ and Range`() {
        assertEquals(
            Or(Not(Symbol('a')), Range('f'..'i')),
            regexp { !'a' / Range('f'..'i') }
        )
    }

    @Test
    fun `⌈BaseRegexp／ε⌋ should apply div to Symbol and ε`() {
        assertEquals(
            Maybe(Symbol('x')),
            regexp { Symbol('x') / ε }
        )
    }

    @Test
    fun `⌈BaseRegexp／Maybe⌋ should apply div to Symbol and Maybe❨Or❨Row，Row，Symbol❩❩`() {
        assertEquals(
            Maybe(Or(Symbol('a'), Row("abc"), listOf(Row("def"), Symbol('b')))),
            regexp { Symbol('a') / Maybe(Or(Row("abc"), Row("def"), listOf(Symbol('b')))) }
        )
    }

    @Test
    fun `⌈ε／BaseRegexp⌋ should apply div to ε and Row`() {
        assertEquals(
            Maybe(Row("abc")),
            regexp { ε / Row("abc") }
        )
    }

    @Test
    fun `⌈ε／ε⌋ should apply div to ε and ε`() {
        assertEquals(
            ε,
            regexp { ε / ε }
        )
    }

    @Test
    fun `⌈ε／Maybe⌋ should apply div to ε and Maybe❨OneOrMore❨Range❩❩`() {
        assertEquals(
            Maybe(OneOrMore(Range('a'..'c'))),
            regexp { ε / Maybe(OneOrMore(Range('a'..'c'))) }
        )
    }

    @Test
    fun `⌈Maybe／Maybe⌋ should apply div to Maybe❨Or❨Row，Row❩❩ and Maybe❨Or❨Symbol，Symbol❩❩`() {
        assertEquals(
            Maybe(Or(Row("abc"), Row("xyz"), listOf(Symbol('p'), Symbol('h')))),
            regexp { Maybe(Or(Row("abc"), Row("xyz"))) / Maybe(Or(Symbol('p'), Symbol('h'))) }
        )
    }
}

class NotTest {
    @Test
    fun `should apply not to Range`() {
        assertEquals(
            Not(Range('a'..'c')),
            regexp { !('a'..'c') }
        )
    }

    @Test
    fun `should apply times to Not and Not`() {
        assertEquals(
            Not(Range('a'..'c'), listOf(Symbol('e'))),
            regexp { !('a'..'c') * !'e' }
        )
    }

    @Test
    fun `should apply times to 4 x Not`() {
        assertEquals(
            Not(Symbol('a'), listOf(Symbol('b'), Symbol('c'), Symbol('d'))),
            regexp { !'a' * !'b' * !'c' * !'d' }
        )
    }
}