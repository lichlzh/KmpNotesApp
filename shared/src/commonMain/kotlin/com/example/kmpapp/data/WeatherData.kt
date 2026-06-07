package com.example.kmpapp.data

/**
 * 天气数据模型 —— 在 commonMain 定义，双平台共享。
 *
 * 数据来源：Open-Meteo API（免费，无需 API Key）
 * 包含温度、天气状况、湿度、风速等核心信息。
 */
data class WeatherData(
    val temperature: Double = 0.0,
    val feelsLike: Double = 0.0,
    val humidity: Int = 0,
    val windSpeed: Double = 0.0,
    val weatherCode: Int = 0,
    val isDay: Boolean = true,
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

    /** 序列化为简短的 JSON 片段，嵌入笔记元数据 */
    fun toJson(): String = buildString {
        append("{")
        append("\"t\":$temperature,")
        append("\"fl\":$feelsLike,")
        append("\"h\":$humidity,")
        append("\"ws\":$windSpeed,")
        append("\"wc\":$weatherCode,")
        append("\"d\":$isDay,")
        append("\"c\":\"${cityName.escapeJson()}\"")
        append("}")
    }

    companion object {
        fun fromJson(json: String): WeatherData? {
            if (json.isBlank()) return null
            return try {
                val map = parseJsonObject(json)
                WeatherData(
                    temperature = (map["t"] as? Number)?.toDouble() ?: 0.0,
                    feelsLike = (map["fl"] as? Number)?.toDouble() ?: 0.0,
                    humidity = (map["h"] as? Number)?.toInt() ?: 0,
                    windSpeed = (map["ws"] as? Number)?.toDouble() ?: 0.0,
                    weatherCode = (map["wc"] as? Number)?.toInt() ?: 0,
                    isDay = map["d"] as? Boolean ?: true,
                    cityName = map["c"] as? String ?: "北京"
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}
