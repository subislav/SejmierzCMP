package pl.jsubczynski.sejmierz.ios

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.window.ComposeUIViewController
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import pl.jsubczynski.sejmierz.data.di.dataModule
import pl.jsubczynski.sejmierz.domain.benchmark.DisplayTracker
import pl.jsubczynski.sejmierz.domain.di.domainModule
import pl.jsubczynski.sejmierz.ios.benchmark.iosColdStartElapsedMs
import pl.jsubczynski.sejmierz.ios.di.iosAppModule
import pl.jsubczynski.sejmierz.ui.core.theme.SejmierzTheme
import pl.jsubczynski.sejmierz.ui.navigation.DefaultRootComponent
import pl.jsubczynski.sejmierz.ui.navigation.RootContent
import platform.UIKit.UIViewController

private fun ensureKoin() {
    if (GlobalContext.getOrNull() != null) return
    startKoin {
        modules(domainModule, dataModule, iosAppModule)
    }
}

fun MainViewController(): UIViewController {
    ensureKoin()
    val koin = GlobalContext.get()
    val displayTracker = koin.get<DisplayTracker>()

    val lifecycle = LifecycleRegistry()
    val root = DefaultRootComponent(
        componentContext = DefaultComponentContext(lifecycle = lifecycle),
        getMps = koin.get(),
        getMpDetail = koin.get(),
        getVotings = koin.get(),
        getVotingDetail = koin.get(),
        getClubs = koin.get(),
        getClubDetail = koin.get(),
        settings = koin.get(),
        metrics = koin.get(),
        exporter = koin.get(),
    )

    displayTracker.reportInitialDisplay(slice = "startup")

    return ComposeUIViewController {
        LaunchedEffect(Unit) {
            displayTracker.reportFullDisplay(slice = "startup")
            displayTracker.reportColdStart(iosColdStartElapsedMs())
        }
        SejmierzTheme {
            RootContent(component = root)
        }
    }
}
