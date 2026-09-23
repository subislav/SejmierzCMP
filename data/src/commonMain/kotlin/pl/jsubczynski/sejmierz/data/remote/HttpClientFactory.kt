package pl.jsubczynski.sejmierz.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.URLProtocol
import io.ktor.http.contentType
import io.ktor.http.ContentType
import pl.jsubczynski.sejmierz.data.remote.benchmark.NetworkBenchmarkPlugin
import pl.jsubczynski.sejmierz.domain.benchmark.Benchmark

object HttpClientFactory {

    const val HOST = "api.sejm.gov.pl"

    fun create(engine: HttpClientEngine, benchmark: Benchmark): HttpClient =
        HttpClient(engine) {
            expectSuccess = false

            install(NetworkBenchmarkPlugin) {
                this.benchmark = benchmark
            }

            install(HttpTimeout) {
                requestTimeoutMillis = 30_000
                connectTimeoutMillis = 15_000
                socketTimeoutMillis = 30_000
            }

            defaultRequest {
                url {
                    protocol = URLProtocol.HTTPS
                    host = HOST
                }
                contentType(ContentType.Application.Json)
            }
        }
}
