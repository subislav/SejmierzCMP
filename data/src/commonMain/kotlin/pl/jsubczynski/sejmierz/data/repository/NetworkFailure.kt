package pl.jsubczynski.sejmierz.data.repository

import kotlinx.io.IOException

internal fun isNetworkFailure(t: Throwable): Boolean {
    var current: Throwable? = t
    while (current != null) {
        if (current is IOException) return true
        if (current::class.qualifiedName == "java.io.IOException") return true
        current = current.cause
    }
    return false
}
