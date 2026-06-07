package com.example.kmpapp.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kmpapp.data.WeatherData
import com.example.kmpapp.data.WeatherService

/**
 * 天气卡片组件 —— 嵌入笔记编辑页顶部。
 *
 * 展示当前城市的实时天气信息，支持切换城市。
 * 使用 Compose Multiplatform 共享 UI，Android/iOS 零改动。
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun WeatherCard(
    weatherService: WeatherService,
    modifier: Modifier = Modifier
) {
    val weather by weatherService.weather.collectAsState()
    val isLoading by weatherService.isLoading.collectAsState()
    val error by weatherService.error.collectAsState()
    var showCityMenu by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        // 渐变背景
        val gradientColors = when {
            weather?.isDay == false -> listOf(Color(0xFF1A237E), Color(0xFF283593))
            weather?.weatherCode in listOf(61, 63, 65, 80, 81, 82) -> listOf(Color(0xFF546E7A), Color(0xFF78909C))
            weather?.weatherCode in listOf(71, 73, 75, 77, 85, 86) -> listOf(Color(0xFFB0BEC5), Color(0xFFCFD8DC))
            else -> listOf(Color(0xFF42A5F5), Color(0xFF64B5F6))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(colors = gradientColors),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(20.dp)
        ) {
            // 城市行 + 刷新按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 城市选择
                Column {
                    Text(
                        text = weather?.cityName ?: "选择城市",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "当前天气",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }

                Row {
                    // 城市切换按钮
                    Text(
                        text = "切换城市 ▾",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showCityMenu = !showCityMenu }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    )

                    // 刷新按钮
                    IconButton(
                        onClick = { weatherService.fetchWeather() },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "刷新",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // 城市下拉菜单
                DropdownMenu(
                    expanded = showCityMenu,
                    onDismissRequest = { showCityMenu = false }
                ) {
                    weatherService.presetCities.forEach { city ->
                        DropdownMenuItem(
                            text = { Text(city.name) },
                            onClick = {
                                showCityMenu = false
                                weatherService.fetchWeather(city.latitude, city.longitude, city.name)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 天气主信息
            AnimatedContent(
                targetState = weather,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "weather_transition"
            ) { currentWeather ->
                if (currentWeather != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 温度
                        Text(
                            text = "${currentWeather.temperature.toInt()}°",
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Light,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            // 天气图标 + 描述
                            Text(
                                text = "${currentWeather.conditionIcon} ${currentWeather.conditionText}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White
                            )
                            Text(
                                text = "体感 ${currentWeather.feelsLike.toInt()}°",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                } else if (isLoading) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("获取天气中...", color = Color.White)
                    }
                } else if (error != null) {
                    Text(
                        text = error ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                } else {
                    Text(
                        text = "点击上方按钮获取天气",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            // 详细数据行
            if (weather != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    WeatherDetail("💧", "湿度", "${weather!!.humidity}%")
                    WeatherDetail("💨", "风速", "${weather!!.windSpeed.toInt()} km/h")
                    WeatherDetail("🌡", "体感", "${weather!!.feelsLike.toInt()}°")
                }
            }
        }
    }
}

@Composable
private fun WeatherDetail(icon: String, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(icon, fontSize = 16.sp)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.6f)
        )
    }
}
