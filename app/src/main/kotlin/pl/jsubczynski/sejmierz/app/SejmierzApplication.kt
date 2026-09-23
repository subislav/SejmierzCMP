package pl.jsubczynski.sejmierz.app

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import pl.jsubczynski.sejmierz.app.di.appModule
import pl.jsubczynski.sejmierz.data.di.dataModule
import pl.jsubczynski.sejmierz.domain.di.domainModule

class SejmierzApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@SejmierzApplication)
            modules(domainModule, dataModule, appModule)
        }
    }
}
