package pl.jsubczynski.sejmierz.domain.model

import kotlin.jvm.JvmInline

@JvmInline
value class Term(val number: Int) {
    init {
        require(number > 0) { "Term number must be positive, was $number" }
    }

    companion object {
        val CURRENT = Term(10)
    }
}
