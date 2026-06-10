package com.example.kmpapp.domain

import androidx.lifecycle.ViewModel
import com.example.kmpapp.data.DashboardConfig
import com.example.kmpapp.data.DashboardService
import com.example.kmpapp.data.Note
import com.example.kmpapp.data.NoteRepository
import com.example.kmpapp.data.WeatherService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn

/**
 * 仪表盘 ViewModel —— 智能首页的状态管理。
 *
 * 在 commonMain 中实现，Android 和 iOS 共享。
 * 负责：
 * - 加载和缓存仪表盘配置
 * - 聚合笔记统计数据（总数、置顶、本周新建、含天气快照）
 * - 管理天气服务
 * - 提供时间相关的问候语和日期显示
 *
 * 使用 object 单例，确保与 NotesViewModel 共享同一个 NoteRepository 实例。
 */
object DashboardViewModel : ViewModel() {

    private val repository = NoteRepository
    private val weatherService = WeatherService()

    private val _config = MutableStateFlow(DashboardConfig.default())
    val config: StateFlow<DashboardConfig> = _config.asStateFlow()

    /** 所有笔记（响应式，来自 Repository 的 StateFlow） */
    val notes: StateFlow<List<Note>> = repository.notes

    /** 天气服务（供 Dashboard 天气卡片使用） */
    val weather: WeatherService = weatherService

    // 统计数据（在 refreshStats 中更新）
    private val _totalNotes = MutableStateFlow(0)
    val totalNotes: StateFlow<Int> = _totalNotes.asStateFlow()

    private val _pinnedNotes = MutableStateFlow(0)
    val pinnedNotes: StateFlow<Int> = _pinnedNotes.asStateFlow()

    private val _recentNotes = MutableStateFlow(0)
    val recentNotes: StateFlow<Int> = _recentNotes.asStateFlow()

    private val _weatherNotes = MutableStateFlow(0)
    val weatherNotes: StateFlow<Int> = _weatherNotes.asStateFlow()

    /** 当前问候语（根据时间段） */
    val greeting: Pair<String, String>
        get() {
            val hour = Clock.System.now()
                .toLocalDateTime(TimeZone.currentSystemDefault()).hour
            return when {
                hour in 5..11 -> "\uD83C\uDF1E" to "早上好"
                hour in 12..13 -> "☀️" to "中午好"
                hour in 14..17 -> "\uD83C\uDF24" to "下午好"
                hour in 18..21 -> "\uD83C\uDF19" to "晚上好"
                else -> "\uD83C\uDF1F" to "夜深了"
            }
        }

    /** 今天的日期文字 */
    val todayText: String
        get() {
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            return "${today.monthNumber}月${today.dayOfMonth}日"
        }

    init {
        _config.value = DashboardService.loadConfig()
        weatherService.fetchWeather()
        refreshStats()
    }

    /** 刷新统计数据 */
    fun refreshStats() {
        val allNotes = repository.notes.value
        _totalNotes.value = allNotes.size
        _pinnedNotes.value = allNotes.count { it.isPinned }
        _weatherNotes.value = allNotes.count { it.weatherSnapshot != null }

        // 近 7 天的笔记
        val sevenDaysAgo = Clock.System.now().toEpochMilliseconds() - 7L * 24 * 60 * 60 * 1000
        _recentNotes.value = allNotes.count { it.updatedAt >= sevenDaysAgo }
    }

    /** 获取最近的笔记（按更新时间倒序） */
    fun getRecentNotes(limit: Int = 5): List<Note> {
        return notes.value
            .sortedByDescending { it.updatedAt }
            .take(limit)
    }

    /** 刷新配置（从缓存/默认值重新加载） */
    fun refreshConfig() {
        _config.value = DashboardService.loadConfig()
        refreshStats()
    }

    /** 从 JSON 加载新配置（模拟远程配置下发） */
    fun loadConfigFromJson(json: String) {
        _config.value = DashboardService.loadFromJson(json)
    }

    /** 重置为默认配置 */
    fun resetConfig() {
        _config.value = DashboardService.resetToDefault()
    }
}
