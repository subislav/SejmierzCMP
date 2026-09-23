package pl.jsubczynski.sejmierz.ui.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SejmRed = Color(0xFFB91C1C)
private val SejmRedDark = Color(0xFFEF5350)

private val LightColors = lightColorScheme(
    primary = SejmRed,
    secondary = Color(0xFF374151),
)

private val DarkColors = darkColorScheme(
    primary = SejmRedDark,
    secondary = Color(0xFF9CA3AF),
)

@Composable
fun SejmierzTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
