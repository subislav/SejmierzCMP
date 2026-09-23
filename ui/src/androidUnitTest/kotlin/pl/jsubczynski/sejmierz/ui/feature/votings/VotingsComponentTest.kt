package pl.jsubczynski.sejmierz.ui.feature.votings

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
import pl.jsubczynski.sejmierz.domain.model.Page
import pl.jsubczynski.sejmierz.domain.model.Term
import pl.jsubczynski.sejmierz.domain.model.Voting
import pl.jsubczynski.sejmierz.domain.model.VotingDetail
import pl.jsubczynski.sejmierz.domain.repository.SettingsRepository
import pl.jsubczynski.sejmierz.domain.repository.VotingRepository
import pl.jsubczynski.sejmierz.domain.usecase.GetVotingsUseCase

@OptIn(ExperimentalCoroutinesApi::class)
class VotingsComponentTest {

    private val dispatcher = UnconfinedTestDispatcher()

    @Test
    fun `loads first page on init`() = runTest(dispatcher) {
        val component = component(scope = CoroutineScope(dispatcher))
        advanceUntilIdle()

        val state = component.state.value
        assertEquals(2, state.votings.size)
        assertNull(state.error)
        assertEquals(false, state.isLoading)
    }

    @Test
    fun `toggling mode reloads from the selected source`() = runTest(dispatcher) {
        val settings = FakeSettings()
        val repo = FakeVotingRepository()
        val component = component(settings = settings, repo = repo, scope = CoroutineScope(dispatcher))
        advanceUntilIdle()

        settings.setDataSourceMode(DataSourceMode.OFFLINE)
        advanceUntilIdle()

        assertEquals(DataSourceMode.OFFLINE, component.state.value.mode)
        assertEquals(DataSource.LOCAL, repo.lastSource)
    }

    private fun component(
        settings: SettingsRepository = FakeSettings(),
        repo: VotingRepository = FakeVotingRepository(),
        scope: CoroutineScope,
    ): DefaultVotingsComponent {
        val benchmark = Benchmark(sink = NoopSink)
        return DefaultVotingsComponent(
            componentContext = DefaultComponentContext(LifecycleRegistry()),
            getVotings = GetVotingsUseCase(repo, benchmark),
            settings = settings,
            onOpenVoting = { _, _ -> },
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

    private class FakeVotingRepository : VotingRepository {
        var lastSource: DataSource? = null
        private val sample = listOf(
            voting(sitting = 2, number = 1),
            voting(sitting = 2, number = 2),
        )

        override suspend fun getVotings(
            term: Term,
            source: DataSource,
            offset: Int,
            limit: Int,
        ): DomainResult<Page<Voting>> {
            lastSource = source
            val items = if (offset == 0) sample else emptyList()
            return DomainResult.success(Page(items, offset, limit))
        }

        override suspend fun getVoting(
            term: Term,
            sitting: Int,
            votingNumber: Int,
            source: DataSource,
        ): DomainResult<VotingDetail> {
            lastSource = source
            return DomainResult.success(
                VotingDetail(sample.first { it.sitting == sitting && it.votingNumber == votingNumber }, emptyList()),
            )
        }

        private companion object {
            fun voting(sitting: Int, number: Int) = Voting(
                sitting = sitting,
                votingNumber = number,
                sittingDay = 1,
                date = "2024-01-16T10:00:00",
                title = "Title $number",
                topic = "Topic $number",
                description = null,
                kind = "ELECTRONIC",
                majorityType = "SIMPLE_MAJORITY",
                yes = 100,
                no = 50,
                abstain = 10,
                notParticipating = 20,
                totalVoted = 160,
            )
        }
    }
}
