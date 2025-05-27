package com.github.andrewkuryan.BNF

import kotlin.reflect.KProperty

sealed class GrammarSymbol

data class Nonterminal(val name: String, val origin: Nonterminal? = null) : GrammarSymbol() {
    override fun toString() = if (isSynthetic) "$name'" else name

    val isSynthetic = origin != null
}

data class Terminal(val value: Char) : GrammarSymbol() {
    override fun toString() = value.toString()
}

open class Production(val symbols: List<GrammarSymbol>) {
    val size = symbols.size

    fun first() = symbols.first()

    override fun toString() = symbols.joinToString(" ")
    override fun equals(other: Any?) = other is Production && symbols == other.symbols
    override fun hashCode() = symbols.hashCode()
}

abstract class AbstractGrammar<P : Production>(
    startSymbol: Nonterminal,
    productions: Map<Nonterminal, Set<P>> = mapOf(startSymbol to mutableSetOf()),
) {
    private var internalStartSymbol: Nonterminal = startSymbol
    private val internalProductions: MutableMap<Nonterminal, MutableSet<P>> = productions
        .mapValues { it.value.toMutableSet() }
        .toMutableMap()

    val startSymbol: Nonterminal
        get() = internalStartSymbol
    val productions: Map<Nonterminal, Set<P>>
        get() = internalProductions

    inner class NonterminalDelegate {
        operator fun getValue(thisRef: AbstractGrammar<P>?, property: KProperty<*>) = Nonterminal(property.name)
    }

    inner class StartDelegate {
        operator fun getValue(thisRef: AbstractGrammar<P>?, property: KProperty<*>) =
            Nonterminal(property.name).apply {
                internalStartSymbol = this
                internalProductions[internalStartSymbol] = mutableSetOf()
            }
    }

    fun nonterm() = NonterminalDelegate()
    fun start() = StartDelegate()

    protected abstract fun List<GrammarSymbol>.prod(): P
    abstract operator fun P.plus(symbol: GrammarSymbol): P
    abstract operator fun P.plus(other: P): P
    abstract fun P.drop(n: Int): P

    protected fun Char.prod() = listOf(Terminal(this)).prod()
    protected fun CharRange.prod() = listOf(Terminal(first), Terminal(last)).prod()
    protected fun RegExp.prod() = listOf(this).prod()
    protected fun Nonterminal.prod() = listOf(this).prod()

    private fun List<P>.addToFirst(production: P) = listOf(production + first()) + drop(1)
    private fun List<P>.addToLast(production: P) = take(size - 1) + listOf(last() + production)


    operator fun Nonterminal.divAssign(nontermProductions: List<P>) {
        internalProductions.getOrPut(this) { mutableSetOf() }.addAll(nontermProductions)
    }

    operator fun Nonterminal.divAssign(production: P) = divAssign(listOf(production))
    operator fun Nonterminal.divAssign(char: Char) = divAssign(char.prod())
    operator fun Nonterminal.divAssign(regexp: RegExp) = divAssign(regexp.prod())
    operator fun Nonterminal.divAssign(range: CharRange) = divAssign(range.prod())
    operator fun Nonterminal.divAssign(nonterm: Nonterminal) = divAssign(nonterm.prod())


    operator fun Char.rangeTo(regexp: RegExp) = listOf(prod() + regexp)
    operator fun Char.rangeTo(nonterm: Nonterminal) = listOf(prod() + nonterm)
    operator fun Char.rangeTo(production: P) = prod() + production
    operator fun Char.rangeTo(productions: List<P>) = productions.addToFirst(prod())

    operator fun RegExp.rangeTo(char: Char) = listOf(prod() + Terminal(char))
    operator fun RegExp.rangeTo(regexp: RegExp) = listOf(prod() + regexp)
    operator fun RegExp.rangeTo(nonterm: Nonterminal) = listOf(prod() + nonterm)
    operator fun RegExp.rangeTo(production: P) = prod() + production
    operator fun RegExp.rangeTo(productions: List<P>) = productions.addToFirst(prod())

    operator fun CharRange.rangeTo(char: Char) = listOf(prod() + Terminal(char))
    operator fun CharRange.rangeTo(regexp: RegExp) = listOf(prod() + regexp)
    operator fun CharRange.rangeTo(nonterm: Nonterminal) = listOf(prod() + nonterm)
    operator fun CharRange.rangeTo(production: P) = listOf(prod() + production)
    operator fun CharRange.rangeTo(productions: List<P>) = productions.addToFirst(prod())

    operator fun Nonterminal.rangeTo(char: Char) = listOf(prod() + Terminal(char))
    operator fun Nonterminal.rangeTo(regexp: RegExp) = listOf(prod() + regexp)
    operator fun Nonterminal.rangeTo(nonterm: Nonterminal) = listOf(prod() + nonterm)
    operator fun Nonterminal.rangeTo(production: P) = listOf(prod() + production)
    operator fun Nonterminal.rangeTo(productions: List<P>) = productions.addToFirst(prod())

    operator fun List<P>.rangeTo(char: Char) = addToLast(char.prod())
    operator fun List<P>.rangeTo(regexp: RegExp) = addToLast(regexp.prod())
    operator fun List<P>.rangeTo(nonterm: Nonterminal) = addToLast(nonterm.prod())
    operator fun List<P>.rangeTo(production: P) = addToLast(production)
    operator fun List<P>.rangeTo(productions: List<P>) = addToLast(productions.first()) + productions.drop(1)


    operator fun Char.div(char: Char) = listOf(prod(), char.prod())
    operator fun Char.div(nonterm: Nonterminal) = listOf(prod(), nonterm.prod())
    operator fun Char.div(regexp: RegExp) = listOf(prod(), regexp.prod())
    operator fun Char.div(production: P) = listOf(prod(), production)

    operator fun RegExp.div(char: Char) = listOf(prod(), char.prod())
    operator fun RegExp.div(nonterm: Nonterminal) = listOf(prod(), nonterm.prod())
    operator fun RegExp.div(regexp: RegExp) = listOf(prod(), regexp.prod())
    operator fun RegExp.div(production: P) = listOf(prod(), production)

    operator fun CharRange.div(char: Char) = listOf(prod(), char.prod())
    operator fun CharRange.div(nonterm: Nonterminal) = listOf(prod(), nonterm.prod())
    operator fun CharRange.div(regexp: RegExp) = listOf(prod(), regexp.prod())
    operator fun CharRange.div(production: P) = listOf(prod(), production)

    operator fun Nonterminal.div(char: Char) = listOf(prod(), char.prod())
    operator fun Nonterminal.div(nonterm: Nonterminal) = listOf(prod(), nonterm.prod())
    operator fun Nonterminal.div(regexp: RegExp) = listOf(prod(), regexp.prod())
    operator fun Nonterminal.div(production: P) = listOf(prod(), production)

    operator fun P.div(char: Char) = listOf(this, char.prod())
    operator fun P.div(regexp: RegExp) = listOf(this, regexp.prod())
    operator fun P.div(nonterm: Nonterminal) = listOf(this, nonterm.prod())
    operator fun P.div(production: P) = listOf(this, production)

    operator fun List<P>.div(char: Char) = this + listOf(char.prod())
    operator fun List<P>.div(regexp: RegExp) = this + listOf(regexp.prod())
    operator fun List<P>.div(nonterm: Nonterminal) = this + listOf(nonterm.prod())
    operator fun List<P>.div(production: P) = this + listOf(production)


    private fun Set<P>.format(head: Nonterminal) = head.toString() + " ::= " + joinToString(" | ")

    override fun toString() = productions.getValue(startSymbol).format(startSymbol) +
            (productions - startSymbol).entries.joinToString("\n", prefix = "\n") { it.value.format(it.key) }
}

class Grammar(
    startSymbol: Nonterminal = S,
    productions: Map<Nonterminal, Set<Production>> = mapOf(startSymbol to mutableSetOf()),
) : AbstractGrammar<Production>(startSymbol, productions) {

    companion object {
        val S = Nonterminal("S")
    }

    override fun List<GrammarSymbol>.prod() = Production(this)
    override fun Production.plus(other: Production) = Production(symbols + other.symbols)
    override fun Production.plus(symbol: GrammarSymbol) = Production(symbols + symbol)
    override fun Production.drop(n: Int) = Production(symbols.drop(n))
}

fun grammar(builder: Grammar.() -> Unit) = Grammar().apply(builder)