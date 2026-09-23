package pl.jsubczynski.sejmierz.ui.feature.clubs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.resources.stringResource
import pl.jsubczynski.sejmierz.ui.core.benchmark.LocalBenchmarkContext
import pl.jsubczynski.sejmierz.ui.core.benchmark.ScreenDisplayTracking
import pl.jsubczynski.sejmierz.ui.core.benchmark.ScrollJankTracking
import pl.jsubczynski.sejmierz.ui.core.components.LoadingView
import pl.jsubczynski.sejmierz.ui.core.components.MessageView
import pl.jsubczynski.sejmierz.ui.core.toMessage
import pl.jsubczynski.sejmierz.ui.resources.Res
import pl.jsubczynski.sejmierz.ui.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClubsScreen(
    component: ClubsComponent,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by component.state.collectAsState()
    val listState = rememberLazyListState()

    ScreenDisplayTracking(slice = "Clubs", contentLoaded = state.clubs.isNotEmpty())

    ScrollJankTracking(
        listState = listState,
        sampler = LocalBenchmarkContext.current.frames,
        cpu = LocalBenchmarkContext.current.cpu,
        slice = "Clubs",
    )

    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = listState.layoutInfo.totalItemsCount
            total > 0 && lastVisible >= total - 5
        }
    }
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && state.canLoadMore && !state.isLoadingMore) {
            component.onIntent(ClubsIntent.LoadMore)
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(Res.string.clubs_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    TextButton(onClick = { component.onIntent(ClubsIntent.Refresh) }) {
                        Text(
                            text = stringResource(Res.string.action_refresh),
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = stringResource(Res.string.settings_menu),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading && state.clubs.isEmpty() -> LoadingView()
                state.error != null && state.clubs.isEmpty() ->
                    MessageView(
                        message = state.error!!.toMessage(),
                        onRetry = { component.onIntent(ClubsIntent.Refresh) },
                    )
                state.clubs.isEmpty() ->
                    MessageView(message = stringResource(Res.string.empty_clubs))
                else -> LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize().testTag("clubList"),
                ) {
                    items(items = state.clubs, key = { it.id }) { club ->
                        ClubListItem(
                            club = club,
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { component.onIntent(ClubsIntent.OpenDetail(club.id)) },
                        )
                    }
                }
            }
        }
    }
}
