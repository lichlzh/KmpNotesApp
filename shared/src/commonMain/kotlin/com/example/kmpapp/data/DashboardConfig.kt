package com.example.kmpapp.data

/**
 * 仪表盘配置数据模型 —— 服务端驱动的动态首页核心。
 *
 * 整个 Dashboard 的布局和内容完全由 [DashboardConfig] 描述，
 * 可以从 JSON（本地缓存或远程服务）加载，也可以在代码中硬编码默认值。
 *
 * 这就是 KMP "动态化"的关键：同一份解析和渲染代码，
 * Android 和 iOS 上行为完全一致，改配置即可改界面。
 */
data class DashboardConfig(
    val version: Int = 1,
    val sections: List<DashboardSection> = emptyList()
) {
    companion object {
        fun fromJson(json: String): DashboardConfig {
            if (json.isBlank()) return default()
            return try {
                val map = parseJsonObject(json)
                DashboardConfig(
                    version = (map["version"] as? Number)?.toInt() ?: 1,
                    sections = parseSectionsArray(json)
                )
            } catch (e: Exception) {
                default()
            }
        }

        fun toJson(config: DashboardConfig): String = buildString {
            append("{")
            append("\"version\":${config.version},")
            append("\"sections\":[")
            config.sections.forEachIndexed { index, section ->
                if (index > 0) append(",")
                append("{")
                append("\"id\":\"${section.id.escapeJson()}\",")
                append("\"type\":\"${section.type.escapeJson()}\",")
                append("\"title\":\"${section.title.escapeJson()}\",")
                append("\"size\":\"${section.size.escapeJson()}\"")
                if (section.config.isNotEmpty()) {
                    append(",\"config\":{")
                    section.config.entries.forEachIndexed { i, (k, v) ->
                        if (i > 0) append(",")
                        append("\"$k\":")
                        when (v) {
                            is Number -> append(v)
                            is Boolean -> append(v)
                            else -> append("\"${(v as String).escapeJson()}\"")
                        }
                    }
                    append("}")
                }
                append("}")
            }
            append("]}")
        }

        /** 解析 sections 数组（手工解析以处理嵌套对象） */
        private fun parseSectionsArray(json: String): List<DashboardSection> {
            val sections = mutableListOf<DashboardSection>()
            val sectionsIdx = json.indexOf("\"sections\"")
            if (sectionsIdx < 0) return sections

            val arrayStart = json.indexOf('[', sectionsIdx)
            if (arrayStart < 0) return sections

            // 找到匹配的 ]
            var depth = 0
            var arrayEnd = arrayStart
            for (i in arrayStart until json.length) {
                when (json[i]) {
                    '[' -> depth++
                    ']' -> { depth--; if (depth == 0) { arrayEnd = i; break } }
                }
            }

            val arrayBody = json.substring(arrayStart + 1, arrayEnd)

            // 逐个提取 { } 对象
            var objDepth = 0
            var objStart = -1
            var inStr = false
            var k = 0
            while (k < arrayBody.length) {
                val c = arrayBody[k]
                if (inStr) {
                    if (c == '\\') { k += 2; continue }
                    if (c == '"') inStr = false
                } else {
                    when (c) {
                        '"' -> inStr = true
                        '{' -> { if (objDepth == 0) objStart = k; objDepth++ }
                        '}' -> {
                            objDepth--
                            if (objDepth == 0 && objStart >= 0) {
                                val sectionJson = arrayBody.substring(objStart, k + 1)
                                val map = parseJsonObject(sectionJson)
                                sections.add(
                                    DashboardSection(
                                        id = map["id"] as? String ?: "section_${sections.size}",
                                        type = map["type"] as? String ?: "unknown",
                                        title = map["title"] as? String ?: "",
                                        size = map["size"] as? String ?: "full_width",
                                        config = parseActionConfig(sectionJson)
                                    )
                                )
                            }
                        }
                    }
                }
                k++
            }

            return sections
        }

        /** 解析 section 中的 config 子对象 */
        private fun parseActionConfig(sectionJson: String): Map<String, Any> {
            val config = mutableMapOf<String, Any>()
            val configIdx = sectionJson.indexOf("\"config\"")
            if (configIdx < 0) return config

            val braceStart = sectionJson.indexOf('{', configIdx)
            if (braceStart < 0) return config

            var depth = 0
            var braceEnd = braceStart
            for (i in braceStart until sectionJson.length) {
                when (sectionJson[i]) {
                    '{' -> depth++
                    '}' -> { depth--; if (depth == 0) { braceEnd = i; break } }
                }
            }

            val configJson = sectionJson.substring(braceStart, braceEnd + 1)

            // 检查是否有 items 数组（actions 类型）
            val itemsIdx = configJson.indexOf("\"items\"")
            if (itemsIdx >= 0) {
                val items = parseActionItems(configJson)
                if (items.isNotEmpty()) {
                    // 将 items 序列化为 JSON 字符串存储
                    config["items"] = buildString {
                        append("[")
                        items.forEachIndexed { idx, item ->
                            if (idx > 0) append(",")
                            append("{\"icon\":\"${item.icon}\",\"label\":\"${item.label}\",\"action\":\"${item.action}\"}")
                        }
                        append("]")
                    }
                }
            } else {
                // 简单 key-value config
                val map = parseJsonObject(configJson)
                map.forEach { (k, v) ->
                    if (v != null) config[k] = v
                }
            }

            return config
        }

        /** 解析 actions 卡片中的 items 数组 */
        private fun parseActionItems(configJson: String): List<ActionItem> {
            val items = mutableListOf<ActionItem>()
            val itemsIdx = configJson.indexOf("\"items\"")
            if (itemsIdx < 0) return items

            val arrayStart = configJson.indexOf('[', itemsIdx)
            if (arrayStart < 0) return items

            var depth = 0
            var arrayEnd = arrayStart
            for (i in arrayStart until configJson.length) {
                when (configJson[i]) {
                    '[' -> depth++
                    ']' -> { depth--; if (depth == 0) { arrayEnd = i; break } }
                }
            }

            val arrayBody = configJson.substring(arrayStart + 1, arrayEnd)
            var objDepth = 0
            var objStart = -1
            var inStr = false
            var j = 0
            while (j < arrayBody.length) {
                val c = arrayBody[j]
                if (inStr) {
                    if (c == '\\') { j += 2; continue }
                    if (c == '"') inStr = false
                } else {
                    when (c) {
                        '"' -> inStr = true
                        '{' -> { if (objDepth == 0) objStart = j; objDepth++ }
                        '}' -> {
                            objDepth--
                            if (objDepth == 0 && objStart >= 0) {
                                val itemJson = arrayBody.substring(objStart, j + 1)
                                val map = parseJsonObject(itemJson)
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
                j++
            }

            return items
        }

        /** 默认仪表盘配置 —— 首次安装时使用 */
        fun default() = DashboardConfig(
            version = 1,
            sections = listOf(
                DashboardSection("weather", "weather", "今日天气", "full_width"),
                DashboardSection("quote", "quote", "每日一言", "full_width"),
                DashboardSection("actions", "actions", "快捷操作", "full_width",
                    config = mapOf(
                        "items" to "[{\"icon\":\"add\",\"label\":\"新建笔记\",\"action\":\"create_note\"},{\"icon\":\"list\",\"label\":\"全部笔记\",\"action\":\"view_all\"},{\"icon\":\"search\",\"label\":\"搜索\",\"action\":\"search\"}]"
                    )
                ),
                DashboardSection("stats", "stats", "数据统计", "full_width"),
                DashboardSection("recent", "note_list", "最近笔记", "full_width",
                    config = mapOf("limit" to 5, "sortBy" to "updatedAt")
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
 * @param config 额外配置（JSON 中的 config 对象，解析为 key-value）
 */
data class DashboardSection(
    val id: String = "",
    val type: String = "",
    val title: String = "",
    val size: String = "full_width",
    val config: Map<String, Any> = emptyMap()
)

/**
 * 快捷操作按钮数据
 */
data class ActionItem(
    val icon: String = "star",
    val label: String = "",
    val action: String = ""
)
