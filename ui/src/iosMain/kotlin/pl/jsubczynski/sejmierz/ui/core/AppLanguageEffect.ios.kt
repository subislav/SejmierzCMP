package pl.jsubczynski.sejmierz.ui.core

import platform.Foundation.NSUserDefaults

actual fun applyAppLanguage(languageCode: String) {
    NSUserDefaults.standardUserDefaults.setObject(listOf(languageCode), forKey = "AppleLanguages")
    NSUserDefaults.standardUserDefaults.synchronize()
}
