package pl.jsubczynski.sejmierz.data.remote.benchmark

import io.ktor.client.plugins.api.Send
import io.ktor.client.plugins.api.createClientPlugin
import pl.jsubczynski.sejmierz.domain.benchmark.Benchmark
import pl.jsubczynski.sejmierz.domain.benchmark.MetricCategory
import pl.jsubczynski.sejmierz.domain.benchmark.MetricTag
import kotlin.time.TimeSource

class NetworkBenchmarkConfig {
    lateinit var benchmark: Benchmark
}

val NetworkBenchmarkPlugin = createClientPlugin("NetworkBenchmark", ::NetworkBenchmarkConfig) {
    val benchmark = pluginConfig.benchmark

    on(Send) { request ->
        val mark = TimeSource.Monotonic.markNow()
        val call = proceed(request)
        val elapsedMs = mark.elapsedNow().inWholeMicroseconds / 1000.0
        benchmark.record(
            name = "network.transport",
            category = MetricCategory.NETWORK,
            value = elapsedMs,
            unit = "ms",
            tags = mapOf(MetricTag.ENDPOINT to request.url.build().encodedPath),
        )
        call
    }
}
