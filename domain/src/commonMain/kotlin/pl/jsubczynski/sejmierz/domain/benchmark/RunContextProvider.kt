package pl.jsubczynski.sejmierz.domain.benchmark

interface RunContextProvider {
    fun globalTags(): Map<String, String>
}

object EmptyRunContextProvider : RunContextProvider {
    override fun globalTags(): Map<String, String> = emptyMap()
}
