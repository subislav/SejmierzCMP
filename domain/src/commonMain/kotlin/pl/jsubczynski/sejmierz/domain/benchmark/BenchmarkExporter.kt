package pl.jsubczynski.sejmierz.domain.benchmark

interface BenchmarkExporter {

    suspend fun export(fileName: String, content: String): String
}
