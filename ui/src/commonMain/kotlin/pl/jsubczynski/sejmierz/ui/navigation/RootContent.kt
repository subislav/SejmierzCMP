package pl.jsubczynski.sejmierz.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import pl.jsubczynski.sejmierz.domain.benchmark.BenchmarkContext
import pl.jsubczynski.sejmierz.domain.repository.SettingsRepository
import pl.jsubczynski.sejmierz.ui.core.applyAppLanguage
import pl.jsubczynski.sejmierz.ui.core.benchmark.LocalBenchmarkContext
import pl.jsubczynski.sejmierz.ui.feature.benchmark.BenchmarkScreen
import pl.jsubczynski.sejmierz.ui.feature.clubdetail.ClubDetailScreen
import pl.jsubczynski.sejmierz.ui.feature.clubs.ClubsScreen
import pl.jsubczynski.sejmierz.ui.feature.mpdetail.MpDetailScreen
import pl.jsubczynski.sejmierz.ui.feature.mps.MpsScreen
import pl.jsubczynski.sejmierz.ui.feature.settings.SettingsScreen
import pl.jsubczynski.sejmierz.ui.feature.votingdetail.VotingDetailScreen
import pl.jsubczynski.sejmierz.ui.feature.votings.VotingsScreen
import pl.jsubczynski.sejmierz.ui.resources.Res
import pl.jsubczynski.sejmierz.ui.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RootContent(
    component: RootComponent,
    modifier: Modifier = Modifier,
) {
    val benchmarkContext = koinInject<BenchmarkContext>()
    val settingsRepository = koinInject<SettingsRepository>()

    val language by settingsRepository.appLanguage.collectAsState()
    val selectedTab by component.selectedTab.subscribeAsState()
    val isSettingsOpen by component.isSettingsOpen.collectAsState()

    LaunchedEffect(language) {
        applyAppLanguage(language.code)
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    LaunchedEffect(isSettingsOpen) {
        if (isSettingsOpen && drawerState.isClosed) drawerState.open()
        if (!isSettingsOpen && drawerState.isOpen) drawerState.close()
    }

    LaunchedEffect(drawerState.currentValue) {
        if (drawerState.isClosed && isSettingsOpen) {
            component.closeSettings()
        }
    }

    CompositionLocalProvider(
        LocalBenchmarkContext provides benchmarkContext,
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            ModalNavigationDrawer(
                drawerState = drawerState,
                gesturesEnabled = true,
                drawerContent = {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        ModalDrawerSheet(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(0.66f),
                        ) {
                            SettingsScreen(component = component.settingsComponent)
                        }
                    }
                },
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Scaffold(
                        modifier = modifier.fillMaxSize(),
                        bottomBar = {
                            NavigationBar(modifier = Modifier.height(80.dp)) {
                                NavigationBarItem(
                                    selected = selectedTab == AppTab.Mps,
                                    onClick = { component.selectTab(AppTab.Mps) },
                                    icon = {
                                        Icon(
                                            Icons.Default.Person,
                                            contentDescription = stringResource(Res.string.mps_title),
                                            modifier = Modifier.size(26.dp),
                                        )
                                    },
                                    label = { Text(stringResource(Res.string.mps_title)) },
                                )
                                NavigationBarItem(
                                    selected = selectedTab == AppTab.Votings,
                                    onClick = { component.selectTab(AppTab.Votings) },
                                    icon = {
                                        Icon(
                                            Icons.AutoMirrored.Filled.Assignment,
                                            contentDescription = stringResource(Res.string.votings_title),
                                            modifier = Modifier.size(26.dp),
                                        )
                                    },
                                    label = { Text(stringResource(Res.string.votings_title)) },
                                )
                                NavigationBarItem(
                                    selected = selectedTab == AppTab.Clubs,
                                    onClick = { component.selectTab(AppTab.Clubs) },
                                    icon = {
                                        Icon(
                                            Icons.Default.Groups,
                                            contentDescription = stringResource(Res.string.clubs_title),
                                            modifier = Modifier.size(26.dp),
                                        )
                                    },
                                    label = { Text(stringResource(Res.string.clubs_title)) },
                                )
                                NavigationBarItem(
                                    selected = selectedTab == AppTab.Telemetry,
                                    onClick = { component.selectTab(AppTab.Telemetry) },
                                    icon = {
                                        Icon(
                                            Icons.Default.BarChart,
                                            contentDescription = stringResource(Res.string.tab_telemetry),
                                            modifier = Modifier.size(26.dp),
                                        )
                                    },
                                    label = { Text(stringResource(Res.string.tab_telemetry)) },
                                )
                            }
                        },
                    ) { padding ->
                        Box(
                            Modifier
                                .fillMaxSize()
                                .padding(padding),
                        ) {
                            when (selectedTab) {
                                AppTab.Mps -> Children(stack = component.mpsStack) { child ->
                                    when (val instance = child.instance) {
                                        is RootComponent.MpsChild.List -> MpsScreen(
                                            component = instance.component,
                                            onOpenSettings = component::openSettings,
                                        )
                                        is RootComponent.MpsChild.Detail ->
                                            MpDetailScreen(instance.component)
                                    }
                                }
                                AppTab.Votings -> Children(stack = component.votingsStack) { child ->
                                    when (val instance = child.instance) {
                                        is RootComponent.VotingsChild.List -> VotingsScreen(
                                            component = instance.component,
                                            onOpenSettings = component::openSettings,
                                        )
                                        is RootComponent.VotingsChild.Detail ->
                                            VotingDetailScreen(instance.component)
                                    }
                                }
                                AppTab.Clubs -> Children(stack = component.clubsStack) { child ->
                                    when (val instance = child.instance) {
                                        is RootComponent.ClubsChild.List -> ClubsScreen(
                                            component = instance.component,
                                            onOpenSettings = component::openSettings,
                                        )
                                        is RootComponent.ClubsChild.Detail ->
                                            ClubDetailScreen(instance.component)
                                    }
                                }
                                AppTab.Telemetry -> BenchmarkScreen(
                                    component = component.benchmarkComponent,
                                    onOpenSettings = component::openSettings,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
