package pl.jsubczynski.sejmierz.ui.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.staticCompositionLocalOf
import platform.Foundation.NSLocale
import platform.Foundation.NSUserDefaults
import platform.Foundation.preferredLanguages

actual object LocalAppLocale {
    private const val LANG_KEY = "AppleLanguages"

    private val default: String =
        (NSLocale.preferredLanguages.firstOrNull() as? String) ?: "pl"

    private val LocalAppLocaleComposition = staticCompositionLocalOf { default }

    actual val current: String
        @Composable get() = LocalAppLocaleComposition.current

    @Composable
    actual infix fun provides(value: String?): ProvidedValue<*> {
        val new = value ?: default
        if (value == null) {
            NSUserDefaults.standardUserDefaults.removeObjectForKey(LANG_KEY)
        } else {
            NSUserDefaults.standardUserDefaults.setObject(listOf(new), forKey = LANG_KEY)
        }
        NSUserDefaults.standardUserDefaults.synchronize()
        return LocalAppLocaleComposition.provides(new)
    }
}
