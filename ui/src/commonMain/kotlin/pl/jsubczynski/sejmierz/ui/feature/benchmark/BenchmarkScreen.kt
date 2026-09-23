package pl.jsubczynski.sejmierz.ui.feature.benchmark

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import pl.jsubczynski.sejmierz.domain.benchmark.stats.BenchmarkStatistics
import pl.jsubczynski.sejmierz.ui.resources.Res
import pl.jsubczynski.sejmierz.ui.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BenchmarkScreen(
    component: BenchmarkComponent,
    onOpenSettings: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val state by component.state.collectAsState()

    var iterations by remember(state.config.totalIterations) {
        mutableStateOf(state.config.totalIterations.toString())
    }
    var warmup by remember(state.config.warmupIterations) {
        mutableStateOf(state.config.warmupIterations.toString())
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.benchmark_title)) },
                actions = {
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
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = iterations,
                    onValueChange = {
                        iterations = it
                        applyConfig(component, iterations, warmup)
                    },
                    label = { Text(stringResource(Res.string.benchmark_iterations)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = warmup,
                    onValueChange = {
                        warmup = it
                        applyConfig(component, iterations, warmup)
                    },
                    label = { Text(stringResource(Res.string.benchmark_warmup)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = component::exportCsv,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(Res.string.benchmark_export_csv))
                }
                OutlinedButton(
                    onClick = component::clear,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(Res.string.benchmark_clear))
                }
            }

            Text(
                stringResource(Res.string.benchmark_samples, state.sampleCount),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            state.lastExportPath?.let {
                Text(
                    stringResource(Res.string.benchmark_exported, it),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF15803D),
                )
            }

            if (state.stats.isEmpty()) {
                Text(
                    stringResource(Res.string.benchmark_no_data),
                    modifier = Modifier.padding(top = 24.dp),
                )
            } else {
                LazyColumn(Modifier.fillMaxSize().padding(top = 12.dp)) {
                    items(state.stats, key = { "${it.name}|${it.slice ?: ""}" }) { StatCard(it) }
                }
            }
        }
    }
}

@Composable
private fun StatCard(stats: BenchmarkStatistics) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ),
        shape = RoundedCornerShape(10.dp),
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            val title = stats.slice?.let { "${stats.name}  ·  $it" } ?: stats.name
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                "n=${stats.sampleCount}  mean=${stats.mean.fmt()} ${stats.unit}  " +
                    "median=${stats.median.fmt()}  p95=${stats.p95.fmt()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                "min=${stats.min.fmt()}  max=${stats.max.fmt()}  sd=${stats.stdDev.fmt()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun applyConfig(component: BenchmarkComponent, iterations: String, warmup: String) {
    val total = iterations.toIntOrNull() ?: return
    val warm = warmup.toIntOrNull() ?: return
    component.setIterations(total, warm)
}

private fun Double.fmt(): String = ((this * 100).toLong() / 100.0).toString()
