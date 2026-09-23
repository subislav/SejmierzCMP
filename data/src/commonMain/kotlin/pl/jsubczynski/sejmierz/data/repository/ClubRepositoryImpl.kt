package pl.jsubczynski.sejmierz.data.repository

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import pl.jsubczynski.sejmierz.data.local.ClubLocalDataSource
import pl.jsubczynski.sejmierz.data.mapper.toDetailDomain
import pl.jsubczynski.sejmierz.data.mapper.toDomain
import pl.jsubczynski.sejmierz.data.remote.ApiHttpException
import pl.jsubczynski.sejmierz.data.remote.SejmApi
import pl.jsubczynski.sejmierz.domain.error.DomainError
import pl.jsubczynski.sejmierz.domain.error.DomainResult
import pl.jsubczynski.sejmierz.domain.model.Club
import pl.jsubczynski.sejmierz.domain.model.ClubDetail
import pl.jsubczynski.sejmierz.domain.model.DataSource
import pl.jsubczynski.sejmierz.domain.model.Page
import pl.jsubczynski.sejmierz.domain.model.Term
import pl.jsubczynski.sejmierz.domain.repository.ClubRepository

class ClubRepositoryImpl(
    private val api: SejmApi,
    private val local: ClubLocalDataSource,
    private val cacheScope: CoroutineScope,
) : ClubRepository {

    override suspend fun getClubs(
        term: Term,
        source: DataSource,
        offset: Int,
        limit: Int,
    ): DomainResult<Page<Club>> = when (source) {
        DataSource.NETWORK -> safeCall {
            if (offset > 0) {
                Page(items = emptyList(), offset = offset, limit = limit)
            } else {
                val clubs = api.getClubs(term.number).map { it.toDomain() }
                cacheScope.launch { runCatching { local.upsertAll(term.number, clubs) } }
                Page(items = clubs, offset = offset, limit = limit)
            }
        }

        DataSource.LOCAL -> safeCall {
            val clubs = local.readPage(term.number, offset, limit)
            Page(items = clubs, offset = offset, limit = limit)
        }.emptyAsNotCached(offset)
    }

    override suspend fun getClub(
        term: Term,
        id: String,
        source: DataSource,
    ): DomainResult<ClubDetail> = when (source) {
        DataSource.NETWORK -> safeCall {
            val dto = api.getClub(term.number, id)
            val detail = dto.toDetailDomain()
            cacheScope.launch { runCatching { local.upsertAll(term.number, listOf(detail.club)) } }
            detail
        }

        DataSource.LOCAL -> {
            when (val cached = safeCall { local.readById(term.number, id) }) {
                is DomainResult.Success ->
                    cached.data?.let { DomainResult.success(ClubDetail(it, emptyList())) }
                        ?: DomainResult.failure(DomainError.NotCached)
                is DomainResult.Failure -> cached
            }
        }
    }


    private fun DomainResult<Page<Club>>.emptyAsNotCached(offset: Int): DomainResult<Page<Club>> =
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
