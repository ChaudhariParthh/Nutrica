package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldGreen,
    secondary = CobaltBlue,
    tertiary = GoldenAmber,
    background = NightDark,
    surface = NightCard,
    onPrimary = Color(0xFF090A0C), // Dark text on lime primary
    onSecondary = PureWhite,
    onBackground = NightTextLight,
    onSurface = NightTextLight,
    outline = NightBorder
)

private val LightColorScheme = lightColorScheme(
    primary = EmeraldGreen,
    secondary = CobaltBlue,
    tertiary = GoldenAmber,
    background = Color(0xFFF8FAFC), // Beautiful clean white/light gray background
    surface = Color(0xFFFFFFFF),    // White card surfaces
    onPrimary = Color(0xFF090A0C),  // Dark text on lime primary
    onSecondary = Color(0xFFFFFFFF),
    onBackground = Color(0xFF0F172A), // Slate 900 for high-contrast dark text
    onSurface = Color(0xFF1E293B),    // Slate 800 for card text
    outline = Color(0xFFE2E8F0)       // Slate 200 for clean light outlines/borders
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disable system dynamic color so our custom hand-crafted look is consistently beautiful
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
