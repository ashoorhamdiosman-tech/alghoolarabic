package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldLight,
    onPrimary = Slate900,
    primaryContainer = EmeraldDark,
    onPrimaryContainer = MintContainer,
    secondary = EmeraldSecondary,
    onSecondary = Color.White,
    secondaryContainer = Slate800,
    onSecondaryContainer = MintLight,
    background = Slate900,
    onBackground = Slate50,
    surface = Slate800,
    onSurface = Slate50,
    surfaceVariant = Slate700,
    onSurfaceVariant = Slate200,
    outline = Slate600
)

private val LightColorScheme = lightColorScheme(
    primary = EmeraldPrimary,
    onPrimary = EmeraldOnPrimary,
    primaryContainer = MintContainer,
    onPrimaryContainer = EmeraldDark,
    secondary = EmeraldSecondary,
    onSecondary = Color.White,
    secondaryContainer = EmeraldSecondaryContainer,
    onSecondaryContainer = EmeraldDark,
    background = Color(0xFFF7FCFA),
    onBackground = Slate900,
    surface = Color.White,
    onSurface = Slate900,
    surfaceVariant = MintLight,
    onSurfaceVariant = Slate700,
    outline = Slate200
)

@Composable
fun EmeraldTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MyApplicationTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent emerald branding
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
