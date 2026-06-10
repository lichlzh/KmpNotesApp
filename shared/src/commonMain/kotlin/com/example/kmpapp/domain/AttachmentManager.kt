package com.example.kmpapp.domain

import com.example.kmpapp.data.ChecklistItem
import com.example.kmpapp.data.NoteAttachment
import com.example.kmpapp.data.PlatformCapabilities
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 附件管理器 —— 管理笔记的富媒体附件状态。
 *
 * 在 commonMain 中实现，通过 PlatformCapabilities（expect/actual）
 * 调用平台原生能力，上层逻辑完全不感知平台差异。
 *
 * 支持的附件类型：
 * - Location: 调用平台定位 API
 * - DeviceInfo: 读取设备硬件信息
 * - Checklist: 交互式待办清单
 * - ShareLink: URL 链接 + 剪贴板集成
 */
class AttachmentManager {

    private val capabilities = PlatformCapabilities()

    private val _attachments = MutableStateFlow<List<NoteAttachment>>(emptyList())
    val attachments: StateFlow<List<NoteAttachment>> = _attachments.asStateFlow()

    /** 操作状态提示（如"已复制到剪贴板"） */
    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    /** 从 JSON 加载已有附件 */
    fun loadFromJson(json: String?) {
        _attachments.value = if (json != null) NoteAttachment.listFromJson(json) else emptyList()
    }

    /** 序列化为 JSON */
    fun toJson(): String = NoteAttachment.listToJson(_attachments.value)

    // ── 添加附件 ──────────────────────────────────────────

    /** 添加设备信息快照 */
    fun addDeviceInfo() {
        val (device, os, battery) = capabilities.getDeviceInfo()
        capabilities.triggerHaptic()
        addAttachment(NoteAttachment.DeviceInfo(device, os, battery))
    }

    /** 获取并添加当前位置 */
    fun addLocation() {
        _statusMessage.value = "正在获取位置..."
        capabilities.getLocation { lat, lng, address ->
            addAttachment(NoteAttachment.Location(lat, lng, address))
            _statusMessage.value = "位置已添加"
        }
    }

    /** 添加待办清单 */
    fun addChecklist(initialItems: List<String> = listOf("待办事项 1", "待办事项 2", "待办事项 3")) {
        addAttachment(NoteAttachment.Checklist(initialItems.map { ChecklistItem(it) }))
    }

    /** 添加分享链接 */
    fun addShareLink(url: String, title: String) {
        if (url.isBlank()) return
        addAttachment(NoteAttachment.ShareLink(url, title.ifBlank { url }))
    }

    /** 从剪贴板导入链接 */
    fun importFromClipboard() {
        val text = capabilities.getFromClipboard()
        if (text != null && (text.startsWith("http://") || text.startsWith("https://"))) {
            addShareLink(text, text)
            _statusMessage.value = "已从剪贴板导入链接"
        } else {
            _statusMessage.value = "剪贴板中没有链接"
        }
    }

    // ── 管理附件 ──────────────────────────────────────────

    fun removeAttachment(index: Int) {
        val list = _attachments.value.toMutableList()
        if (index in list.indices) {
            list.removeAt(index)
            _attachments.value = list
        }
    }

    /** 切换清单项的完成状态 */
    fun toggleChecklistItem(attachmentIndex: Int, itemIndex: Int) {
        val list = _attachments.value.toMutableList()
        val attachment = list.getOrNull(attachmentIndex)
        if (attachment is NoteAttachment.Checklist) {
            val items = attachment.items.toMutableList()
            if (itemIndex in items.indices) {
                items[itemIndex] = items[itemIndex].copy(isDone = !items[itemIndex].isDone)
                list[attachmentIndex] = attachment.copy(items = items)
                _attachments.value = list
                capabilities.triggerHaptic()
            }
        }
    }

    /** 向清单项添加条目 */
    fun addChecklistItem(attachmentIndex: Int, text: String) {
        val list = _attachments.value.toMutableList()
        val attachment = list.getOrNull(attachmentIndex)
        if (attachment is NoteAttachment.Checklist) {
            val items = attachment.items + ChecklistItem(text)
            list[attachmentIndex] = attachment.copy(items = items)
            _attachments.value = list
        }
    }

    /** 复制分享链接到剪贴板 */
    fun copyLinkToClipboard(url: String) {
        capabilities.copyToClipboard(url)
        _statusMessage.value = "已复制到剪贴板"
    }

    fun clearStatus() {
        _statusMessage.value = null
    }

    private fun addAttachment(attachment: NoteAttachment) {
        _attachments.value = _attachments.value + attachment
    }
}
