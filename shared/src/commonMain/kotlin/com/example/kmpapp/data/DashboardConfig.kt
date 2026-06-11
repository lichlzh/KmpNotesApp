package com.example.kmpapp.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

/**
 * 仪表盘配置数据模型 —— 服务端驱动的动态首页核心。
 *
 * 整个 Dashboard 的布局和内容完全由 [DashboardConfig] 描述，
 * 可以从 JSON（本地缓存或远程服务）加载，也可以在代码中硬编码默认值。
 *
 * 这就是 KMP "动态化"的关键：同一份解析和渲染代码，
 * Android 和 iOS 上行为完全一致，改配置即可改界面。
 */
@Serializable
data class DashboardConfig(
    val version: Int = 1,
    val sections: List<DashboardSection> = emptyList()
) {
    companion object {
        /** 默认仪表盘配置 —— 首次安装时使用 */
        fun default() = DashboardConfig(
            version = 1,
            sections = listOf(
                DashboardSection("weather", "weather", "今日天气", "full_width"),
                DashboardSection("quote", "quote", "每日一言", "full_width"),
                DashboardSection("actions", "actions", "快捷操作", "full_width",
                    config = jsonConfig(
                        "items" to JsonArray(listOf(
                            JsonObject(mapOf(
                                "icon" to JsonPrimitive("add"),
                                "label" to JsonPrimitive("新建笔记"),
                                "action" to JsonPrimitive("create_note")
                            )),
                            JsonObject(mapOf(
                                "icon" to JsonPrimitive("list"),
                                "label" to JsonPrimitive("全部笔记"),
                                "action" to JsonPrimitive("view_all")
                            )),
                            JsonObject(mapOf(
                                "icon" to JsonPrimitive("search"),
                                "label" to JsonPrimitive("搜索"),
                                "action" to JsonPrimitive("search")
                            ))
                        ))
                    )
                ),
                DashboardSection("stats", "stats", "数据统计", "full_width"),
                DashboardSection("recent", "note_list", "最近笔记", "full_width",
                    config = jsonConfig(
                        "limit" to JsonPrimitive(5),
                        "sortBy" to JsonPrimitive("updatedAt")
                    )
                )
            )
        )
    }
}

/**
 * 仪表盘区块 —— 每个区块对应一张卡片。
 *
 * @param id 唯一标识
 * @param type 卡片类型：weather / quote / actions / stats / note_list
 * @param title 区块标题（显示在卡片上方）
 * @param size 尺寸：full_width / half_width
 * @param config 额外配置，使用 JsonElement 支持任意 JSON 值类型
 */
@Serializable
data class DashboardSection(
    val id: String = "",
    val type: String = "",
    val title: String = "",
    val size: String = "full_width",
    val config: Map<String, JsonElement> = emptyMap()
)

/**
 * 快捷操作按钮数据
 */
@Serializable
data class ActionItem(
    val icon: String = "star",
    val label: String = "",
    val action: String = ""
)

// ── JsonElement 工具函数 ──────────────────────────────────────────────────

/** 快捷构建 Map<String, JsonElement> 配置 */
fun jsonConfig(vararg pairs: Pair<String, JsonElement>): Map<String, JsonElement> = mapOf(*pairs)

/** 从 config map 中提取 ActionItem 列表 */
fun Map<String, JsonElement>.getActionItems(): List<ActionItem> {
    val itemsElement = this["items"] ?: return defaultActionItems()
    return try {
        itemsElement.jsonArray.map { element ->
            val obj = element as JsonObject
            ActionItem(
                icon = obj["icon"]?.jsonPrimitive?.content ?: "star",
                label = obj["label"]?.jsonPrimitive?.content ?: "",
                action = obj["action"]?.jsonPrimitive?.content ?: ""
            )
        }
    } catch (e: Exception) {
        defaultActionItems()
    }
}

/** 从 config map 中提取 Int 值 */
fun Map<String, JsonElement>.getInt(key: String, default: Int = 0): Int {
    return try { this[key]?.jsonPrimitive?.int ?: default } catch (e: Exception) { default }
}

/** 从 config map 中提取 String 值 */
fun Map<String, JsonElement>.getString(key: String, default: String = ""): String {
    return try { this[key]?.jsonPrimitive?.content ?: default } catch (e: Exception) { default }
}

/** 默认操作按钮 */
fun defaultActionItems() = listOf(
    ActionItem("add", "新建笔记", "create_note"),
    ActionItem("list", "全部笔记", "view_all"),
    ActionItem("search", "搜索", "search")
)
