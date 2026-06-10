package com.example.kmpapp.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.DevicesOther
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * 附件选择工具栏 —— 横滚按钮栏，提供 4 种附件添加入口。
 *
 * 每个按钮调用 PlatformCapabilities（expect/actual）的平台原生能力，
 * 但 UI 层完全不感知平台差异。
 */
@Composable
fun AttachmentPickerBar(
    onAddLocation: () -> Unit,
    onAddDeviceInfo: () -> Unit,
    onAddChecklist: () -> Unit,
    onAddLink: () -> Unit,
    onImportClipboard: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PickerChip("\uD83D\uDCCD", "位置", onAddLocation)
        PickerChip("\uD83D\uDCF1", "设备信息", onAddDeviceInfo)
        PickerChip("✅", "清单", onAddChecklist)
        PickerChip("\uD83D\uDD17", "链接", onAddLink)
        PickerChip("\uD83D\uDCCB", "剪贴板", onImportClipboard)
    }
}

@Composable
private fun PickerChip(
    emoji: String,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(emoji)
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}
