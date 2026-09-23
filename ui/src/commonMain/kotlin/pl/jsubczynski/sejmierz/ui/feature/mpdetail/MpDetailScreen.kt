package pl.jsubczynski.sejmierz.ui.feature.mpdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import pl.jsubczynski.sejmierz.domain.model.Mp
import pl.jsubczynski.sejmierz.ui.core.benchmark.ScreenDisplayTracking
import pl.jsubczynski.sejmierz.ui.core.components.LoadingView
import pl.jsubczynski.sejmierz.ui.core.components.MessageView
import pl.jsubczynski.sejmierz.ui.core.toMessage
import pl.jsubczynski.sejmierz.ui.resources.Res
import pl.jsubczynski.sejmierz.ui.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MpDetailScreen(
    component: MpDetailComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.collectAsState()

    ScreenDisplayTracking(slice = "MP detail", contentLoaded = state.mp != null)

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.mp_detail_title)) },
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
                state.mp != null -> MpDetailContent(state.mp!!)
            }
        }
    }
}

@Composable
private fun MpDetailContent(mp: Mp) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = mp.firstLastName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            if (!mp.active) {
                Text(
                    text = stringResource(Res.string.mp_inactive),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        HorizontalDivider()

        Text(
            text = stringResource(Res.string.mp_section_basic),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        mp.club?.let { DetailRow(stringResource(Res.string.mp_label_club), it) }
        mp.districtName?.let { DetailRow(stringResource(Res.string.mp_label_district), it) }
        mp.districtNum?.let { DetailRow(stringResource(Res.string.mp_label_district_num), it.toString()) }
        mp.voivodeship?.let { DetailRow(stringResource(Res.string.mp_label_voivodeship), it) }
        mp.numberOfVotes?.let { DetailRow(stringResource(Res.string.mp_label_votes), it.toString()) }

        HorizontalDivider()

        Text(
            text = stringResource(Res.string.mp_section_personal),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        mp.profession?.let { DetailRow(stringResource(Res.string.mp_label_profession), it) }
        mp.educationLevel?.let { DetailRow(stringResource(Res.string.mp_label_education), it) }
        mp.birthDate?.let { DetailRow(stringResource(Res.string.mp_label_birth_date), it) }
        mp.birthLocation?.let { DetailRow(stringResource(Res.string.mp_label_birth_location), it) }
        mp.email?.let { DetailRow(stringResource(Res.string.mp_label_email), it) }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(140.dp),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
        )
    }
}
