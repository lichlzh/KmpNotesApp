package com.example.kmpapp.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 笔记数据模型 —— 在 commonMain 中定义，Android/iOS 共享同一份代码。
 *
 * 使用 kotlinx.serialization 自动处理 JSON 序列化，
 * 嵌套对象（WeatherData、NoteAttachment）直接作为属性类型，
 * 无需手工拼接/解析 JSON 字符串。
 */
@Serializable
data class Note(
    val id: Long = 0L,
    val title: String = "",
    val content: String = "",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val colorHex: Long = 0xFFFFF3E0,       // 暖黄色默认背景
    val isPinned: Boolean = false,
    @SerialName("ws")
    val weatherSnapshot: WeatherData? = null,    // 创建笔记时的天气快照
    val attachments: List<NoteAttachment> = emptyList()  // 附件列表
)
