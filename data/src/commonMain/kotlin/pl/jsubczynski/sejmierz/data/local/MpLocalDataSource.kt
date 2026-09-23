package pl.jsubczynski.sejmierz.data.local

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pl.jsubczynski.sejmierz.data.db.SejmierzDatabase
import pl.jsubczynski.sejmierz.data.mapper.toDomain
import pl.jsubczynski.sejmierz.domain.benchmark.Benchmark
import pl.jsubczynski.sejmierz.domain.benchmark.MetricCategory
import pl.jsubczynski.sejmierz.domain.benchmark.MetricTag
import pl.jsubczynski.sejmierz.domain.model.Mp

class MpLocalDataSource(
    private val database: SejmierzDatabase,
    private val benchmark: Benchmark,
) {
    private val queries get() = database.mpEntityQueries

    suspend fun readPage(term: Int, offset: Int, limit: Int): List<Mp> =
        benchmark.measured(
            category = MetricCategory.DB,
            name = "db.read.page",
            tags = mapOf(MetricTag.SLICE to "MPs"),
        ) {
            withContext(Dispatchers.IO) {
                queries.selectPage(
                    term = term.toLong(),
                    limit = limit.toLong(),
                    offset = offset.toLong(),
                ).executeAsList().map { it.toDomain() }
            }
        }

    suspend fun readById(term: Int, id: Int): Mp? =
        benchmark.measured(
            category = MetricCategory.DB,
            name = "db.read.byId",
            tags = mapOf(MetricTag.SLICE to "MPs"),
        ) {
            withContext(Dispatchers.IO) {
                queries.selectById(term = term.toLong(), id = id.toLong())
                    .executeAsOneOrNull()
                    ?.toDomain()
            }
        }

    suspend fun count(term: Int): Long = withContext(Dispatchers.IO) {
        queries.countByTerm(term.toLong()).executeAsOne()
    }


    suspend fun upsertAll(term: Int, mps: List<Mp>) {
        benchmark.measured(
            category = MetricCategory.DB,
            name = "db.bulkInsert",
            tags = mapOf(
                MetricTag.SLICE to "MPs",
                "rows" to mps.size.toString(),
            ),
        ) {
            withContext(Dispatchers.IO) {
                queries.transaction {
                    mps.forEach { mp ->
                        queries.upsert(
                            term = term.toLong(),
                            id = mp.id.toLong(),
                            firstLastName = mp.firstLastName,
                            club = mp.club,
                            active = if (mp.active) 1L else 0L,
                            districtName = mp.districtName,
                            districtNum = mp.districtNum?.toLong(),
                            voivodeship = mp.voivodeship,
                            profession = mp.profession,
                            educationLevel = mp.educationLevel,
                            email = mp.email,
                            numberOfVotes = mp.numberOfVotes?.toLong(),
                            birthDate = mp.birthDate,
                            birthLocation = mp.birthLocation,
                        )
                    }
                }
            }
        }
    }
}
