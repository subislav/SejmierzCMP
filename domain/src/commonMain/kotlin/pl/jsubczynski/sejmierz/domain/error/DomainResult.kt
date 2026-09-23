package pl.jsubczynski.sejmierz.domain.error

sealed interface DomainResult<out T> {
    data class Success<T>(val data: T) : DomainResult<T>
    data class Failure(val error: DomainError) : DomainResult<Nothing>

    val isSuccess: Boolean get() = this is Success

    fun getOrNull(): T? = (this as? Success)?.data

    companion object {
        fun <T> success(data: T): DomainResult<T> = Success(data)
        fun failure(error: DomainError): DomainResult<Nothing> = Failure(error)
    }
}

inline fun <T, R> DomainResult<T>.map(transform: (T) -> R): DomainResult<R> = when (this) {
    is DomainResult.Success -> DomainResult.Success(transform(data))
    is DomainResult.Failure -> this
}

inline fun <T> DomainResult<T>.onSuccess(action: (T) -> Unit): DomainResult<T> {
    if (this is DomainResult.Success) action(data)
    return this
}

inline fun <T> DomainResult<T>.onFailure(action: (DomainError) -> Unit): DomainResult<T> {
    if (this is DomainResult.Failure) action(error)
    return this
}
