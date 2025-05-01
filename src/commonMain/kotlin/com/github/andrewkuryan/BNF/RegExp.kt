package com.github.andrewkuryan.BNF

sealed interface AtomicRegexp : EnumerableRegexp, RepeatableRegexp
sealed interface RepeatableRegexp : BaseRegexp
sealed interface EnumerableRegexp : BaseRegexp {
    fun toStringNested(): String
}

sealed interface BaseRegexp

sealed class RegExp {
    data object ε : RegExp()

    data class Symbol(val value: Char) : RegExp(), AtomicRegexp {
        override fun toStringNested() = value.toString()

        override fun toString() = toStringNested()
    }

    data class Row(val value: String) : RegExp(), AtomicRegexp {
        override fun toStringNested() = value

        override fun toString() = toStringNested()
    }

    data class Range(val value: CharRange) : RegExp(), AtomicRegexp {
        override fun toStringNested() = "${value.first}-${value.last}"

        override fun toString() = "[${toStringNested()}]"
    }

    data class OneOrMore(val value: RepeatableRegexp) : RegExp(), EnumerableRegexp {
        override fun toStringNested() = "$value+"

        override fun toString() = toStringNested()
    }

    data class Or(
        val first: EnumerableRegexp,
        val second: EnumerableRegexp,
        val rest: List<EnumerableRegexp> = listOf(),
    ) : RegExp(), RepeatableRegexp {
        override fun toString() = (listOf(first, second) + rest).let { exps ->
            when {
                exps.all { it is Symbol || it is Range } ->
                    exps.joinToString("", "[", "]") { it.toStringNested() }

                else -> exps.joinToString("|", "(", ")") { it.toString() }
            }
        }
    }

    data class Maybe(val value: BaseRegexp) : RegExp() {
        override fun toString() = when (value) {
            is OneOrMore -> "${value.value}*"
            else -> "$value?"
        }
    }
}

class RegExpBuilder {
    operator fun Char.div(other: Char) = regexp(this).div(other)
    operator fun Char.div(other: String) = regexp(this).div(other)
    operator fun Char.div(other: CharRange) = regexp(this).div(other)
    operator fun Char.div(other: RegExp) = regexp(this).div(other)

    operator fun String.div(other: Char) = regexp(this).div(other)
    operator fun String.div(other: String) = regexp(this).div(other)
    operator fun String.div(other: CharRange) = regexp(this).div(other)
    operator fun String.div(other: RegExp) = regexp(this).div(other)

    operator fun CharRange.div(other: Char) = regexp(this).div(other)
    operator fun CharRange.div(other: String) = regexp(this).div(other)
    operator fun CharRange.div(other: CharRange) = regexp(this).div(other)
    operator fun CharRange.div(other: RegExp) = regexp(this).div(other)

    operator fun RegExp.div(other: Char) = this.div(regexp(other))
    operator fun RegExp.div(other: String) = this.div(regexp(other))
    operator fun RegExp.div(other: CharRange) = this.div(regexp(other))

    private fun BaseRegexp.baseDiv(other: BaseRegexp): BaseRegexp = when (this) {
        is RegExp.Or -> when (other) {
            is RegExp.Or -> this.copy(rest = rest + listOf(other.first, other.second) + other.rest)
            is EnumerableRegexp -> this.copy(rest = rest + other)
        }

        is EnumerableRegexp -> when (other) {
            is RegExp.Or -> RegExp.Or(this, other.first, listOf(other.second) + other.rest)
            is EnumerableRegexp -> RegExp.Or(this, other)
        }
    }

    operator fun RegExp.div(other: RegExp): RegExp = when (this) {
        is BaseRegexp -> when (other) {
            is BaseRegexp -> this.baseDiv(other) as RegExp
            is RegExp.ε -> RegExp.Maybe(this)
            is RegExp.Maybe -> RegExp.Maybe(this.baseDiv(other.value))
        }

        is RegExp.ε -> when (other) {
            is BaseRegexp -> RegExp.Maybe(other)
            is RegExp.ε -> RegExp.ε
            is RegExp.Maybe -> other
        }

        is RegExp.Maybe -> when (other) {
            is BaseRegexp -> RegExp.Maybe(this.value.baseDiv(other))
            is RegExp.ε -> this
            is RegExp.Maybe -> RegExp.Maybe(this.value.baseDiv(other.value))
        }
    }


    fun Char.oneOrMore() = regexp(this).oneOrMore()
    fun String.oneOrMore() = regexp(this).oneOrMore()
    fun CharRange.oneOrMore() = regexp(this).oneOrMore()

    private fun BaseRegexp.baseOneOrMore(): BaseRegexp = when (this) {
        is RegExp.OneOrMore -> this
        is RepeatableRegexp -> RegExp.OneOrMore(this)
    }

    fun RegExp.oneOrMore(): RegExp = when (this) {
        is BaseRegexp -> baseOneOrMore() as RegExp
        is RegExp.ε -> RegExp.ε
        is RegExp.Maybe -> RegExp.Maybe(value.baseOneOrMore())
    }


    fun Char.maybe() = regexp(this).maybe()
    fun String.maybe() = regexp(this).maybe()
    fun CharRange.maybe() = regexp(this).maybe()
    fun RegExp.maybe(): RegExp = when (this) {
        is BaseRegexp -> RegExp.Maybe(this)
        is RegExp.ε -> RegExp.ε
        is RegExp.Maybe -> this
    }
}

fun regexp(builder: RegExpBuilder.() -> RegExp): RegExp = RegExpBuilder().run(builder)
fun regexp(pattern: Char): RegExp = RegExp.Symbol(pattern)
fun regexp(pattern: String): RegExp = when (pattern.length) {
    0 -> RegExp.ε
    1 -> RegExp.Symbol(pattern[0])
    else -> RegExp.Row(pattern)
}

fun regexp(pattern: CharRange): RegExp = RegExp.Range(pattern)