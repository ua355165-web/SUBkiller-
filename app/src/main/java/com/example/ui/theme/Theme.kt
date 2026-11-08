package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val FinanceDarkColorScheme = darkColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color(0xFF022C22),
    primaryContainer = EmeraldDark,
    onPrimaryContainer = EmeraldGlow,
    secondary = CyanAccent,
    onSecondary = Color(0xFF083344),
    secondaryContainer = Color(0xFF164E63),
    onSecondaryContainer = CyanGlow,
    tertiary = PurplePremium,
    onTertiary = Color.White,
    background = FinanceObsidian,
    onBackground = TextPrimary,
    surface = FinanceSurface,
    onSurface = TextPrimary,
    surfaceVariant = FinanceCard,
    onSurfaceVariant = TextSecondary,
    outline = FinanceBorder,
    outlineVariant = FinanceBorderLight,
    error = CrimsonAlert,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Finance style dark mode
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = FinanceDarkColorScheme,
        typography = Typography,
        content = content
    )
}
