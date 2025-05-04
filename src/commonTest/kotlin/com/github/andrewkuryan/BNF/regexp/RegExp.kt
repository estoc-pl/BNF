package com.github.andrewkuryan.BNF.regexp

import com.github.andrewkuryan.BNF.regexp
import kotlin.test.Test
import kotlin.test.assertEquals
import com.github.andrewkuryan.BNF.RegExp.ε

class ToStringTest {
    @Test
    fun `should print range`() {
        val r = regexp('a'..'f')

        assertEquals("[a-f]", r.toString())
    }

    @Test
    fun `⌈OneOrMore⌋ should print［0-6］+`() {
        val r = regexp { regexp('0'..'6').oneOrMore() }

        assertEquals("[0-6]+", r.toString())
    }

    @Test
    fun `⌈Or⌋ should print［012X-Zax-zdt］`() {
        val r = regexp { '0' / '1' / '2' / ('X'..'Z') / 'a' / ('x'..'z') / 'd' / 't' }

        assertEquals("[012X-Zax-zdt]", r.toString())
    }

    @Test
    fun `⌈Or⌋ should print ❨abc ⏐ k-l ⏐［b-e］+❩`() {
        val r = regexp { "abc" / ('k'..'l') / ('b'..'e').oneOrMore() }

        assertEquals("(abc|[k-l]|[b-e]+)", r.toString())
    }

    @Test
    fun `⌈Or⌋ should print ❨a ⏐［b-d］⏐［ˆx-z］❩`() {
        val r = regexp { 'a' / ('b'..'d') / !('x'..'z') }

        assertEquals("(a|[b-d]|[^x-z])", r.toString())
    }

    @Test
    fun `⌈Or⌋ should print ❨abc ⏐［ˆg-i］+❩`() {
        val r = regexp { "abc" / (!('g'..'i')).oneOrMore() }

        assertEquals("(abc|[^g-i]+)", r.toString())
    }

    @Test
    fun `⌈Maybe⌋ should print ❨abc ⏐ f❩？`() {
        val r = regexp { "abc" / 'f' / ε }

        assertEquals("(abc|f)?", r.toString())
    }

    @Test
    fun `⌈Maybe⌋ should print ［a-c］＊`() {
        val r = regexp { ('a'..'c').oneOrMore().maybe() }

        assertEquals("[a-c]*", r.toString())
    }

    @Test
    fun `⌈Not⌋ should print ［ˆa-cfgh］`() {
        val r = regexp { !('a'..'c') * !'f' * !'g' * !'h' }

        assertEquals("[^a-cfgh]", r.toString())
    }
}