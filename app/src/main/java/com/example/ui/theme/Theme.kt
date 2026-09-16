package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val VNDarkColorScheme = darkColorScheme(
    primary = VNCyan,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF083344),
    onPrimaryContainer = VNCyan,
    secondary = VNPurple,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF3B0764),
    onSecondaryContainer = Color(0xFFF3E8FF),
    tertiary = VNAmber,
    onTertiary = Color.Black,
    background = VNBackground,
    onBackground = VNTextPrimary,
    surface = VNSurface,
    onSurface = VNTextPrimary,
    surfaceVariant = VNSurfaceVariant,
    onSurfaceVariant = VNTextSecondary,
    outline = VNBorder,
    outlineVariant = VNBorderLight,
    error = VNRose,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Editor is always pro studio dark theme
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = VNDarkColorScheme,
        typography = Typography,
        content = content
    )
}
