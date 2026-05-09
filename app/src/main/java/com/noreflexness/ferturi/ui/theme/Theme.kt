package com.noreflexness.ferturi.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = Color(0xFF2E7D32),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB8E6B8),
    onPrimaryContainer = Color(0xFF002106),
    secondary = Color(0xFF52634F),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD5E8CF),
    onSecondaryContainer = Color(0xFF111F0F),
    tertiary = Color(0xFF38656A),
    background = Color(0xFFFCFDF6),
    surface = Color(0xFFFCFDF6),
    surfaceVariant = Color(0xFFDEE5D9),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF9CD49B),
    onPrimary = Color(0xFF003910),
    primaryContainer = Color(0xFF155322),
    onPrimaryContainer = Color(0xFFB8F0B8),
    secondary = Color(0xFFB9CCB4),
    onSecondary = Color(0xFF253423),
    secondaryContainer = Color(0xFF3B4B38),
    onSecondaryContainer = Color(0xFFD5E8CF),
    tertiary = Color(0xFF9FCDD3),
    background = Color(0xFF1A1C18),
    surface = Color(0xFF1A1C18),
    surfaceVariant = Color(0xFF424940),
)

@Composable
fun FerturiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val ctx = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content,
    )
}
