package pl.jsubczynski.sejmierz.data.repository

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import pl.jsubczynski.sejmierz.data.local.MpLocalDataSource
import pl.jsubczynski.sejmierz.data.mapper.toDomain
import pl.jsubczynski.sejmierz.data.remote.ApiHttpException
import pl.jsubczynski.sejmierz.data.remote.SejmApi
import pl.jsubczynski.sejmierz.domain.error.DomainError
import pl.jsubczynski.sejmierz.domain.error.DomainResult
import pl.jsubczynski.sejmierz.domain.model.DataSource
import pl.jsubczynski.sejmierz.domain.model.Mp
import pl.jsubczynski.sejmierz.domain.model.Page
import pl.jsubczynski.sejmierz.domain.model.Term
import pl.jsubczynski.sejmierz.domain.repository.MpRepository

class MpRepositoryImpl(
    private val api: SejmApi,
    private val local: MpLocalDataSource,
    private val cacheScope: CoroutineScope,
) : MpRepository {

    override suspend fun getMps(
        term: Term,
        source: DataSource,
        offset: Int,
        limit: Int,
    ): DomainResult<Page<Mp>> = when (source) {
        DataSource.NETWORK -> safeCall {
            val mps = api.getMps(term.number, offset, limit).map { it.toDomain() }
            cacheScope.launch { runCatching { local.upsertAll(term.number, mps) } }
            Page(items = mps, offset = offset, limit = limit)
        }

        DataSource.LOCAL -> safeCall {
            val mps = local.readPage(term.number, offset, limit)
            Page(items = mps, offset = offset, limit = limit)
        }.emptyAsNotCached(offset)
    }

    override suspend fun getMp(
        term: Term,
        id: Int,
        source: DataSource,
    ): DomainResult<Mp> = when (source) {
        DataSource.NETWORK -> safeCall {
            val mp = api.getMp(term.number, id).toDomain()
            cacheScope.launch { runCatching { local.upsertAll(term.number, listOf(mp)) } }
            mp
        }

        DataSource.LOCAL -> {
            when (val cached = safeCall { local.readById(term.number, id) }) {
                is DomainResult.Success ->
                    cached.data?.let { DomainResult.success(it) }
                        ?: DomainResult.failure(DomainError.NotCached)
                is DomainResult.Failure -> cached
            }
        }
    }


    private fun DomainResult<Page<Mp>>.emptyAsNotCached(offset: Int): DomainResult<Page<Mp>> =
        if (this is DomainResult.Success && data.items.isEmpty() && offset == 0) {
            DomainResult.failure(DomainError.NotCached)
        } else {
            this
        }

    private inline fun <T> safeCall(block: () -> T): DomainResult<T> = try {
        DomainResult.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: ApiHttpException) {
        DomainResult.failure(DomainError.Http(e.code, e.message))
    } catch (e: SerializationException) {
        DomainResult.failure(DomainError.Serialization(e.message))
    } catch (e: Throwable) {
        if (isNetworkFailure(e)) {
            DomainResult.failure(DomainError.Network(e.message))
        } else {
            DomainResult.failure(DomainError.Unknown(e.message))
        }
    }
}
