package com.example.kmpapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kmpapp.data.NoteAttachment

/**
 * 附件卡片 —— 根据附件类型渲染不同的 UI。
 *
 * 4 种附件类型各有独特的卡片设计：
 * - Location: 渐变色背景 + 坐标 + 地址
 * - DeviceInfo: 设备信息列表 + 电量指示
 * - Checklist: 可交互的复选框列表 + 进度条
 * - ShareLink: 链接预览 + 复制按钮
 *
 * 所有 UI 代码在 commonMain，Android/iOS 完全一致。
 */
@Composable
fun AttachmentCard(
    attachment: NoteAttachment,
    index: Int,
    onRemove: (Int) -> Unit,
    onToggleChecklistItem: ((Int, Int) -> Unit)? = null,
    onCopyLink: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    when (attachment) {
        is NoteAttachment.Location -> LocationAttachmentCard(
            location = attachment, index = index, onRemove = onRemove, modifier = modifier
        )
        is NoteAttachment.DeviceInfo -> DeviceInfoCard(
            info = attachment, index = index, onRemove = onRemove, modifier = modifier
        )
        is NoteAttachment.Checklist -> ChecklistCard(
            checklist = attachment, index = index, onRemove = onRemove,
            onToggle = onToggleChecklistItem, modifier = modifier
        )
        is NoteAttachment.ShareLink -> ShareLinkCard(
            link = attachment, index = index, onRemove = onRemove,
            onCopy = onCopyLink, modifier = modifier
        )
    }
}

// ── 位置附件卡片 ────────────────────────────────────────────

@Composable
private fun LocationAttachmentCard(
    location: NoteAttachment.Location,
    index: Int,
    onRemove: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE3F2FD)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "\uD83D\uDCCD 位置",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1565C0)
                )
                IconButton(onClick = { onRemove(index) }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "删除", tint = Color(0xFF1565C0).copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = location.address.ifBlank { "未知位置" },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${location.latitude.toFormattedString(4)}, ${location.longitude.toFormattedString(4)}",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF1565C0).copy(alpha = 0.7f)
            )
        }
    }
}

// ── 设备信息卡片 ────────────────────────────────────────────

@Composable
private fun DeviceInfoCard(
    info: NoteAttachment.DeviceInfo,
    index: Int,
    onRemove: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF3E5F5)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "\uD83D\uDCF1 设备信息",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF7B1FA2)
                )
                IconButton(onClick = { onRemove(index) }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "删除", tint = Color(0xFF7B1FA2).copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            InfoRow("设备", info.device)
            InfoRow("系统", info.os)
            InfoRow("电量", "${info.batteryPercent}%")
            // 电量进度条
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { info.batteryPercent / 100f },
                modifier = Modifier.fillMaxWidth().height(4.dp),
                color = if (info.batteryPercent > 20) Color(0xFF4CAF50) else Color(0xFFF44336),
                trackColor = Color(0xFF7B1FA2).copy(alpha = 0.1f),
            )
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = Color(0xFF7B1FA2).copy(alpha = 0.7f))
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}

// ── 待办清单卡片 ────────────────────────────────────────────

@Composable
private fun ChecklistCard(
    checklist: NoteAttachment.Checklist,
    index: Int,
    onRemove: (Int) -> Unit,
    onToggle: ((Int, Int) -> Unit)?,
    modifier: Modifier = Modifier
) {
    val doneCount = checklist.items.count { it.isDone }
    val totalCount = checklist.items.size
    val progress = if (totalCount > 0) doneCount.toFloat() / totalCount else 0f

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE8F5E9)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "✅ 清单 ($doneCount/$totalCount)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
                IconButton(onClick = { onRemove(index) }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "删除", tint = Color(0xFF2E7D32).copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
                }
            }

            // 进度条
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(3.dp),
                color = Color(0xFF4CAF50),
                trackColor = Color(0xFF2E7D32).copy(alpha = 0.1f),
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 清单项
            checklist.items.forEachIndexed { itemIdx, item ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp)
                ) {
                    Checkbox(
                        checked = item.isDone,
                        onCheckedChange = { onToggle?.invoke(index, itemIdx) },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFF4CAF50),
                            checkmarkColor = Color.White
                        ),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (item.isDone) Color(0xFF2E7D32).copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// ── 分享链接卡片 ────────────────────────────────────────────

@Composable
private fun ShareLinkCard(
    link: NoteAttachment.ShareLink,
    index: Int,
    onRemove: (Int) -> Unit,
    onCopy: ((String) -> Unit)?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFBE9E7)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "\uD83D\uDD17 链接",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE65100)
                )
                Row {
                    IconButton(onClick = { onCopy?.invoke(link.url) }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "复制", tint = Color(0xFFE65100).copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = { onRemove(index) }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "删除", tint = Color(0xFFE65100).copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = link.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = link.url,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFE65100).copy(alpha = 0.7f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontSize = 12.sp
            )
        }
    }
}

/** 跨平台的 Double 格式化（保留指定小数位） */
private fun Double.toFormattedString(decimals: Int): String {
    val str = this.toString()
    val dot = str.indexOf('.')
    if (dot < 0) return "$str.${"0".repeat(decimals)}"
    val intPart = str.substring(0, dot)
    val fracPart = str.substring(dot + 1).padEnd(decimals, '0').take(decimals)
    return "$intPart.$fracPart"
}
