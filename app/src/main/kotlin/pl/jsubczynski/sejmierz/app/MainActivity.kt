package pl.jsubczynski.sejmierz.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.arkivanov.decompose.retainedComponent
import org.koin.android.ext.android.get
import pl.jsubczynski.sejmierz.app.benchmark.AndroidFrameMetricsSampler
import pl.jsubczynski.sejmierz.app.benchmark.androidColdStartElapsedMs
import pl.jsubczynski.sejmierz.domain.benchmark.DisplayTracker
import pl.jsubczynski.sejmierz.domain.benchmark.FrameMetricsSampler
import pl.jsubczynski.sejmierz.ui.core.theme.SejmierzTheme
import pl.jsubczynski.sejmierz.ui.navigation.DefaultRootComponent
import pl.jsubczynski.sejmierz.ui.navigation.RootContent

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val displayTracker = get<DisplayTracker>()
        val frameSampler = get<FrameMetricsSampler>()

        val root = retainedComponent { componentContext ->
            DefaultRootComponent(
                componentContext = componentContext,
                getMps = get(),
                getMpDetail = get(),
                getVotings = get(),
                getVotingDetail = get(),
                getClubs = get(),
                getClubDetail = get(),
                settings = get(),
                metrics = get(),
                exporter = get(),
            )
        }

        setContent {
            SejmierzTheme {
                RootContent(component = root)
            }
        }

        displayTracker.reportInitialDisplay(slice = "startup")
        window.decorView.post {
            (frameSampler as? AndroidFrameMetricsSampler)?.attachWindow(window)
            displayTracker.reportFullDisplay(slice = "startup")
            displayTracker.reportColdStart(androidColdStartElapsedMs())
            reportFullyDrawn()
        }
    }
}
