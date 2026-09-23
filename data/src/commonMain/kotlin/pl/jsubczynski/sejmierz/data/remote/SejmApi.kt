package pl.jsubczynski.sejmierz.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import pl.jsubczynski.sejmierz.data.remote.dto.ClubDto
import pl.jsubczynski.sejmierz.data.remote.dto.MpDto
import pl.jsubczynski.sejmierz.data.remote.dto.VotingDto
import pl.jsubczynski.sejmierz.domain.benchmark.BenchmarkContext
import pl.jsubczynski.sejmierz.domain.benchmark.MetricCategory
import pl.jsubczynski.sejmierz.domain.benchmark.MetricTag

class ApiHttpException(val code: Int, message: String?) : Exception(message)

class SejmApi(
    private val client: HttpClient,
    private val json: Json,
    private val benchmarkContext: BenchmarkContext,
) {
    suspend fun getMps(term: Int, offset: Int, limit: Int): List<MpDto> {
        val raw = client.get("/sejm/term$term/MP") {
            parameter("offset", offset)
            parameter("limit", limit)
        }.also { response ->
            if (!response.status.isSuccess()) {
                throw ApiHttpException(response.status.value, response.status.description)
            }
        }.bodyAsText()

        return decode("/MP", ListSerializerCache.mpList, raw)
    }

    suspend fun getMp(term: Int, id: Int): MpDto {
        val raw = client.get("/sejm/term$term/MP/$id").also { response ->
            if (!response.status.isSuccess()) {
                throw ApiHttpException(response.status.value, response.status.description)
            }
        }.bodyAsText()

        return decode("/MP/{id}", MpDto.serializer(), raw)
    }

    suspend fun searchVotings(
        term: Int,
        offset: Int,
        limit: Int,
        dateFrom: String? = null,
        dateTo: String? = null,
        title: String? = null,
    ): List<VotingDto> {
        val raw = client.get("/sejm/term$term/votings/search") {
            parameter("offset", offset)
            parameter("limit", limit)
            parameter("dateFrom", dateFrom)
            parameter("dateTo", dateTo)
            parameter("title", title)
        }.also { response ->
            if (!response.status.isSuccess()) {
                throw ApiHttpException(response.status.value, response.status.description)
            }
        }.bodyAsText()

        return decode("/votings/search", ListSerializerCache.votingList, raw)
    }

    suspend fun getVoting(term: Int, sitting: Int, votingNumber: Int): VotingDto {
        val raw = client.get("/sejm/term$term/votings/$sitting/$votingNumber").also { response ->
            if (!response.status.isSuccess()) {
                throw ApiHttpException(response.status.value, response.status.description)
            }
        }.bodyAsText()

        return decode("/votings/{sitting}/{num}", VotingDto.serializer(), raw)
    }

    suspend fun getClubs(term: Int): List<ClubDto> {
        val raw = client.get("/sejm/term$term/clubs").also { response ->
            if (!response.status.isSuccess()) {
                throw ApiHttpException(response.status.value, response.status.description)
            }
        }.bodyAsText()

        return decode("/clubs", ListSerializerCache.clubList, raw)
    }

    suspend fun getClub(term: Int, id: String): ClubDto {
        val raw = client.get("/sejm/term$term/clubs/$id").also { response ->
            if (!response.status.isSuccess()) {
                throw ApiHttpException(response.status.value, response.status.description)
            }
        }.bodyAsText()

        return decode("/clubs/{id}", ClubDto.serializer(), raw)
    }



    private suspend inline fun <T> decode(
        endpoint: String,
        deserializer: kotlinx.serialization.DeserializationStrategy<T>,
        raw: String,
    ): T = benchmarkContext.measuredWithMemory(
        category = MetricCategory.PARSING,
        name = "parsing.decode",
        tags = mapOf(
            MetricTag.ENDPOINT to endpoint,
            "bytes" to raw.length.toString(),
        ),
    ) {
        json.decodeFromString(deserializer, raw)
    }
}

private object ListSerializerCache {
    val mpList = kotlinx.serialization.builtins.ListSerializer(MpDto.serializer())
    val votingList = kotlinx.serialization.builtins.ListSerializer(VotingDto.serializer())
    val clubList = kotlinx.serialization.builtins.ListSerializer(ClubDto.serializer())
}
