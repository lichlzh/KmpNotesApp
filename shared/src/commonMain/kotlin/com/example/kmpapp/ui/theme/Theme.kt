package com.example.kmpapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import com.example.kmpapp.data.ThemeConfig
import com.example.kmpapp.data.ThemeEngine
import com.example.kmpapp.data.toColor

/**
 * 共享主题 —— Compose Multiplatform 让 Android/iOS 共享完全一致的 Material 3 主题。
 *
 * 保留原始静态配色（兼容无动态主题场景），同时新增 [DynamicAppTheme] 支持运行时切换。
 */

// 暖色调调色板（默认静态主题）
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

/** 原始静态主题（保留向后兼容） */
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

/**
 * 动态主题 —— 从 ThemeEngine 读取当前主题配置，构建 Material 3 ColorScheme。
 *
 * 这是动态化的核心：ThemeEngine 的 currentTheme StateFlow 变化时，
 * collectAsState() 会触发 recompose，整个 UI 树自动更新颜色。
 *
 * 切换主题 = 替换一份 JSON → Compose 自动渲染新颜色，零平台代码。
 */
@Composable
fun DynamicAppTheme(
    content: @Composable () -> Unit
) {
    val themeConfig by ThemeEngine.currentTheme.collectAsState()

    val colorScheme = themeConfig.toMaterialColorScheme()

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}

/** 将 ThemeConfig 转为 Material 3 的 ColorScheme */
private fun ThemeConfig.toMaterialColorScheme() = with(colors) {
    if (isDark) {
        darkColorScheme(
            primary = primary.toColor(),
            onPrimary = onPrimary.toColor(),
            primaryContainer = primaryContainer.toColor(),
            onPrimaryContainer = onPrimaryContainer.toColor(),
            secondary = secondary.toColor(),
            onSecondary = onSecondary.toColor(),
            tertiary = tertiary.toColor(),
            onTertiary = onTertiary.toColor(),
            tertiaryContainer = tertiaryContainer.toColor(),
            onTertiaryContainer = onTertiaryContainer.toColor(),
            background = background.toColor(),
            onBackground = onBackground.toColor(),
            surface = surface.toColor(),
            onSurface = onSurface.toColor(),
            surfaceVariant = surfaceVariant.toColor(),
            onSurfaceVariant = onSurfaceVariant.toColor(),
            error = error.toColor(),
            onError = onError.toColor()
        )
    } else {
        lightColorScheme(
            primary = primary.toColor(),
            onPrimary = onPrimary.toColor(),
            primaryContainer = primaryContainer.toColor(),
            onPrimaryContainer = onPrimaryContainer.toColor(),
            secondary = secondary.toColor(),
            onSecondary = onSecondary.toColor(),
            tertiary = tertiary.toColor(),
            onTertiary = onTertiary.toColor(),
            tertiaryContainer = tertiaryContainer.toColor(),
            onTertiaryContainer = onTertiaryContainer.toColor(),
            background = background.toColor(),
            onBackground = onBackground.toColor(),
            surface = surface.toColor(),
            onSurface = onSurface.toColor(),
            surfaceVariant = surfaceVariant.toColor(),
            onSurfaceVariant = onSurfaceVariant.toColor(),
            error = error.toColor(),
            onError = onError.toColor()
        )
    }
}
