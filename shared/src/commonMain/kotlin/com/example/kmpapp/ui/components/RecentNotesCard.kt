package com.example.kmpapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.kmpapp.data.Note
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * 最近笔记卡片 —— 横向滚动的笔记预览列表。
 *
 * 展示最近的 [notes] 条笔记，点击跳转到编辑页。
 * 横向滚动设计让 Dashboard 保持紧凑，同时方便用户快速访问。
 */
@Composable
fun RecentNotesCard(
    notes: List<Note>,
    onNoteClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    if (notes.isEmpty()) {
        // 空状态
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(16.dp)
        ) {
            Text(
                text = "\u8FD8\u6CA1\u6709\u7B14\u8BB0\uFF0C\u70B9\u51FB\u201C\u65B0\u5EFA\u7B14\u8BB0\u201D\u5F00\u59CB\u8BB0\u5F55",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
        return
    }

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items = notes, key = { it.id }) { note ->
            NotePreviewCard(
                note = note,
                onClick = { onNoteClick(note.id) }
            )
        }
    }
}

/**
 * 笔记预览小卡片 —— 用于 Dashboard 横向滚动列表。
 *
 * 紧凑设计：顶部彩色指示条 + 标题 + 内容预览 + 时间戳。
 */
@Composable
private fun NotePreviewCard(
    note: Note,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardColor = Color(note.colorHex.toInt())

    Card(
        modifier = modifier
            .width(170.dp)
            .height(140.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.fillMaxHeight()) {
            // 顶部彩色指示条
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                    .background(cardColor)
            )

            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // 标题
                Text(
                    text = note.title.ifBlank { "\u65E0\u6807\u9898" },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // 内容预览
                Text(
                    text = note.content.ifBlank { "\u65E0\u5185\u5BB9" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                // 时间戳
                Text(
                    text = formatShortTimestamp(note.updatedAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}

/** 简短的时间戳：MM-DD HH:mm */
private fun formatShortTimestamp(epochMillis: Long): String {
    val instant = Instant.fromEpochMilliseconds(epochMillis)
    val local = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return "${local.monthNumber.toString().padStart(2, '0')}-${local.dayOfMonth.toString().padStart(2, '0')} " +
            "${local.hour.toString().padStart(2, '0')}:${local.minute.toString().padStart(2, '0')}"
}
