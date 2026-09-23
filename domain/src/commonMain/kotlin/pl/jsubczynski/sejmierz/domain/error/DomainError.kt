package pl.jsubczynski.sejmierz.domain.error

sealed interface DomainError {

    data class Network(val cause: String? = null) : DomainError


    data class Http(val code: Int, val message: String? = null) : DomainError


    data class Serialization(val cause: String? = null) : DomainError


    data object NotCached : DomainError


    data object Empty : DomainError


    data class Unknown(val cause: String? = null) : DomainError
}
