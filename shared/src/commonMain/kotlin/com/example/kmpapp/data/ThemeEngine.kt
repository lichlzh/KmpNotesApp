package com.example.kmpapp.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

/**
 * 主题引擎 —— 管理主题的加载、切换、缓存和导入/导出。
 *
 * 核心功能：
 * - 从平台存储加载/保存当前主题
 * - 切换内置主题或导入自定义 JSON 主题
 * - 导出当前主题为 JSON（可分享到其他设备）
 * - 通过 StateFlow 驱动 Compose UI 自动 recompose
 *
 * 使用 kotlinx.serialization 自动处理 JSON，
 * 所有逻辑在 commonMain，零平台代码。
 * 动态化体现：更换主题 JSON → 整个 App UI 自动变色，无需重新编译。
 */
object ThemeEngine {
    private val storage = PlatformKeyValueStorage()
    private const val STORAGE_KEY = "kmp_active_theme"

    private val _currentTheme = MutableStateFlow<ThemeConfig>(WarmOrangeTheme)
    val currentTheme: StateFlow<ThemeConfig> = _currentTheme.asStateFlow()

    init {
        loadSavedTheme()
    }

    /** 从平台存储加载上次保存的主题 */
    fun loadSavedTheme() {
        val saved = storage.getString(STORAGE_KEY)
        if (saved != null) {
            try {
                _currentTheme.value = AppJson.decodeFromString<ThemeConfig>(saved)
            } catch (e: Exception) {
                // 旧格式数据不兼容，使用默认
            }
        }
    }

    /** 切换到指定的内置或自定义主题 */
    fun applyTheme(config: ThemeConfig) {
        _currentTheme.value = config
        storage.putString(STORAGE_KEY, AppJson.encodeToString(config))
    }

    /** 通过主题 ID 切换内置主题 */
    fun applyThemeById(themeId: String) {
        val theme = builtinThemes.find { it.id == themeId }
        if (theme != null) {
            applyTheme(theme)
        }
    }

    /** 从 JSON 字符串导入自定义主题并应用 */
    fun importFromJson(json: String): Boolean {
        return try {
            val config = AppJson.decodeFromString<ThemeConfig>(json)
            val timestamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
            applyTheme(config.copy(id = "custom_$timestamp"))
            true
        } catch (e: Exception) {
            false
        }
    }

    /** 导出当前主题为 JSON 字符串 */
    fun exportCurrentTheme(): String {
        return AppJson.encodeToString(_currentTheme.value)
    }

    /** 重置为默认主题（暖阳橙） */
    fun resetToDefault() {
        applyTheme(WarmOrangeTheme)
    }

    /** 获取所有内置主题 */
    fun getBuiltinThemes(): List<ThemeConfig> = builtinThemes
}
