package pl.jsubczynski.sejmierz.data.repository

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import pl.jsubczynski.sejmierz.data.local.VotingLocalDataSource
import pl.jsubczynski.sejmierz.data.mapper.toDetailDomain
import pl.jsubczynski.sejmierz.data.mapper.toDomain
import pl.jsubczynski.sejmierz.data.remote.ApiHttpException
import pl.jsubczynski.sejmierz.data.remote.SejmApi
import pl.jsubczynski.sejmierz.domain.error.DomainError
import pl.jsubczynski.sejmierz.domain.error.DomainResult
import pl.jsubczynski.sejmierz.domain.model.DataSource
import pl.jsubczynski.sejmierz.domain.model.Page
import pl.jsubczynski.sejmierz.domain.model.Term
import pl.jsubczynski.sejmierz.domain.model.Voting
import pl.jsubczynski.sejmierz.domain.model.VotingDetail
import pl.jsubczynski.sejmierz.domain.repository.VotingRepository

class VotingRepositoryImpl(
    private val api: SejmApi,
    private val local: VotingLocalDataSource,
    private val cacheScope: CoroutineScope,
) : VotingRepository {

    override suspend fun getVotings(
        term: Term,
        source: DataSource,
        offset: Int,
        limit: Int,
    ): DomainResult<Page<Voting>> = when (source) {
        DataSource.NETWORK -> safeCall {
            val votings = api.searchVotings(
                term = term.number,
                offset = offset,
                limit = limit,
                dateFrom = DEFAULT_DATE_FROM,
                dateTo = DEFAULT_DATE_TO,
            ).map { it.toDomain() }
            cacheScope.launch { runCatching { local.upsertAll(term.number, votings) } }
            Page(items = votings, offset = offset, limit = limit)
        }

        DataSource.LOCAL -> safeCall {
            val votings = local.readPage(term.number, offset, limit)
            Page(items = votings, offset = offset, limit = limit)
        }.emptyAsNotCached(offset)
    }

    override suspend fun getVoting(
        term: Term,
        sitting: Int,
        votingNumber: Int,
        source: DataSource,
    ): DomainResult<VotingDetail> = when (source) {
        DataSource.NETWORK -> safeCall {
            val dto = api.getVoting(term.number, sitting, votingNumber)
            val detail = dto.toDetailDomain()
            cacheScope.launch { runCatching { local.upsertAll(term.number, listOf(detail.voting)) } }
            detail
        }

        DataSource.LOCAL -> {
            when (val cached = safeCall { local.readById(term.number, sitting, votingNumber) }) {
                is DomainResult.Success ->
                    cached.data?.let { DomainResult.success(VotingDetail(it, emptyList())) }
                        ?: DomainResult.failure(DomainError.NotCached)
                is DomainResult.Failure -> cached
            }
        }
    }


    private fun DomainResult<Page<Voting>>.emptyAsNotCached(offset: Int): DomainResult<Page<Voting>> =
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

    private companion object {
        const val DEFAULT_DATE_FROM = "2024-01-01"
        const val DEFAULT_DATE_TO = "2024-12-31"
    }
}
