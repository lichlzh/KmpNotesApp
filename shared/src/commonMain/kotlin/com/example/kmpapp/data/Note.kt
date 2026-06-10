package com.example.kmpapp.data

import kotlinx.datetime.Clock

/**
 * 笔记数据模型 —— 在 commonMain 中定义，Android/iOS 共享同一份代码。
 *
 * 使用 kotlinx.datetime 的 Instant 做时间戳，避免平台相关的时间 API。
 * isPinned 字段演示了跨平台一致的布尔标记处理。
 */
data class Note(
    val id: Long = Clock.System.now().toEpochMilliseconds(),
    val title: String = "",
    val content: String = "",
    val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
    val updatedAt: Long = Clock.System.now().toEpochMilliseconds(),
    val colorHex: Long = 0xFFFFF3E0,       // 暖黄色默认背景
    val isPinned: Boolean = false,
    val weatherSnapshot: String? = null,    // 创建笔记时的天气快照（JSON）
    val attachmentsJson: String? = null     // 附件列表（JSON 数组）
) {
    /**
     * 将 Note 序列化为简易 JSON 字符串。
     * 生产项目建议使用 kotlinx.serialization，这里为了减少依赖用手工拼接。
     */
    fun toJson(): String = buildString {
        append("{")
        append("\"id\":$id,")
        append("\"title\":\"${title.escapeJson()}\",")
        append("\"content\":\"${content.escapeJson()}\",")
        append("\"createdAt\":$createdAt,")
        append("\"updatedAt\":$updatedAt,")
        append("\"colorHex\":$colorHex,")
        append("\"isPinned\":$isPinned")
        if (weatherSnapshot != null) {
            append(",\"ws\":\"${weatherSnapshot.escapeJson()}\"")
        }
        if (attachmentsJson != null) {
            append(",\"att\":\"${attachmentsJson.escapeJson()}\"")
        }
        append("}")
    }

    companion object {
        fun fromJson(json: String): Note {
            val map = parseJsonObject(json)
            return Note(
                id = (map["id"] as? Number)?.toLong() ?: 0L,
                title = map["title"] as? String ?: "",
                content = map["content"] as? String ?: "",
                createdAt = (map["createdAt"] as? Number)?.toLong() ?: 0L,
                updatedAt = (map["updatedAt"] as? Number)?.toLong() ?: 0L,
                colorHex = (map["colorHex"] as? Number)?.toLong() ?: 0xFFFFF3E0,
                isPinned = map["isPinned"] as? Boolean ?: false,
                weatherSnapshot = map["ws"] as? String,
                attachmentsJson = map["att"] as? String
            )
        }

        fun listToJson(notes: List<Note>): String =
            notes.joinToString(prefix = "[", postfix = "]", separator = ",") { it.toJson() }

        fun listFromJson(json: String): List<Note> {
            if (json.isBlank() || json == "[]") return emptyList()
            // 按顶层 {} 拆分各对象，正确跳过字符串内的花括号
            val items = mutableListOf<String>()
            var depth = 0
            var start = -1
            var inString = false
            var i = 0
            while (i < json.length) {
                val c = json[i]
                if (inString) {
                    if (c == '\\') { i += 2; continue } // 跳过转义字符
                    if (c == '"') inString = false
                } else {
                    when (c) {
                        '"' -> inString = true
                        '{' -> { if (depth == 0) start = i; depth++ }
                        '}' -> {
                            depth--
                            if (depth == 0 && start >= 0) {
                                items.add(json.substring(start, i + 1))
                            }
                        }
                    }
                }
                i++
            }
            return items.map { fromJson(it) }
        }
    }
}

// ── 简易 JSON 工具 ──────────────────────────────────────────────────────────

internal fun String.escapeJson(): String = this
    .replace("\\", "\\\\")
    .replace("\"", "\\\"")
    .replace("\n", "\\n")
    .replace("\r", "\\r")
    .replace("\t", "\\t")

internal fun String.unescapeJson(): String {
    // 单次遍历解码，避免顺序 replace 导致的 \\n 误解析问题
    val sb = StringBuilder()
    var i = 0
    while (i < length) {
        if (this[i] == '\\' && i + 1 < length) {
            when (this[i + 1]) {
                '"'  -> { sb.append('"');  i += 2 }
                '\\' -> { sb.append('\\'); i += 2 }
                'n'  -> { sb.append('\n'); i += 2 }
                'r'  -> { sb.append('\r'); i += 2 }
                't'  -> { sb.append('\t'); i += 2 }
                else -> { sb.append(this[i]); i++ }
            }
        } else {
            sb.append(this[i]); i++
        }
    }
    return sb.toString()
}

internal fun parseJsonObject(json: String): Map<String, Any?> {
    val result = mutableMapOf<String, Any?>()
    // 去掉外层 {}
    val body = json.trim().removePrefix("{").removeSuffix("}").trim()
    if (body.isEmpty()) return result

    var i = 0
    while (i < body.length) {
        // 跳过空白
        while (i < body.length && body[i].isWhitespace()) i++
        if (i >= body.length) break

        // 读 key
        val key = readJsonString(body, i)
        i = key.second
        // 跳过 :
        while (i < body.length && (body[i].isWhitespace() || body[i] == ':')) i++

        // 读 value（带越界保护）
        if (i >= body.length) break
        when {
            body[i] == '"' -> {
                val v = readJsonString(body, i)
                result[key.first] = v.first.unescapeJson()
                i = v.second
            }
            body.substring(i).startsWith("true") -> {
                result[key.first] = true; i += 4
            }
            body.substring(i).startsWith("false") -> {
                result[key.first] = false; i += 5
            }
            body.substring(i).startsWith("null") -> {
                result[key.first] = null; i += 4
            }
            else -> {
                // 数字
                val numStr = buildString {
                    while (i < body.length && (body[i].isDigit() || body[i] == '.' || body[i] == '-')) {
                        append(body[i]); i++
                    }
                }
                result[key.first] = if (numStr.contains('.')) numStr.toDouble() else numStr.toLong()
            }
        }

        // 跳过逗号
        while (i < body.length && (body[i].isWhitespace() || body[i] == ',')) i++
    }
    return result
}

private fun readJsonString(s: String, start: Int): Pair<String, Int> {
    require(s[start] == '"') { "Expected '\"' at $start" }
    var i = start + 1
    val sb = StringBuilder()
    while (i < s.length) {
        if (s[i] == '\\') {
            sb.append(s[i]); sb.append(s[i + 1]); i += 2
        } else if (s[i] == '"') {
            return sb.toString() to (i + 1)
        } else {
            sb.append(s[i]); i++
        }
    }
    return sb.toString() to i
}
