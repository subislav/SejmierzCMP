package pl.jsubczynski.sejmierz.ui.navigation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.DelicateDecomposeApi
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import pl.jsubczynski.sejmierz.domain.benchmark.BenchmarkExporter
import pl.jsubczynski.sejmierz.domain.benchmark.MetricRepository
import pl.jsubczynski.sejmierz.domain.repository.SettingsRepository
import pl.jsubczynski.sejmierz.domain.usecase.GetClubDetailUseCase
import pl.jsubczynski.sejmierz.domain.usecase.GetClubsUseCase
import pl.jsubczynski.sejmierz.domain.usecase.GetMpDetailUseCase
import pl.jsubczynski.sejmierz.domain.usecase.GetMpsUseCase
import pl.jsubczynski.sejmierz.domain.usecase.GetVotingDetailUseCase
import pl.jsubczynski.sejmierz.domain.usecase.GetVotingsUseCase
import pl.jsubczynski.sejmierz.ui.feature.benchmark.BenchmarkComponent
import pl.jsubczynski.sejmierz.ui.feature.benchmark.DefaultBenchmarkComponent
import pl.jsubczynski.sejmierz.ui.feature.clubdetail.ClubDetailComponent
import pl.jsubczynski.sejmierz.ui.feature.clubdetail.DefaultClubDetailComponent
import pl.jsubczynski.sejmierz.ui.feature.clubs.ClubsComponent
import pl.jsubczynski.sejmierz.ui.feature.clubs.DefaultClubsComponent
import pl.jsubczynski.sejmierz.ui.feature.mpdetail.DefaultMpDetailComponent
import pl.jsubczynski.sejmierz.ui.feature.mpdetail.MpDetailComponent
import pl.jsubczynski.sejmierz.ui.feature.mps.DefaultMpsComponent
import pl.jsubczynski.sejmierz.ui.feature.mps.MpsComponent
import pl.jsubczynski.sejmierz.ui.feature.settings.DefaultSettingsComponent
import pl.jsubczynski.sejmierz.ui.feature.settings.SettingsComponent
import pl.jsubczynski.sejmierz.ui.feature.votingdetail.DefaultVotingDetailComponent
import pl.jsubczynski.sejmierz.ui.feature.votingdetail.VotingDetailComponent
import pl.jsubczynski.sejmierz.ui.feature.votings.DefaultVotingsComponent
import pl.jsubczynski.sejmierz.ui.feature.votings.VotingsComponent

enum class AppTab {
    Mps,
    Votings,
    Clubs,
    Telemetry,
}

interface RootComponent {
    val selectedTab: Value<AppTab>
    val isSettingsOpen: StateFlow<Boolean>

    val mpsStack: Value<ChildStack<*, MpsChild>>
    val votingsStack: Value<ChildStack<*, VotingsChild>>
    val clubsStack: Value<ChildStack<*, ClubsChild>>
    val benchmarkComponent: BenchmarkComponent
    val settingsComponent: SettingsComponent

    fun selectTab(tab: AppTab)
    fun openSettings()
    fun closeSettings()

    sealed interface MpsChild {
        class List(val component: MpsComponent) : MpsChild
        class Detail(val component: MpDetailComponent) : MpsChild
    }

    sealed interface VotingsChild {
        class List(val component: VotingsComponent) : VotingsChild
        class Detail(val component: VotingDetailComponent) : VotingsChild
    }

    sealed interface ClubsChild {
        class List(val component: ClubsComponent) : ClubsChild
        class Detail(val component: ClubDetailComponent) : ClubsChild
    }
}

@OptIn(DelicateDecomposeApi::class)
class DefaultRootComponent(
    componentContext: ComponentContext,
    private val getMps: GetMpsUseCase,
    private val getMpDetail: GetMpDetailUseCase,
    private val getVotings: GetVotingsUseCase,
    private val getVotingDetail: GetVotingDetailUseCase,
    private val getClubs: GetClubsUseCase,
    private val getClubDetail: GetClubDetailUseCase,
    private val settings: SettingsRepository,
    private val metrics: MetricRepository,
    private val exporter: BenchmarkExporter,
) : RootComponent, ComponentContext by componentContext {

    private val _selectedTab = MutableValue(AppTab.Mps)
    override val selectedTab: Value<AppTab> = _selectedTab

    private val _isSettingsOpen = MutableStateFlow(false)
    override val isSettingsOpen: StateFlow<Boolean> = _isSettingsOpen.asStateFlow()

    private val mpsNav = StackNavigation<MpsConfig>()
    private val votingsNav = StackNavigation<VotingsConfig>()
    private val clubsNav = StackNavigation<ClubsConfig>()

    override val mpsStack: Value<ChildStack<*, RootComponent.MpsChild>> =
        childStack(
            source = mpsNav,
            serializer = null,
            initialConfiguration = MpsConfig.List,
            key = "mps",
            handleBackButton = true,
            childFactory = ::mpsChild,
        )

    override val votingsStack: Value<ChildStack<*, RootComponent.VotingsChild>> =
        childStack(
            source = votingsNav,
            serializer = null,
            initialConfiguration = VotingsConfig.List,
            key = "votings",
            handleBackButton = true,
            childFactory = ::votingsChild,
        )

    override val clubsStack: Value<ChildStack<*, RootComponent.ClubsChild>> =
        childStack(
            source = clubsNav,
            serializer = null,
            initialConfiguration = ClubsConfig.List,
            key = "clubs",
            handleBackButton = true,
            childFactory = ::clubsChild,
        )

    override val benchmarkComponent: BenchmarkComponent =
        DefaultBenchmarkComponent(
            componentContext = componentContext,
            metrics = metrics,
            settings = settings,
            exporter = exporter,
            onFinished = {},
        )

    override val settingsComponent: SettingsComponent =
        DefaultSettingsComponent(
            componentContext = componentContext,
            settingsRepository = settings,
            onGoBack = ::closeSettings,
        )

    override fun selectTab(tab: AppTab) {
        _selectedTab.value = tab
    }

    override fun openSettings() {
        _isSettingsOpen.value = true
    }

    override fun closeSettings() {
        _isSettingsOpen.value = false
    }

    private fun mpsChild(config: MpsConfig, context: ComponentContext): RootComponent.MpsChild =
        when (config) {
            MpsConfig.List -> RootComponent.MpsChild.List(
                DefaultMpsComponent(
                    componentContext = context,
                    getMps = getMps,
                    settings = settings,
                    onOpenMp = { id -> mpsNav.push(MpsConfig.Detail(id)) },
                ),
            )
            is MpsConfig.Detail -> RootComponent.MpsChild.Detail(
                DefaultMpDetailComponent(
                    componentContext = context,
                    mpId = config.id,
                    getMpDetail = getMpDetail,
                    settings = settings,
                    onFinished = { mpsNav.pop() },
                ),
            )
        }

    private fun votingsChild(
        config: VotingsConfig,
        context: ComponentContext,
    ): RootComponent.VotingsChild =
        when (config) {
            VotingsConfig.List -> RootComponent.VotingsChild.List(
                DefaultVotingsComponent(
                    componentContext = context,
                    getVotings = getVotings,
                    settings = settings,
                    onOpenVoting = { sitting, votingNumber ->
                        votingsNav.push(VotingsConfig.Detail(sitting, votingNumber))
                    },
                ),
            )
            is VotingsConfig.Detail -> RootComponent.VotingsChild.Detail(
                DefaultVotingDetailComponent(
                    componentContext = context,
                    sitting = config.sitting,
                    votingNumber = config.votingNumber,
                    getVotingDetail = getVotingDetail,
                    settings = settings,
                    onFinished = { votingsNav.pop() },
                ),
            )
        }

    private fun clubsChild(
        config: ClubsConfig,
        context: ComponentContext,
    ): RootComponent.ClubsChild =
        when (config) {
            ClubsConfig.List -> RootComponent.ClubsChild.List(
                DefaultClubsComponent(
                    componentContext = context,
                    getClubs = getClubs,
                    settings = settings,
                    onOpenClub = { id -> clubsNav.push(ClubsConfig.Detail(id)) },
                ),
            )
            is ClubsConfig.Detail -> RootComponent.ClubsChild.Detail(
                DefaultClubDetailComponent(
                    componentContext = context,
                    clubId = config.id,
                    getClubDetail = getClubDetail,
                    settings = settings,
                    onFinished = { clubsNav.pop() },
                ),
            )
        }

    private sealed interface MpsConfig {
        data object List : MpsConfig
        data class Detail(val id: Int) : MpsConfig
    }

    private sealed interface VotingsConfig {
        data object List : VotingsConfig
        data class Detail(val sitting: Int, val votingNumber: Int) : VotingsConfig
    }

    private sealed interface ClubsConfig {
        data object List : ClubsConfig
        data class Detail(val id: String) : ClubsConfig
    }
}
