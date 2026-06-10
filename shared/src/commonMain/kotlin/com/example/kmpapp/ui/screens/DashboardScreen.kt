package com.example.kmpapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.kmpapp.data.ActionItem
import com.example.kmpapp.data.getPlatformName
import com.example.kmpapp.domain.DashboardViewModel
import com.example.kmpapp.ui.components.ActionsCard
import com.example.kmpapp.ui.components.QuoteCard
import com.example.kmpapp.ui.components.RecentNotesCard
import com.example.kmpapp.ui.components.StatsCard
import com.example.kmpapp.ui.components.WeatherCard

/**
 * 智能仪表盘首页 —— 服务端驱动（Server-Driven）的 Dashboard。
 *
 * 整个页面由 [DashboardConfig] 的 sections 列表驱动，
 * 每个 section 对应一种卡片类型，渲染引擎按 type 分发到对应的 Composable。
 *
 * 这是 KMP 动态化的核心展示：
 * - JSON 配置决定首页布局（增减卡片、改排列、改列数）
 * - 所有解析和渲染逻辑在 commonMain，Android/iOS 零平台代码
 * - 新增卡片类型只需添加 type 字符串 + Composable 渲染函数
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNoteClick: (Long?) -> Unit,
    onViewAllNotes: () -> Unit,
    onSearch: () -> Unit,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier
) {
    val config by viewModel.config.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val totalNotes by viewModel.totalNotes.collectAsState()
    val pinnedNotes by viewModel.pinnedNotes.collectAsState()
    val recentNotes by viewModel.recentNotes.collectAsState()
    val weatherNotes by viewModel.weatherNotes.collectAsState()

    val (greetEmoji, greetText) = viewModel.greeting

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 88.dp)
        ) {
            // ── 顶部问候区 ──────────────────────────────────
            item {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "$greetEmoji $greetText",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${viewModel.todayText} · ${getPlatformName()}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = onToggleTheme) {
                            Icon(
                                Icons.Default.Palette,
                                contentDescription = "主题设置",
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        IconButton(onClick = { viewModel.refreshConfig() }) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "\u5237\u65B0\u914D\u7F6E",
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }

            // ── 配置驱动的卡片区 ─────────────────────────────
            items(config.sections.size) { index ->
                val section = config.sections[index]

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    // 区块标题（quote 类型不显示标题，已在卡片内嵌）
                    if (section.title.isNotBlank() && section.type != "quote") {
                        Text(
                            text = section.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    // 按 type 分发渲染
                    when (section.type) {
                        "weather" -> {
                            WeatherCard(weatherService = viewModel.weather)
                        }

                        "actions" -> {
                            val actions = parseActionItems(section.config)
                            ActionsCard(
                                actions = actions,
                                onAction = { action ->
                                    when (action) {
                                        "create_note" -> onNoteClick(null)
                                        "view_all" -> onViewAllNotes()
                                        "search" -> onSearch()
                                        "toggle_theme" -> onToggleTheme()
                                    }
                                }
                            )
                        }

                        "stats" -> {
                            StatsCard(
                                totalNotes = totalNotes,
                                pinnedNotes = pinnedNotes,
                                recentNotes = recentNotes,
                                weatherNotes = weatherNotes
                            )
                        }

                        "quote" -> {
                            QuoteCard()
                        }

                        "note_list" -> {
                            val limit = (section.config["limit"] as? Number)?.toInt() ?: 5
                            val recentNotesList = viewModel.getRecentNotes(limit)
                            RecentNotesCard(
                                notes = recentNotesList,
                                onNoteClick = { noteId -> onNoteClick(noteId) }
                            )
                        }

                        else -> {
                            // 未知类型 → 优雅降级为占位卡片
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "\u672A\u77E5\u5361\u7247\u7C7B\u578B: ${section.type}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── FAB：新建笔记 ──────────────────────────────────
        ExtendedFloatingActionButton(
            onClick = { onNoteClick(null) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            text = { Text("\u65B0\u5EFA\u7B14\u8BB0") }
        )
    }
}

/**
 * 从 config map 中解析 actions 列表。
 * config 中的 "items" 字段存储为 JSON 字符串（数组格式）。
 */
private fun parseActionItems(config: Map<String, Any>): List<ActionItem> {
    val itemsJson = config["items"] as? String ?: return defaultActions()
    return try {
        val items = mutableListOf<ActionItem>()
        // 简单的 JSON 数组解析
        val body = itemsJson.removePrefix("[").removeSuffix("]").trim()
        if (body.isEmpty()) return defaultActions()

        var depth = 0
        var start = -1
        var inStr = false
        var i = 0
        while (i < body.length) {
            val c = body[i]
            if (inStr) {
                if (c == '\\') { i += 2; continue }
                if (c == '"') inStr = false
            } else {
                when (c) {
                    '"' -> inStr = true
                    '{' -> { if (depth == 0) start = i; depth++ }
                    '}' -> {
                        depth--
                        if (depth == 0 && start >= 0) {
                            val itemJson = body.substring(start, i + 1)
                            val map = com.example.kmpapp.data.parseJsonObject(itemJson)
                            items.add(
                                ActionItem(
                                    icon = map["icon"] as? String ?: "star",
                                    label = map["label"] as? String ?: "",
                                    action = map["action"] as? String ?: ""
                                )
                            )
                        }
                    }
                }
            }
            i++
        }

        if (items.isEmpty()) defaultActions() else items
    } catch (e: Exception) {
        defaultActions()
    }
}

/** 默认操作按钮（当配置解析失败时使用） */
private fun defaultActions() = listOf(
    ActionItem("add", "\u65B0\u5EFA\u7B14\u8BB0", "create_note"),
    ActionItem("list", "\u5168\u90E8\u7B14\u8BB0", "view_all"),
    ActionItem("search", "\u641C\u7D22", "search")
)
