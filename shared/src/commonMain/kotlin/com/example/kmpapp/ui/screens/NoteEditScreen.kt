package com.example.kmpapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kmpapp.data.Note
import com.example.kmpapp.data.WeatherService
import com.example.kmpapp.domain.AttachmentManager
import com.example.kmpapp.domain.NotesViewModel
import com.example.kmpapp.ui.components.AttachmentCard
import com.example.kmpapp.ui.components.AttachmentPickerBar
import com.example.kmpapp.ui.components.WeatherCard

/**
 * 笔记编辑页 —— 新建 / 编辑共用。
 *
 * 展示 Compose Multiplatform 的表单交互能力：
 * - 天气卡片（自动获取当前天气，支持切换城市）
 * - 文本输入（标题 + 正文）
 * - 颜色选择器（横滚圆点）
 * - 置顶切换 / 删除操作
 * - 富媒体附件（位置、设备信息、清单、链接）← Feature 3 新增
 *
 * 天气快照和附件会在保存时记录到笔记元数据中。
 * 整段 UI 代码在 Android/iOS 上零改动共享。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditScreen(
    noteId: Long?,
    viewModel: NotesViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val existingNote = remember(noteId) {
        if (noteId != null) {
            viewModel.notes.value.find { it.id == noteId }
        } else null
    }

    // 天气服务 —— 进入编辑页时自动获取天气
    val weatherService = remember { WeatherService() }
    val weatherData by weatherService.weather.collectAsState()

    // 附件管理器 —— Feature 3 新增
    val attachmentManager = remember { AttachmentManager() }
    val attachments by attachmentManager.attachments.collectAsState()
    val statusMessage by attachmentManager.statusMessage.collectAsState()

    LaunchedEffect(Unit) {
        weatherService.fetchWeather()
        // 加载已有附件
        attachmentManager.loadFromJson(existingNote?.attachmentsJson)
    }

    var title by remember { mutableStateOf(existingNote?.title ?: "") }
    var content by remember { mutableStateOf(existingNote?.content ?: "") }
    var colorHex by remember { mutableStateOf(existingNote?.colorHex ?: 0xFFFFF3E0) }
    var isPinned by remember { mutableStateOf(existingNote?.isPinned ?: false) }

    // 根据选中颜色动态改变背景
    val backgroundColor = Color(colorHex.toInt())

    Column(modifier = modifier.fillMaxSize().background(backgroundColor.copy(alpha = 0.3f))) {
        // 顶栏
        TopAppBar(
            title = { Text(if (existingNote != null) "编辑笔记" else "新建笔记") },
            navigationIcon = {
                IconButton(onClick = {
                    saveNote(viewModel, noteId, title, content, colorHex, isPinned,
                        weatherData?.toJson(), attachmentManager.toJson())
                    onBack()
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                }
            },
            actions = {
                // 保存按钮（显眼的对勾图标）
                IconButton(onClick = {
                    saveNote(viewModel, noteId, title, content, colorHex, isPinned,
                        weatherData?.toJson(), attachmentManager.toJson())
                    onBack()
                }) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "保存",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
                // 置顶切换
                IconButton(onClick = { isPinned = !isPinned }) {
                    Icon(
                        imageVector = if (isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                        contentDescription = "置顶",
                        tint = if (isPinned) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                // 删除（仅编辑模式）
                if (existingNote != null) {
                    IconButton(onClick = {
                        viewModel.deleteNote(existingNote.id)
                        onBack()
                    }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "删除",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )

        // 编辑区
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 天气卡片 —— 进入页面自动加载
            WeatherCard(weatherService = weatherService)

            // 标题输入
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("标题", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(fontSize = 22.sp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                ),
                shape = RoundedCornerShape(12.dp)
            )

            // 内容输入
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                placeholder = { Text("写点什么...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                modifier = Modifier.fillMaxWidth().height(250.dp),
                textStyle = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                ),
                shape = RoundedCornerShape(12.dp)
            )

            // 颜色选择器
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "笔记颜色",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NotesViewModel.colorOptions.forEach { color ->
                        val isSelected = color == colorHex
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(color.toInt()))
                                .then(
                                    if (isSelected) Modifier.border(
                                        width = 3.dp,
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = CircleShape
                                    ) else Modifier
                                )
                                .clickable { colorHex = color }
                        )
                    }
                }
            }

            // ── 附件区域（Feature 3）──────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "附件 (${attachments.size})",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // 附件选择工具栏
                AttachmentPickerBar(
                    onAddLocation = { attachmentManager.addLocation() },
                    onAddDeviceInfo = { attachmentManager.addDeviceInfo() },
                    onAddChecklist = { attachmentManager.addChecklist() },
                    onAddLink = { attachmentManager.addShareLink("https://kmp.example.com", "KMP 示例链接") },
                    onImportClipboard = { attachmentManager.importFromClipboard() }
                )

                // 状态消息
                AnimatedVisibility(visible = statusMessage != null, enter = fadeIn(), exit = fadeOut()) {
                    Text(
                        text = statusMessage ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // 附件卡片列表
                attachments.forEachIndexed { index, attachment ->
                    AttachmentCard(
                        attachment = attachment,
                        index = index,
                        onRemove = { idx -> attachmentManager.removeAttachment(idx) },
                        onToggleChecklistItem = { aIdx, iIdx -> attachmentManager.toggleChecklistItem(aIdx, iIdx) },
                        onCopyLink = { url -> attachmentManager.copyLinkToClipboard(url) }
                    )
                }
            }

            // 天气快照预览（编辑已有笔记时显示）
            if (existingNote?.weatherSnapshot != null) {
                val savedWeather = com.example.kmpapp.data.WeatherData.fromJson(existingNote.weatherSnapshot)
                if (savedWeather != null) {
                    Text(
                        text = "创建时天气: ${savedWeather.conditionIcon} ${savedWeather.temperature.toInt()}° ${savedWeather.conditionText} · ${savedWeather.cityName}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/** 保存或更新笔记（含天气快照和附件） */
private fun saveNote(
    viewModel: NotesViewModel,
    noteId: Long?,
    title: String,
    content: String,
    colorHex: Long,
    isPinned: Boolean,
    weatherSnapshot: String?,
    attachmentsJson: String?
) {
    if (title.isBlank() && content.isBlank()) return

    val finalAttachments = if (attachmentsJson == "[]") null else attachmentsJson

    if (noteId != null) {
        val existing = viewModel.notes.value.find { it.id == noteId }
        if (existing != null) {
            viewModel.updateNote(
                existing.copy(
                    title = title,
                    content = content,
                    colorHex = colorHex,
                    isPinned = isPinned,
                    weatherSnapshot = weatherSnapshot ?: existing.weatherSnapshot,
                    attachmentsJson = finalAttachments ?: existing.attachmentsJson
                )
            )
        }
    } else {
        viewModel.addNote(title, content, colorHex, weatherSnapshot, finalAttachments)
    }
}
