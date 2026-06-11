package com.example.kmpapp.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 天气数据模型 —— 在 commonMain 定义，双平台共享。
 *
 * 数据来源：Open-Meteo API（https://open-meteo.com）
 * 包含温度、天气状况、湿度、风速等核心信息。
 *
 * 使用 @SerialName 将简短的 JSON 字段映射到可读的 Kotlin 属性名。
 */
@Serializable
data class WeatherData(
    @SerialName("t")
    val temperature: Double = 0.0,
    @SerialName("fl")
    val feelsLike: Double = 0.0,
    @SerialName("h")
    val humidity: Int = 0,
    @SerialName("ws")
    val windSpeed: Double = 0.0,
    @SerialName("wc")
    val weatherCode: Int = 0,
    @SerialName("d")
    val isDay: Boolean = true,
    @SerialName("c")
    val cityName: String = "北京"
) {
    /** 将天气代码转为人类可读的描述 */
    val conditionText: String
        get() = when (weatherCode) {
            0 -> "晴朗"
            1, 2 -> "大部晴朗"
            3 -> "多云"
            45, 48 -> "雾"
            51, 53, 55 -> "毛毛雨"
            61, 63 -> "小雨"
            65 -> "大雨"
            71, 73 -> "小雪"
            75, 77 -> "大雪"
            80, 81 -> "阵雨"
            82 -> "暴雨"
            85, 86 -> "阵雪"
            95 -> "雷暴"
            96, 99 -> "雷暴+冰雹"
            else -> "未知"
        }

    /** 天气图标（使用 Unicode emoji，跨平台一致） */
    val conditionIcon: String
        get() = when {
            weatherCode == 0 && isDay -> "☀️"
            weatherCode == 0 && !isDay -> "🌙"
            weatherCode in 1..2 -> if (isDay) "🌤" else "☁️"
            weatherCode == 3 -> "☁️"
            weatherCode in 45..48 -> "🌫"
            weatherCode in 51..55 -> "🌦"
            weatherCode in 61..65 -> "🌧"
            weatherCode in 71..77 -> "🌨"
            weatherCode in 80..82 -> "⛈"
            weatherCode >= 95 -> "⛈"
            else -> "🌡"
        }
}

/**
 * Open-Meteo API 响应结构 —— 仅用于解析 API 返回值，
 * 解析后转为 [WeatherData] 存储。
 */
@Serializable
data class OpenMeteoResponse(
    val current: CurrentWeather
) {
    @Serializable
    data class CurrentWeather(
        val temperature_2m: Double = 0.0,
        val apparent_temperature: Double = 0.0,
        val relative_humidity_2m: Int = 0,
        val wind_speed_10m: Double = 0.0,
        val weather_code: Int = 0,
        val is_day: Int = 1
    )
}
