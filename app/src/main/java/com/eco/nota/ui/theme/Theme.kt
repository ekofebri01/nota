package com.eco.nota.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class NotaColors(
    val backgroundMain: Color = ProBackground,
    val surfaceCard: Color = ProCardSurface,
    val brandPrimary: Color = AccentBlue,
    val brandSecondary: Color = AccentPurple,
    val textPrimary: Color = TextPrimary,
    val textSecondary: Color = TextGray
)

val LocalNotaColors = staticCompositionLocalOf { NotaColors() }
val LocalNotaTypography = staticCompositionLocalOf { NotaTypography() }

object NotaTheme {
    val colors: NotaColors
        @Composable
        get() = LocalNotaColors.current

    val typography: NotaTypography
        @Composable
        get() = LocalNotaTypography.current
}

@Composable
fun NotaTheme(
    content: @Composable () -> Unit
) {
    val colors = NotaColors()
    val typography = NotaTypography()

    CompositionLocalProvider(
        LocalNotaColors provides colors,
        LocalNotaTypography provides typography
    ) {
        MaterialTheme(
            colorScheme = darkColorScheme(
                primary = AccentBlue,
                background = ProBackground,
                surface = ProCardSurface
            ),
            content = content
        )
    }
}
