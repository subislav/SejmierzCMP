package pl.jsubczynski.sejmierz.ui.feature.votingdetail

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
import pl.jsubczynski.sejmierz.domain.model.ClubVoteSummary
import pl.jsubczynski.sejmierz.domain.model.Voting
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
fun VotingDetailScreen(
    component: VotingDetailComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.collectAsState()
    val listState = rememberLazyListState()


    ScreenDisplayTracking(slice = "Voting detail", contentLoaded = state.detail != null)


    ScrollJankTracking(
        listState = listState,
        sampler = LocalBenchmarkContext.current.frames,
        cpu = LocalBenchmarkContext.current.cpu,
        slice = "Voting detail",
    )

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.voting_detail_title)) },
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
                        item { VotingHeader(detail.voting) }
                        if (detail.clubVotes.isEmpty()) {
                            item {
                                Text(
                                    stringResource(Res.string.voting_no_club_breakdown),
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(top = 16.dp),
                                )
                            }
                        } else {
                            item {
                                Text(
                                    stringResource(Res.string.voting_club_breakdown),
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                                )
                            }
                            items(detail.clubVotes, key = { it.club }) { ClubVoteRow(it) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VotingHeader(voting: Voting) {
    Column {
        Text(
            voting.topic?.takeIf { it.isNotBlank() }
                ?: voting.title
                ?: stringResource(Res.string.voting_fallback_title, voting.sitting, voting.votingNumber),
            style = MaterialTheme.typography.headlineSmall,
        )
        voting.title?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
        voting.date?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
        voting.kind?.let { Text(stringResource(Res.string.voting_kind, it)) }
        voting.majorityType?.let { Text(stringResource(Res.string.voting_majority, it)) }
        Text(
            stringResource(Res.string.voting_tally, voting.yes, voting.no, voting.abstain),
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(stringResource(Res.string.voting_total_voted, voting.totalVoted))
    }
}

@Composable
private fun ClubVoteRow(summary: ClubVoteSummary) {
    Column(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Text(summary.club, style = MaterialTheme.typography.titleSmall)
        Text(
            stringResource(
                Res.string.voting_club_tally,
                summary.yes,
                summary.no,
                summary.abstain,
                summary.absent,
            ),
            style = MaterialTheme.typography.bodySmall,
        )
    }
    HorizontalDivider()
}
