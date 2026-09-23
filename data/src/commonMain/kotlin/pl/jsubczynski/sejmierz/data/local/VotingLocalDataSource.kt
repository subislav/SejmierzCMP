package pl.jsubczynski.sejmierz.data.local

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pl.jsubczynski.sejmierz.data.db.SejmierzDatabase
import pl.jsubczynski.sejmierz.data.mapper.toDomain
import pl.jsubczynski.sejmierz.domain.benchmark.Benchmark
import pl.jsubczynski.sejmierz.domain.benchmark.MetricCategory
import pl.jsubczynski.sejmierz.domain.benchmark.MetricTag
import pl.jsubczynski.sejmierz.domain.model.Voting

class VotingLocalDataSource(
    private val database: SejmierzDatabase,
    private val benchmark: Benchmark,
) {
    private val queries get() = database.votingEntityQueries

    suspend fun readPage(term: Int, offset: Int, limit: Int): List<Voting> =
        benchmark.measured(
            category = MetricCategory.DB,
            name = "db.read.page",
            tags = mapOf(MetricTag.SLICE to "Votings"),
        ) {
            withContext(Dispatchers.Default) {
                queries.selectPage(
                    term = term.toLong(),
                    limit = limit.toLong(),
                    offset = offset.toLong(),
                ).executeAsList().map { it.toDomain() }
            }
        }

    suspend fun readById(term: Int, sitting: Int, votingNumber: Int): Voting? =
        benchmark.measured(
            category = MetricCategory.DB,
            name = "db.read.byId",
            tags = mapOf(MetricTag.SLICE to "Votings"),
        ) {
            withContext(Dispatchers.Default) {
                queries.selectById(
                    term = term.toLong(),
                    sitting = sitting.toLong(),
                    votingNumber = votingNumber.toLong(),
                ).executeAsOneOrNull()?.toDomain()
            }
        }

    suspend fun count(term: Int): Long = withContext(Dispatchers.Default) {
        queries.countByTerm(term.toLong()).executeAsOne()
    }


    suspend fun upsertAll(term: Int, votings: List<Voting>) {
        benchmark.measured(
            category = MetricCategory.DB,
            name = "db.bulkInsert",
            tags = mapOf(
                MetricTag.SLICE to "Votings",
                "rows" to votings.size.toString(),
            ),
        ) {
            withContext(Dispatchers.Default) {
                queries.transaction {
                    votings.forEach { voting ->
                        queries.upsert(
                            term = term.toLong(),
                            sitting = voting.sitting.toLong(),
                            votingNumber = voting.votingNumber.toLong(),
                            sittingDay = voting.sittingDay?.toLong(),
                            date = voting.date,
                            title = voting.title,
                            topic = voting.topic,
                            description = voting.description,
                            kind = voting.kind,
                            majorityType = voting.majorityType,
                            yes = voting.yes.toLong(),
                            no = voting.no.toLong(),
                            abstain = voting.abstain.toLong(),
                            notParticipating = voting.notParticipating.toLong(),
                            totalVoted = voting.totalVoted.toLong(),
                        )
                    }
                }
            }
        }
    }
}
