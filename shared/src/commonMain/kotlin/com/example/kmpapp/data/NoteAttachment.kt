package com.example.kmpapp.data

/**
 * 笔记附件密封类 —— 支持 4 种富媒体附件类型。
 *
 * 每种附件类型展示了不同的 KMP 能力：
 * - Location:  expect/actual 调用平台定位 API
 * - DeviceInfo: expect/actual 读取设备硬件信息
 * - Checklist: 纯共享代码，展示 UI 状态管理
 * - ShareLink: 纯共享代码 + expect/actual 剪贴板
 *
 * 附件序列化为 JSON 数组，存储在 Note.attachmentsJson 中。
 */
sealed class NoteAttachment {

    abstract fun toJson(): String

    /** 位置附件 —— 记录经纬度和地址描述 */
    data class Location(
        val latitude: Double,
        val longitude: Double,
        val address: String
    ) : NoteAttachment() {
        override fun toJson(): String = buildString {
            append("{\"type\":\"location\",")
            append("\"lat\":$latitude,")
            append("\"lng\":$longitude,")
            append("\"addr\":\"${address.escapeJson()}\"}")
        }
    }

    /** 设备信息快照 —— 记录创建笔记时的设备状态 */
    data class DeviceInfo(
        val device: String,
        val os: String,
        val batteryPercent: Int
    ) : NoteAttachment() {
        override fun toJson(): String = buildString {
            append("{\"type\":\"device\",")
            append("\"dev\":\"${device.escapeJson()}\",")
            append("\"os\":\"${os.escapeJson()}\",")
            append("\"bat\":$batteryPercent}")
        }
    }

    /** 待办清单 —— 支持交互式勾选 */
    data class Checklist(
        val items: List<ChecklistItem>
    ) : NoteAttachment() {
        override fun toJson(): String = buildString {
            append("{\"type\":\"checklist\",\"items\":[")
            items.forEachIndexed { i, item ->
                if (i > 0) append(",")
                append("{\"text\":\"${item.text.escapeJson()}\",\"done\":${item.isDone}}")
            }
            append("]}")
        }
    }

    /** 分享链接 —— URL + 标题 */
    data class ShareLink(
        val url: String,
        val title: String
    ) : NoteAttachment() {
        override fun toJson(): String = buildString {
            append("{\"type\":\"link\",")
            append("\"url\":\"${url.escapeJson()}\",")
            append("\"title\":\"${title.escapeJson()}\"}")
        }
    }

    companion object {
        /** 从 JSON 数组字符串解析附件列表 */
        fun listFromJson(json: String): List<NoteAttachment> {
            if (json.isBlank() || json == "[]") return emptyList()
            val items = mutableListOf<NoteAttachment>()
            // 提取每个 { } 对象
            var depth = 0
            var start = -1
            var inStr = false
            var i = 0
            while (i < json.length) {
                val c = json[i]
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
                                val obj = json.substring(start, i + 1)
                                parseOne(obj)?.let { items.add(it) }
                            }
                        }
                    }
                }
                i++
            }
            return items
        }

        /** 将附件列表序列化为 JSON 数组 */
        fun listToJson(attachments: List<NoteAttachment>): String =
            attachments.joinToString(prefix = "[", postfix = "]", separator = ",") { it.toJson() }

        private fun parseOne(json: String): NoteAttachment? {
            return try {
                val map = parseJsonObject(json)
                when (map["type"]) {
                    "location" -> Location(
                        latitude = (map["lat"] as? Number)?.toDouble() ?: 0.0,
                        longitude = (map["lng"] as? Number)?.toDouble() ?: 0.0,
                        address = map["addr"] as? String ?: ""
                    )
                    "device" -> DeviceInfo(
                        device = map["dev"] as? String ?: "",
                        os = map["os"] as? String ?: "",
                        batteryPercent = (map["bat"] as? Number)?.toInt() ?: 0
                    )
                    "checklist" -> {
                        val items = parseChecklistItems(json)
                        Checklist(items)
                    }
                    "link" -> ShareLink(
                        url = map["url"] as? String ?: "",
                        title = map["title"] as? String ?: ""
                    )
                    else -> null
                }
            } catch (e: Exception) { null }
        }

        private fun parseChecklistItems(json: String): List<ChecklistItem> {
            val items = mutableListOf<ChecklistItem>()
            val itemsIdx = json.indexOf("\"items\"")
            if (itemsIdx < 0) return items

            val arrayStart = json.indexOf('[', itemsIdx)
            if (arrayStart < 0) return items

            var depth = 0
            var arrayEnd = arrayStart
            for (i in arrayStart until json.length) {
                when (json[i]) {
                    '[' -> depth++
                    ']' -> { depth--; if (depth == 0) { arrayEnd = i; break } }
                }
            }

            val body = json.substring(arrayStart + 1, arrayEnd)
            var objDepth = 0
            var objStart = -1
            var inStr = false
            var j = 0
            while (j < body.length) {
                val c = body[j]
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
                                val itemJson = body.substring(objStart, j + 1)
                                val map = parseJsonObject(itemJson)
                                items.add(ChecklistItem(
                                    text = map["text"] as? String ?: "",
                                    isDone = map["done"] as? Boolean ?: false
                                ))
                            }
                        }
                    }
                }
                j++
            }
            return items
        }
    }
}

/** 清单项 */
data class ChecklistItem(val text: String, val isDone: Boolean = false)
