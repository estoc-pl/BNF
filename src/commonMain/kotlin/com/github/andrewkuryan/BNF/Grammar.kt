package com.github.andrewkuryan.BNF

import kotlin.reflect.KFunction2
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0

sealed class GrammarSymbol

data class Nonterminal(val name: String, val origin: Nonterminal? = null) : GrammarSymbol() {
    override fun toString() = if (isSynthetic) "$name'" else name

    val isSynthetic = origin != null
}

data class Terminal(val value: Char) : GrammarSymbol() {
    override fun toString() = value.toString()
}

data class Production<N : SyntaxNode>(val symbols: List<GrammarSymbol>, val action: SemanticAction<N>? = null) {
    val size = symbols.size

    fun first() = symbols.first()
    fun drop(n: Int) = Production(symbols.drop(n), action)

    operator fun plus(symbol: GrammarSymbol) = Production(symbols + symbol, action)
    operator fun plus(other: Production<N>) = Production(symbols + other.symbols, other.action ?: action)

    override fun toString() = symbols.joinToString(" ")
}

class Grammar<N : SyntaxNode>(
    startSymbol: Nonterminal = S,
    productions: Map<Nonterminal, Set<Production<N>>> = mapOf(startSymbol to mutableSetOf()),
) {
    companion object {
        val S = Nonterminal("S")
    }

    private var internalStartSymbol: Nonterminal = startSymbol
    private val internalProductions: MutableMap<Nonterminal, MutableSet<Production<N>>> = productions
        .mapValues { it.value.toMutableSet() }
        .toMutableMap()

    val startSymbol: Nonterminal
        get() = internalStartSymbol
    val productions: Map<Nonterminal, Set<Production<N>>>
        get() = internalProductions

    inner class NonterminalDelegate {
        operator fun getValue(thisRef: Grammar<N>?, property: KProperty<*>) = Nonterminal(property.name)
    }

    inner class StartDelegate {
        operator fun getValue(thisRef: Grammar<N>?, property: KProperty<*>) =
            Nonterminal(property.name).apply {
                internalStartSymbol = this
                internalProductions[internalStartSymbol] = mutableSetOf()
            }
    }

    fun nonterm() = NonterminalDelegate()
    fun start() = StartDelegate()

    private fun Char.prod() = Production<N>(listOf(Terminal(this)))
    private fun CharRange.prod() = Production<N>(listOf(Terminal(first), Terminal(last)))
    private fun Nonterminal.prod() = Production<N>(listOf(this))

    private fun List<Production<N>>.addToFirst(production: Production<N>) = listOf(production + first()) + drop(1)
    private fun List<Production<N>>.addToLast(production: Production<N>) = take(size - 1) + listOf(last() + production)


    operator fun Nonterminal.divAssign(nontermProductions: List<Production<N>>) {
        internalProductions.getOrPut(this) { mutableSetOf() }.addAll(nontermProductions)
    }

    operator fun Nonterminal.divAssign(production: Production<N>) = divAssign(listOf(production))
    operator fun Nonterminal.divAssign(char: Char) = divAssign(char.prod())
    operator fun Nonterminal.divAssign(range: CharRange) = divAssign(range.prod())
    operator fun Nonterminal.divAssign(nonterm: Nonterminal) = divAssign(nonterm.prod())


    operator fun Char.rangeTo(nonterm: Nonterminal) = listOf(prod() + nonterm)
    operator fun Char.rangeTo(production: Production<N>) = prod() + production
    operator fun Char.rangeTo(productions: List<Production<N>>) = productions.addToFirst(prod())

    operator fun CharRange.rangeTo(char: Char) = listOf(prod() + Terminal(char))
    operator fun CharRange.rangeTo(nonterm: Nonterminal) = listOf(prod() + nonterm)
    operator fun CharRange.rangeTo(production: Production<N>) = listOf(prod() + production)
    operator fun CharRange.rangeTo(productions: List<Production<N>>) = productions.addToFirst(prod())

    operator fun Nonterminal.rangeTo(char: Char) = listOf(prod() + Terminal(char))
    operator fun Nonterminal.rangeTo(nonterm: Nonterminal) = listOf(prod() + nonterm)
    operator fun Nonterminal.rangeTo(production: Production<N>) = listOf(prod() + production)
    operator fun Nonterminal.rangeTo(productions: List<Production<N>>) = productions.addToFirst(prod())

    operator fun List<Production<N>>.rangeTo(char: Char) = addToLast(char.prod())
    operator fun List<Production<N>>.rangeTo(nonterm: Nonterminal) = addToLast(nonterm.prod())
    operator fun List<Production<N>>.rangeTo(production: Production<N>) = addToLast(production)
    operator fun List<Production<N>>.rangeTo(productions: List<Production<N>>) =
        addToLast(productions.first()) + productions.drop(1)


    operator fun Char.div(char: Char) = listOf(prod(), char.prod())
    operator fun Char.div(nonterm: Nonterminal) = listOf(prod(), nonterm.prod())
    operator fun Char.div(production: Production<N>) = listOf(prod(), production)

    operator fun CharRange.div(char: Char) = listOf(prod(), char.prod())
    operator fun CharRange.div(nonterm: Nonterminal) = listOf(prod(), nonterm.prod())
    operator fun CharRange.div(production: Production<N>) = listOf(prod(), production)

    operator fun Nonterminal.div(char: Char) = listOf(prod(), char.prod())
    operator fun Nonterminal.div(nonterm: Nonterminal) = listOf(prod(), nonterm.prod())
    operator fun Nonterminal.div(production: Production<N>) = listOf(prod(), production)

    operator fun Production<N>.div(char: Char) = listOf(this, char.prod())
    operator fun Production<N>.div(nonterm: Nonterminal) = listOf(this, nonterm.prod())
    operator fun Production<N>.div(production: Production<N>) = listOf(this, production)

    operator fun List<Production<N>>.div(char: Char) = this + listOf(char.prod())
    operator fun List<Production<N>>.div(nonterm: Nonterminal) = this + listOf(nonterm.prod())
    operator fun List<Production<N>>.div(production: Production<N>) = this + listOf(production)


    operator fun Production<N>.invoke(fn: KProperty0<(N, List<N>) -> Unit>) = invoke(SemanticAction(fn.name, fn.get()))
    operator fun Production<N>.invoke(fn: KFunction2<N, List<N>, Unit>) = invoke(SemanticAction(fn.name, fn))
    operator fun Char.invoke(fn: KProperty0<(N, List<N>) -> Unit>) = prod().invoke(fn)
    operator fun Char.invoke(fn: KFunction2<N, List<N>, Unit>) = prod().invoke(fn)
    operator fun CharRange.invoke(fn: KProperty0<(N, List<N>) -> Unit>) = prod().invoke(fn)
    operator fun CharRange.invoke(fn: KFunction2<N, List<N>, Unit>) = prod().invoke(fn)
    operator fun Nonterminal.invoke(fn: KProperty0<(N, List<N>) -> Unit>) = prod().invoke(fn)
    operator fun Nonterminal.invoke(fn: KFunction2<N, List<N>, Unit>) = prod().invoke(fn)

    operator fun Production<N>.invoke(action: SemanticAction<N>) = Production(symbols, action)
    operator fun Char.invoke(action: SemanticAction<N>) = prod().invoke(action)
    operator fun CharRange.invoke(action: SemanticAction<N>) = prod().invoke(action)
    operator fun Nonterminal.invoke(action: SemanticAction<N>) = prod().invoke(action)


    private fun Set<Production<N>>.format(head: Nonterminal) = head.toString() + " ::= " + joinToString(" | ")

    override fun toString() = productions.getValue(startSymbol).format(startSymbol) +
            (productions - startSymbol).entries.joinToString("\n", prefix = "\n") { it.value.format(it.key) }
}

fun grammar(builder: Grammar<SyntaxNode>.() -> Unit) = Grammar<SyntaxNode>().apply(builder)
fun <N : SyntaxNode> grammar(builder: Grammar<N>.() -> Unit) = Grammar<N>().apply(builder)