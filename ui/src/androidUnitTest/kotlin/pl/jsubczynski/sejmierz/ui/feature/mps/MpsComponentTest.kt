package pl.jsubczynski.sejmierz.ui.feature.mps

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import pl.jsubczynski.sejmierz.domain.benchmark.Benchmark
import pl.jsubczynski.sejmierz.domain.benchmark.BenchmarkConfig
import pl.jsubczynski.sejmierz.domain.benchmark.Metric
import pl.jsubczynski.sejmierz.domain.benchmark.MetricSink
import pl.jsubczynski.sejmierz.domain.error.DomainResult
import pl.jsubczynski.sejmierz.domain.model.DataSource
import pl.jsubczynski.sejmierz.domain.model.DataSourceMode
import pl.jsubczynski.sejmierz.domain.model.Mp
import pl.jsubczynski.sejmierz.domain.model.Page
import pl.jsubczynski.sejmierz.domain.model.Term
import pl.jsubczynski.sejmierz.domain.repository.MpRepository
import pl.jsubczynski.sejmierz.domain.repository.SettingsRepository
import pl.jsubczynski.sejmierz.domain.usecase.GetMpsUseCase

@OptIn(ExperimentalCoroutinesApi::class)
class MpsComponentTest {

    private val dispatcher = UnconfinedTestDispatcher()

    @Test
    fun `loads first page on init`() = runTest(dispatcher) {
        val component = component(scope = CoroutineScope(dispatcher))
        advanceUntilIdle()

        val state = component.state.value
        assertEquals(2, state.mps.size)
        assertNull(state.error)
        assertEquals(false, state.isLoading)
    }

    @Test
    fun `toggling mode reloads from the selected source`() = runTest(dispatcher) {
        val settings = FakeSettings()
        val repo = FakeMpRepository()
        val component = component(settings = settings, repo = repo, scope = CoroutineScope(dispatcher))
        advanceUntilIdle()

        settings.setDataSourceMode(DataSourceMode.OFFLINE)
        advanceUntilIdle()

        assertEquals(DataSourceMode.OFFLINE, component.state.value.mode)
        assertEquals(DataSource.LOCAL, repo.lastSource)
    }

    private fun component(
        settings: SettingsRepository = FakeSettings(),
        repo: MpRepository = FakeMpRepository(),
        scope: CoroutineScope,
    ): DefaultMpsComponent {
        val benchmark = Benchmark(sink = NoopSink)
        return DefaultMpsComponent(
            componentContext = DefaultComponentContext(LifecycleRegistry()),
            getMps = GetMpsUseCase(repo, benchmark),
            settings = settings,
            onOpenMp = {},
            scope = scope,
        )
    }

    private object NoopSink : MetricSink {
        override fun record(metric: Metric) = Unit
    }

    private class FakeSettings : SettingsRepository {
        private val mode = MutableStateFlow(DataSourceMode.ONLINE)
        private val config = MutableStateFlow(BenchmarkConfig())
        private val language = MutableStateFlow(pl.jsubczynski.sejmierz.domain.model.AppLanguage.EN)
        override val dataSourceMode: StateFlow<DataSourceMode> = mode
        override val benchmarkConfig: StateFlow<BenchmarkConfig> = config
        override val appLanguage: StateFlow<pl.jsubczynski.sejmierz.domain.model.AppLanguage> = language
        override fun setDataSourceMode(m: DataSourceMode) { mode.value = m }
        override fun setBenchmarkConfig(c: BenchmarkConfig) { config.value = c }
        override fun setAppLanguage(language: pl.jsubczynski.sejmierz.domain.model.AppLanguage) {
            this.language.value = language
        }
    }

    private class FakeMpRepository : MpRepository {
        var lastSource: DataSource? = null
        private val sample = listOf(
            Mp(1, "Anna Nowak", "KO", true, null, null, null, null, null, null, null, null, null),
            Mp(2, "Jan Kowalski", "PiS", true, null, null, null, null, null, null, null, null, null),
        )

        override suspend fun getMps(
            term: Term,
            source: DataSource,
            offset: Int,
            limit: Int,
        ): DomainResult<Page<Mp>> {
            lastSource = source
            val items = if (offset == 0) sample else emptyList()
            return DomainResult.success(Page(items, offset, limit))
        }

        override suspend fun getMp(term: Term, id: Int, source: DataSource): DomainResult<Mp> {
            lastSource = source
            return DomainResult.success(sample.first { it.id == id })
        }
    }
}
