package com.example.kmpapp.ui.screens

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kmpapp.data.ThemeConfig
import com.example.kmpapp.data.ThemeEngine
import com.example.kmpapp.data.toColor

/**
 * 主题设置页 —— 展示所有内置主题并提供实时预览和切换。
 *
 * 展示 KMP 动态化能力：
 * - 点击主题卡片 → ThemeEngine 更新 StateFlow → Compose recompose → 全局变色
 * - 支持导出当前主题为 JSON 字符串
 * - 支持重置为默认主题
 *
 * 整个页面在 commonMain，Android/iOS 共享。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentTheme by ThemeEngine.currentTheme.collectAsState()
    val builtinThemes = ThemeEngine.getBuiltinThemes()

    Column(modifier = modifier.fillMaxSize()) {
        // 顶栏
        TopAppBar(
            title = {
                Column {
                    Text("主题设置")
                    Text(
                        text = "当前: ${currentTheme.name}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                }
            },
            actions = {
                // 导出当前主题
                IconButton(onClick = {
                    val json = ThemeEngine.exportCurrentTheme()
                    // JSON 已生成，实际项目中可通过平台剪贴板分享
                    // 这里打印到日志供调试
                    println("Theme JSON export:\n$json")
                }) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "导出主题")
                }
                // 重置为默认
                IconButton(onClick = { ThemeEngine.resetToDefault() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "重置默认")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        // 主题列表
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 标题
            Text(
                text = "选择主题",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "点击卡片即可切换主题，整个 App 会立即变色。主题配置保存在本地，下次启动自动恢复。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // 内置主题网格（2 列）
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                builtinThemes.chunked(2).forEach { rowThemes ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowThemes.forEach { theme ->
                            ThemePreviewCard(
                                theme = theme,
                                isSelected = theme.id == currentTheme.id,
                                onClick = { ThemeEngine.applyTheme(theme) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        // 填充空位保持对齐
                        if (rowThemes.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 当前主题详情
            Text(
                text = "当前主题详情",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            CurrentThemeDetail(theme = currentTheme)

            // JSON 预览
            Text(
                text = "主题 JSON（可复制到其他设备导入）",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Text(
                    text = ThemeEngine.exportCurrentTheme(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp),
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * 主题预览卡片 —— 展示主题的配色方案预览。
 *
 * 包含：渐变色背景、色块预览、主题名称、选中状态指示。
 */
@Composable
private fun ThemePreviewCard(
    theme: ThemeConfig,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primary = theme.colors.primary.toColor()
    val primaryContainer = theme.colors.primaryContainer.toColor()
    val surface = theme.colors.surface.toColor()
    val onSurface = theme.colors.onSurface.toColor()
    val secondary = theme.colors.secondary.toColor()

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) primary else Color.Transparent,
        label = "borderColor"
    )

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .then(
                if (isSelected) Modifier.border(2.dp, borderColor, RoundedCornerShape(16.dp))
                else Modifier
            ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp),
        colors = CardDefaults.cardColors(containerColor = surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // 渐变色预览条
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(primary, primaryContainer, secondary)
                        )
                    )
            ) {
                if (isSelected) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "已选中",
                        tint = theme.colors.onPrimary.toColor(),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 色块预览
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ColorDot(primary)
                ColorDot(primaryContainer)
                ColorDot(secondary)
                ColorDot(theme.colors.tertiary.toColor())
                ColorDot(theme.colors.background.toColor())
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 主题名称
            Text(
                text = theme.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = onSurface
            )

            // 模式标签
            Text(
                text = if (theme.isDark) "深色模式" else "浅色模式",
                style = MaterialTheme.typography.labelSmall,
                color = onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun ColorDot(color: Color, size: Int = 20) {
    Surface(
        shape = CircleShape,
        color = color,
        modifier = Modifier.size(size.dp)
    ) {}
}

/**
 * 当前主题的详细配色展示
 */
@Composable
private fun CurrentThemeDetail(theme: ThemeConfig) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 主题名称
                Text(
                    text = "${if (theme.isDark) "\uD83C\uDF19" else "☀️"} ${theme.name}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 主色调展示
            Text(
                text = "主色调",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ColorChip("Primary", theme.colors.primary.toColor())
                ColorChip("Container", theme.colors.primaryContainer.toColor())
                ColorChip("Secondary", theme.colors.secondary.toColor())
                ColorChip("Tertiary", theme.colors.tertiary.toColor())
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 背景/表面色展示
            Text(
                text = "背景与表面",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ColorChip("背景", theme.colors.background.toColor())
                ColorChip("表面", theme.colors.surface.toColor())
                ColorChip("变体", theme.colors.surfaceVariant.toColor())
            }
        }
    }
}

@Composable
private fun ColorChip(label: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(60.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(color)
                .border(1.dp, Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 9.sp,
            maxLines = 1
        )
    }
}
