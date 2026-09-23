package pl.jsubczynski.sejmierz.ui.core

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

actual fun applyAppLanguage(languageCode: String) {
    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(languageCode))
}
