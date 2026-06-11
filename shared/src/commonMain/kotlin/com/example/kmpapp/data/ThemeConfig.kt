package com.example.kmpapp.data

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * 主题配置数据模型 —— 动态主题引擎的核心。
 *
 * 整个 App 的配色方案由一份 JSON 配置驱动，可以从本地内置主题切换，
 * 也可以从远程服务器加载新主题，无需重新发版。
 *
 * 使用 @Serializable + 自定义 HexColorSerializer 自动处理
 * Long ↔ "#RRGGBB" 的转换，JSON 输出与之前手工拼接完全兼容。
 */
@Serializable
data class ThemeConfig(
    val id: String,
    val name: String,
    val isDark: Boolean = false,
    val colors: ThemeColors,
    val cornerRadius: Int = 16,
    val cardElevation: Int = 2
)

/**
 * 主题颜色定义 —— 对应 Material 3 的完整色彩体系。
 * 使用 @Serializable(with = HexColorSerializer::class) 自动将
 * Long 值序列化为 "#RRGGBB" 格式字符串。
 */
@Serializable
data class ThemeColors(
    @Serializable(with = HexColorSerializer::class) val primary: Long,
    @Serializable(with = HexColorSerializer::class) val onPrimary: Long,
    @Serializable(with = HexColorSerializer::class) val primaryContainer: Long,
    @Serializable(with = HexColorSerializer::class) val onPrimaryContainer: Long,
    @Serializable(with = HexColorSerializer::class) val secondary: Long,
    @Serializable(with = HexColorSerializer::class) val onSecondary: Long,
    @Serializable(with = HexColorSerializer::class) val tertiary: Long,
    @Serializable(with = HexColorSerializer::class) val onTertiary: Long,
    @Serializable(with = HexColorSerializer::class) val tertiaryContainer: Long,
    @Serializable(with = HexColorSerializer::class) val onTertiaryContainer: Long,
    @Serializable(with = HexColorSerializer::class) val background: Long,
    @Serializable(with = HexColorSerializer::class) val onBackground: Long,
    @Serializable(with = HexColorSerializer::class) val surface: Long,
    @Serializable(with = HexColorSerializer::class) val onSurface: Long,
    @Serializable(with = HexColorSerializer::class) val surfaceVariant: Long,
    @Serializable(with = HexColorSerializer::class) val onSurfaceVariant: Long,
    @Serializable(with = HexColorSerializer::class) val error: Long,
    @Serializable(with = HexColorSerializer::class) val onError: Long
)

// ── 自定义序列化器 ──────────────────────────────────────────────────────────

/**
 * 将 Long 颜色值序列化为 "#RRGGBB" 格式的 JSON 字符串，
 * 反序列化时自动还原为带 alpha 通道的 Long 值。
 */
object HexColorSerializer : KSerializer<Long> {
    override val descriptor = PrimitiveSerialDescriptor("HexColor", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Long) {
        val rgb = value and 0x00FFFFFF
        encoder.encodeString("#${rgb.toString(16).padStart(6, '0').uppercase()}")
    }

    override fun deserialize(decoder: Decoder): Long {
        val hex = decoder.decodeString().removePrefix("#").removePrefix("0x")
        return hex.toLong(16) or 0xFF000000
    }
}

// ── 颜色工具函数 ──────────────────────────────────────────────────────────

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
