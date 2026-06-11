package com.example.kmpapp.data

import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

/**
 * 仪表盘服务 —— 负责配置的加载、缓存和持久化。
 *
 * 展示 KMP 的 "动态化" 能力：
 * - 配置存储在平台 Key-Value 存储中（SharedPreferences / NSUserDefaults）
 * - 可以从远程服务器加载新的 JSON 配置来改变首页布局
 * - 旧版本遇到未知卡片类型时优雅降级（显示占位卡片）
 *
 * 使用 kotlinx.serialization 自动处理 JSON，
 * 所有逻辑都在 commonMain，零平台代码。
 */
object DashboardService {
    private val storage = PlatformKeyValueStorage()
    private const val STORAGE_KEY = "kmp_dashboard_config"

    /** 从本地缓存加载配置，无缓存则返回默认配置 */
    fun loadConfig(): DashboardConfig {
        val cached = storage.getString(STORAGE_KEY)
        return if (cached != null) {
            try {
                AppJson.decodeFromString<DashboardConfig>(cached)
            } catch (e: Exception) {
                DashboardConfig.default()
            }
        } else {
            DashboardConfig.default()
        }
    }

    /** 保存配置到本地缓存 */
    fun saveConfig(config: DashboardConfig) {
        storage.putString(STORAGE_KEY, AppJson.encodeToString(config))
    }

    /** 从 JSON 字符串加载配置（可用于远程配置下发） */
    fun loadFromJson(json: String): DashboardConfig {
        val config = try {
            AppJson.decodeFromString<DashboardConfig>(json)
        } catch (e: Exception) {
            DashboardConfig.default()
        }
        saveConfig(config)
        return config
    }

    /** 重置为默认配置 */
    fun resetToDefault(): DashboardConfig {
        val config = DashboardConfig.default()
        saveConfig(config)
        return config
    }
}
