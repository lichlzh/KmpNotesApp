package com.example.kmpapp.data

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * 主题配置数据模型 —— 动态主题引擎的核心。
 *
 * 整个 App 的配色方案由一份 JSON 配置驱动，可以从本地内置主题切换，
 * 也可以从远程服务器加载新主题，无需重新发版。
 *
 * 展示 KMP 动态化能力：同一套主题引擎代码，Android/iOS 行为完全一致，
 * Compose Multiplatform 的声明式 UI 让主题切换自动触发全 UI 树 recompose。
 */
data class ThemeConfig(
    val id: String,
    val name: String,
    val isDark: Boolean = false,
    val colors: ThemeColors,
    val cornerRadius: Int = 16,
    val cardElevation: Int = 2
) {
    companion object {
        fun fromJson(json: String): ThemeConfig? {
            return try {
                val map = parseJsonObject(json)
                ThemeConfig(
                    id = map["id"] as? String ?: "custom",
                    name = map["name"] as? String ?: "自定义",
                    isDark = map["isDark"] as? Boolean ?: false,
                    colors = parseColors(map["colors"] as? String ?: "{}"),
                    cornerRadius = (map["cornerRadius"] as? Number)?.toInt() ?: 16,
                    cardElevation = (map["cardElevation"] as? Number)?.toInt() ?: 2
                )
            } catch (e: Exception) {
                null
            }
        }

        fun toJson(config: ThemeConfig): String = buildString {
            append("{")
            append("\"id\":\"${config.id.escapeJson()}\",")
            append("\"name\":\"${config.name.escapeJson()}\",")
            append("\"isDark\":${config.isDark},")
            append("\"cornerRadius\":${config.cornerRadius},")
            append("\"cardElevation\":${config.cardElevation},")
            append("\"colors\":")
            append(colorsToJson(config.colors))
            append("}")
        }

        private fun parseColors(json: String): ThemeColors {
            val map = parseJsonObject(json)
            return ThemeColors(
                primary = (map["primary"] as? String)?.toComposeColor() ?: 0xFFF57C00,
                onPrimary = (map["onPrimary"] as? String)?.toComposeColor() ?: 0xFFFFFFFF,
                primaryContainer = (map["primaryContainer"] as? String)?.toComposeColor() ?: 0xFFFFB74D,
                onPrimaryContainer = (map["onPrimaryContainer"] as? String)?.toComposeColor() ?: 0xFFE65100,
                secondary = (map["secondary"] as? String)?.toComposeColor() ?: 0xFF625B71,
                onSecondary = (map["onSecondary"] as? String)?.toComposeColor() ?: 0xFFFFFFFF,
                tertiary = (map["tertiary"] as? String)?.toComposeColor() ?: 0xFF7D5260,
                onTertiary = (map["onTertiary"] as? String)?.toComposeColor() ?: 0xFFFFFFFF,
                tertiaryContainer = (map["tertiaryContainer"] as? String)?.toComposeColor() ?: 0xFFFFD8E4,
                onTertiaryContainer = (map["onTertiaryContainer"] as? String)?.toComposeColor() ?: 0xFF31111D,
                background = (map["background"] as? String)?.toComposeColor() ?: 0xFFF8F5F2,
                onBackground = (map["onBackground"] as? String)?.toComposeColor() ?: 0xFF1C1B1F,
                surface = (map["surface"] as? String)?.toComposeColor() ?: 0xFFFFFBFE,
                onSurface = (map["onSurface"] as? String)?.toComposeColor() ?: 0xFF1C1B1F,
                surfaceVariant = (map["surfaceVariant"] as? String)?.toComposeColor() ?: 0xFFF5F0EB,
                onSurfaceVariant = (map["onSurfaceVariant"] as? String)?.toComposeColor() ?: 0xFF49454F,
                error = (map["error"] as? String)?.toComposeColor() ?: 0xFFB3261E,
                onError = (map["onError"] as? String)?.toComposeColor() ?: 0xFFFFFFFF
            )
        }

        private fun colorsToJson(colors: ThemeColors): String = buildString {
            append("{")
            append("\"primary\":\"${colors.primary.toHex()}\",")
            append("\"onPrimary\":\"${colors.onPrimary.toHex()}\",")
            append("\"primaryContainer\":\"${colors.primaryContainer.toHex()}\",")
            append("\"onPrimaryContainer\":\"${colors.onPrimaryContainer.toHex()}\",")
            append("\"secondary\":\"${colors.secondary.toHex()}\",")
            append("\"onSecondary\":\"${colors.onSecondary.toHex()}\",")
            append("\"tertiary\":\"${colors.tertiary.toHex()}\",")
            append("\"onTertiary\":\"${colors.onTertiary.toHex()}\",")
            append("\"tertiaryContainer\":\"${colors.tertiaryContainer.toHex()}\",")
            append("\"onTertiaryContainer\":\"${colors.onTertiaryContainer.toHex()}\",")
            append("\"background\":\"${colors.background.toHex()}\",")
            append("\"onBackground\":\"${colors.onBackground.toHex()}\",")
            append("\"surface\":\"${colors.surface.toHex()}\",")
            append("\"onSurface\":\"${colors.onSurface.toHex()}\",")
            append("\"surfaceVariant\":\"${colors.surfaceVariant.toHex()}\",")
            append("\"onSurfaceVariant\":\"${colors.onSurfaceVariant.toHex()}\",")
            append("\"error\":\"${colors.error.toHex()}\",")
            append("\"onError\":\"${colors.onError.toHex()}\"")
            append("}")
        }
    }
}

/**
 * 主题颜色定义 —— 对应 Material 3 的完整色彩体系。
 */
data class ThemeColors(
    val primary: Long,
    val onPrimary: Long,
    val primaryContainer: Long,
    val onPrimaryContainer: Long,
    val secondary: Long,
    val onSecondary: Long,
    val tertiary: Long,
    val onTertiary: Long,
    val tertiaryContainer: Long,
    val onTertiaryContainer: Long,
    val background: Long,
    val onBackground: Long,
    val surface: Long,
    val onSurface: Long,
    val surfaceVariant: Long,
    val onSurfaceVariant: Long,
    val error: Long,
    val onError: Long
)

// ── 颜色工具函数 ──────────────────────────────────────────────────────────

/** 将 #RRGGBB 格式的字符串转为 Long 颜色值 */
internal fun String.toComposeColor(): Long {
    val hex = removePrefix("#").removePrefix("0x")
    return hex.toLong(16) or 0xFF000000
}

/** 将 Long 颜色值转为 #RRGGBB 格式字符串 */
internal fun Long.toHex(): String {
    val rgb = this and 0x00FFFFFF
    return "#${rgb.toString(16).padStart(6, '0').uppercase()}"
}

/** 将 Long 颜色值转为 Compose Color 对象 */
internal fun Long.toColor(): Color = Color(this.toInt())

// ── 内置主题 ──────────────────────────────────────────────────────────────

/** 暖阳橙 —— 项目默认主题 */
val WarmOrangeTheme = ThemeConfig(
    id = "warm_orange",
    name = "暖阳橙",
    isDark = false,
    colors = ThemeColors(
        primary = 0xFFF57C00,
        onPrimary = 0xFFFFFFFF,
        primaryContainer = 0xFFFFB74D,
        onPrimaryContainer = 0xFFE65100,
        secondary = 0xFF625B71,
        onSecondary = 0xFFFFFFFF,
        tertiary = 0xFF7D5260,
        onTertiary = 0xFFFFFFFF,
        tertiaryContainer = 0xFFFFD8E4,
        onTertiaryContainer = 0xFF31111D,
        background = 0xFFF8F5F2,
        onBackground = 0xFF1C1B1F,
        surface = 0xFFFFFBFE,
        onSurface = 0xFF1C1B1F,
        surfaceVariant = 0xFFF5F0EB,
        onSurfaceVariant = 0xFF49454F,
        error = 0xFFB3261E,
        onError = 0xFFFFFFFF
    )
)

/** 深海蓝 —— 沉稳专业 */
val OceanBlueTheme = ThemeConfig(
    id = "ocean_blue",
    name = "深海蓝",
    isDark = false,
    colors = ThemeColors(
        primary = 0xFF1565C0,
        onPrimary = 0xFFFFFFFF,
        primaryContainer = 0xFFD1E4FF,
        onPrimaryContainer = 0xFF001D36,
        secondary = 0xFF546E7A,
        onSecondary = 0xFFFFFFFF,
        tertiary = 0xFF6750A4,
        onTertiary = 0xFFFFFFFF,
        tertiaryContainer = 0xFFEADDFF,
        onTertiaryContainer = 0xFF21005D,
        background = 0xFFFDFBFF,
        onBackground = 0xFF1A1C1E,
        surface = 0xFFFDFBFF,
        onSurface = 0xFF1A1C1E,
        surfaceVariant = 0xFFDFE2EB,
        onSurfaceVariant = 0xFF43474E,
        error = 0xFFBA1A1A,
        onError = 0xFFFFFFFF
    )
)

/** 森林绿 —— 自然清新 */
val ForestGreenTheme = ThemeConfig(
    id = "forest_green",
    name = "森林绿",
    isDark = false,
    colors = ThemeColors(
        primary = 0xFF2E7D32,
        onPrimary = 0xFFFFFFFF,
        primaryContainer = 0xFFA5D6A7,
        onPrimaryContainer = 0xFF002106,
        secondary = 0xFF526350,
        onSecondary = 0xFFFFFFFF,
        tertiary = 0xFF39656B,
        onTertiary = 0xFFFFFFFF,
        tertiaryContainer = 0xFFBCEBF1,
        onTertiaryContainer = 0xFF001F24,
        background = 0xFFFCFDF7,
        onBackground = 0xFF1A1C19,
        surface = 0xFFFCFDF7,
        onSurface = 0xFF1A1C19,
        surfaceVariant = 0xFFDEE5D9,
        onSurfaceVariant = 0xFF424940,
        error = 0xFFBA1A1A,
        onError = 0xFFFFFFFF
    )
)

/** 暗夜紫 —— 深色模式 */
val NightPurpleTheme = ThemeConfig(
    id = "night_purple",
    name = "暗夜紫",
    isDark = true,
    colors = ThemeColors(
        primary = 0xFFD0BCFF,
        onPrimary = 0xFF381E72,
        primaryContainer = 0xFF4F378B,
        onPrimaryContainer = 0xFFEADDFF,
        secondary = 0xFFCCC2DC,
        onSecondary = 0xFF332D41,
        tertiary = 0xFFEFB8C8,
        onTertiary = 0xFF492532,
        tertiaryContainer = 0xFF633B48,
        onTertiaryContainer = 0xFFFFD8E4,
        background = 0xFF141218,
        onBackground = 0xFFE6E1E5,
        surface = 0xFF1C1B1F,
        onSurface = 0xFFE6E1E5,
        surfaceVariant = 0xFF2B2930,
        onSurfaceVariant = 0xFFCAC4D0,
        error = 0xFFF2B8B5,
        onError = 0xFF601410
    )
)

/** 所有内置主题 */
val builtinThemes = listOf(WarmOrangeTheme, OceanBlueTheme, ForestGreenTheme, NightPurpleTheme)
