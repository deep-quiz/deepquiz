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

private val PremiumDarkColorScheme = darkColorScheme(
    primary = PremiumPrimary,
    onPrimary = PremiumOnBackground,
    secondary = PremiumSecondary,
    onSecondary = PremiumOnBackground,
    background = PremiumBackground,
    onBackground = PremiumOnBackground,
    surface = PremiumSurface,
    onSurface = PremiumOnSurface,
    surfaceVariant = PremiumSurface,
    onSurfaceVariant = PremiumOnSurfaceVariant,
    error = PremiumError,
    onError = PremiumOnBackground
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force dark theme
    dynamicColor: Boolean = false, // Disable dynamic colors to keep premium colors
    content: @Composable () -> Unit,
) {
    val colorScheme = PremiumDarkColorScheme

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
