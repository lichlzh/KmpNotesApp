package com.example.kmpapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * 共享主题 —— Compose Multiplatform 让 Android/iOS 共享完全一致的 Material 3 主题。
 */

// 暖色调调色板
val WarmOrange = Color(0xFFF57C00)
val WarmOrangeLight = Color(0xFFFFB74D)
val WarmOrangeDark = Color(0xFFE65100)
val SurfaceLight = Color(0xFFFFFBFE)
val SurfaceDark = Color(0xFF1C1B1F)
val OnSurfaceLight = Color(0xFF1C1B1F)
val OnSurfaceDark = Color(0xFFE6E1E5)

private val LightColorScheme = lightColorScheme(
    primary = WarmOrange,
    onPrimary = Color.White,
    primaryContainer = WarmOrangeLight,
    onPrimaryContainer = WarmOrangeDark,
    secondary = Color(0xFF625B71),
    onSecondary = Color.White,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = Color(0xFFF5F0EB),
    onSurfaceVariant = Color(0xFF49454F),
    background = Color(0xFFF8F5F2),
    onBackground = OnSurfaceLight,
    error = Color(0xFFB3261E),
    onError = Color.White,
)

private val DarkColorScheme = darkColorScheme(
    primary = WarmOrangeLight,
    onPrimary = WarmOrangeDark,
    primaryContainer = WarmOrange,
    onPrimaryContainer = Color.White,
    secondary = Color(0xFFCCC2DC),
    onSecondary = Color(0xFF332D41),
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = Color(0xFF2B2930),
    onSurfaceVariant = Color(0xFFCAC4D0),
    background = Color(0xFF141218),
    onBackground = OnSurfaceDark,
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
)

val AppTypography = Typography()

@Composable
fun AppTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
