package com.example.kmpapp.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 每日一言卡片 —— 渐变色背景 + 名言/诗句。
 *
 * 名言列表硬编码在代码中，点击刷新按钮随机切换。
 * 未来可以扩展为从远程 API 加载，展示动态化能力。
 *
 * 使用 AnimatedContent 实现名言切换的淡入淡出动画。
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun QuoteCard(
    modifier: Modifier = Modifier
) {
    var quoteIndex by remember { mutableIntStateOf(0) }
    val quotes = remember {
        listOf(
            "\u201C\u5B66\u800C\u4E0D\u601D\u5219\u7F54\uFF0C\u601D\u800C\u4E0D\u5B66\u5219\u6B86\u3002\u201D" to "\u2014\u2014 \u5B54\u5B50",
            "\u201C\u751F\u6D3B\u4E0D\u662F\u6211\u4EEC\u6240\u7ECF\u5386\u7684\uFF0C\u800C\u662F\u6211\u4EEC\u6240\u8BB0\u4F4F\u7684\u3002\u201D" to "\u2014\u2014 \u52A0\u897F\u4E9A\u00B7\u9A6C\u5C14\u514B\u65AF",
            "\u201C\u6BCF\u4E00\u6B21\u8BB0\u5F55\uFF0C\u90FD\u662F\u4E0E\u672A\u6765\u7684\u81EA\u5DF1\u5BF9\u8BDD\u3002\u201D" to "\u2014\u2014 KMP Notes",
            "\u201C\u7B80\u5355\u662F\u7EC8\u6781\u7684\u590D\u6742\u3002\u201D" to "\u2014\u2014 \u8FBE\u00B7\u82AC\u5947",
            "\u201C\u5343\u91CC\u4E4B\u884C\uFF0C\u59CB\u4E8E\u8DB3\u4E0B\u3002\u201D" to "\u2014\u2014 \u8001\u5B50",
            "\u201C\u4E16\u754C\u4E0A\u6700\u5FEB\u7684\u4E1C\u897F\u662F\u65F6\u95F4\uFF0C\u6700\u6162\u7684\u4E5F\u662F\u65F6\u95F4\u3002\u201D" to "\u2014\u2014 \u7231\u56E0\u65AF\u5766",
            "\u201C\u8BB0\u5F55\u662F\u62B5\u5FA1\u9057\u5FD8\u7684\u6B66\u5668\u3002\u201D" to "\u2014\u2014 KMP Notes",
            "\u201C\u5DF2\u7ECF\u8D70\u8FC7\u7684\u8DEF\u4E0D\u80FD\u518D\u8D70\uFF0C\u4F46\u53EF\u4EE5\u518D\u56DE\u5934\u770B\u770B\u3002\u201D" to "\u2014\u2014 KMP Notes"
        )
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        val gradientColors = listOf(
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.8f),
            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(colors = gradientColors),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "\u2728 \u6BCF\u65E5\u4E00\u8A00",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                )
                IconButton(
                    onClick = { quoteIndex = (quoteIndex + 1) % quotes.size },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "\u6362\u4E00\u6362",
                        tint = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            AnimatedContent(
                targetState = quoteIndex,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "quote_transition"
            ) { index ->
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = quotes[index].first,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp,
                        lineHeight = 24.sp
                    )
                    Text(
                        text = quotes[index].second,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}
