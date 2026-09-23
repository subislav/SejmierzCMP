package pl.jsubczynski.sejmierz.data.local

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pl.jsubczynski.sejmierz.data.db.SejmierzDatabase
import pl.jsubczynski.sejmierz.data.mapper.toDomain
import pl.jsubczynski.sejmierz.domain.benchmark.Benchmark
import pl.jsubczynski.sejmierz.domain.benchmark.MetricCategory
import pl.jsubczynski.sejmierz.domain.benchmark.MetricTag
import pl.jsubczynski.sejmierz.domain.model.Club

class ClubLocalDataSource(
    private val database: SejmierzDatabase,
    private val benchmark: Benchmark,
) {
    private val queries get() = database.clubEntityQueries

    suspend fun readPage(term: Int, offset: Int, limit: Int): List<Club> =
        benchmark.measured(
            category = MetricCategory.DB,
            name = "db.read.page",
            tags = mapOf(MetricTag.SLICE to "Clubs"),
        ) {
            withContext(Dispatchers.IO) {
                queries.selectPage(
                    term = term.toLong(),
                    limit = limit.toLong(),
                    offset = offset.toLong(),
                ).executeAsList().map { it.toDomain() }
            }
        }

    suspend fun readById(term: Int, id: String): Club? =
        benchmark.measured(
            category = MetricCategory.DB,
            name = "db.read.byId",
            tags = mapOf(MetricTag.SLICE to "Clubs"),
        ) {
            withContext(Dispatchers.IO) {
                queries.selectById(term = term.toLong(), id = id)
                    .executeAsOneOrNull()
                    ?.toDomain()
            }
        }

    suspend fun count(term: Int): Long = withContext(Dispatchers.IO) {
        queries.countByTerm(term.toLong()).executeAsOne()
    }


    suspend fun upsertAll(term: Int, clubs: List<Club>) {
        benchmark.measured(
            category = MetricCategory.DB,
            name = "db.bulkInsert",
            tags = mapOf(
                MetricTag.SLICE to "Clubs",
                "rows" to clubs.size.toString(),
            ),
        ) {
            withContext(Dispatchers.IO) {
                queries.transaction {
                    clubs.forEach { club ->
                        queries.upsert(
                            term = term.toLong(),
                            id = club.id,
                            name = club.name,
                            membersCount = club.membersCount.toLong(),
                            email = club.email,
                            phone = club.phone,
                            fax = club.fax,
                        )
                    }
                }
            }
        }
    }
}
