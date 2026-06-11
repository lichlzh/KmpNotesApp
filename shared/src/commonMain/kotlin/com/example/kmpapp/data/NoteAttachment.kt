package com.example.kmpapp.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 笔记附件密封类 —— 支持 4 种富媒体附件类型。
 *
 * 使用 kotlinx.serialization 的多态序列化（polymorphic serialization），
 * 通过 @SerialName 指定每种类型的判别值，自动处理 "type" 字段。
 *
 * 每种附件类型展示了不同的 KMP 能力：
 * - Location:  expect/actual 调用平台定位 API
 * - DeviceInfo: expect/actual 读取设备硬件信息
 * - Checklist: 纯共享代码，展示 UI 状态管理
 * - ShareLink: 纯共享代码 + expect/actual 剪贴板
 */
@Serializable
sealed class NoteAttachment {

    /** 位置附件 —— 记录经纬度和地址描述 */
    @Serializable
    @SerialName("location")
    data class Location(
        val latitude: Double = 0.0,
        val longitude: Double = 0.0,
        val address: String = ""
    ) : NoteAttachment()

    /** 设备信息快照 —— 记录创建笔记时的设备状态 */
    @Serializable
    @SerialName("device")
    data class DeviceInfo(
        val device: String = "",
        val os: String = "",
        val batteryPercent: Int = 0
    ) : NoteAttachment()

    /** 待办清单 —— 支持交互式勾选 */
    @Serializable
    @SerialName("checklist")
    data class Checklist(
        val items: List<ChecklistItem> = emptyList()
    ) : NoteAttachment()

    /** 分享链接 —— URL + 标题 */
    @Serializable
    @SerialName("link")
    data class ShareLink(
        val url: String = "",
        val title: String = ""
    ) : NoteAttachment()
}

/** 清单项 */
@Serializable
data class ChecklistItem(val text: String, val isDone: Boolean = false)
