package pl.jsubczynski.sejmierz.ui.feature.clubdetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import pl.jsubczynski.sejmierz.domain.model.Club
import pl.jsubczynski.sejmierz.domain.model.ClubMember
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
fun ClubDetailScreen(
    component: ClubDetailComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.collectAsState()
    val listState = rememberLazyListState()


    ScreenDisplayTracking(slice = "Club detail", contentLoaded = state.detail != null)


    ScrollJankTracking(
        listState = listState,
        sampler = LocalBenchmarkContext.current.frames,
        cpu = LocalBenchmarkContext.current.cpu,
        slice = "Club detail",
    )

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.club_detail_title)) },
                navigationIcon = {
                    IconButton(onClick = component::onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.action_back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> LoadingView()
                state.error != null ->
                    MessageView(message = state.error!!.toMessage(), onRetry = component::onRetry)
                state.detail != null -> {
                    val detail = state.detail!!
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                    ) {
                        item { ClubHeader(detail.club) }
                        if (detail.members.isEmpty()) {
                            item {
                                Text(
                                    stringResource(Res.string.club_no_members),
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(top = 16.dp),
                                )
                            }
                        } else {
                            item {
                                Text(
                                    stringResource(Res.string.club_members_header),
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                                )
                            }
                            items(detail.members, key = { it.id }) { ClubMemberRow(it) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ClubHeader(club: Club) {
    Column {
        Text(club.name, style = MaterialTheme.typography.headlineSmall)
        Text(stringResource(Res.string.club_id, club.id), style = MaterialTheme.typography.bodySmall)
        Text(stringResource(Res.string.club_members, club.membersCount))
        club.email?.let { Text(stringResource(Res.string.club_email, it)) }
        club.phone?.let { Text(stringResource(Res.string.club_phone, it)) }
        club.fax?.let { Text(stringResource(Res.string.club_fax, it)) }
    }
}

@Composable
private fun ClubMemberRow(member: ClubMember) {
    Column(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Text(member.fullName, style = MaterialTheme.typography.titleSmall)
        member.function?.takeIf { it.isNotBlank() }?.let {
            Text(it, style = MaterialTheme.typography.bodySmall)
        }
    }
    HorizontalDivider()
}
