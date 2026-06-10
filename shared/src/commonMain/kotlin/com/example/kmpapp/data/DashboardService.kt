package com.example.kmpapp.data

/**
 * 仪表盘服务 —— 负责配置的加载、缓存和持久化。
 *
 * 展示 KMP 的 "动态化" 能力：
 * - 配置存储在平台 Key-Value 存储中（SharedPreferences / NSUserDefaults）
 * - 可以从远程服务器加载新的 JSON 配置来改变首页布局
 * - 旧版本遇到未知卡片类型时优雅降级（显示占位卡片）
 *
 * 所有逻辑都在 commonMain，零平台代码。
 */
object DashboardService {
    private val storage = PlatformKeyValueStorage()
    private const val STORAGE_KEY = "kmp_dashboard_config"

    /** 从本地缓存加载配置，无缓存则返回默认配置 */
    fun loadConfig(): DashboardConfig {
        val cached = storage.getString(STORAGE_KEY)
        return if (cached != null) {
            DashboardConfig.fromJson(cached)
        } else {
            DashboardConfig.default()
        }
    }

    /** 保存配置到本地缓存 */
    fun saveConfig(config: DashboardConfig) {
        storage.putString(STORAGE_KEY, DashboardConfig.toJson(config))
    }

    /** 从 JSON 字符串加载配置（可用于远程配置下发） */
    fun loadFromJson(json: String): DashboardConfig {
        val config = DashboardConfig.fromJson(json)
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
