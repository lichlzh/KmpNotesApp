package com.example.kmpapp.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 天气服务 —— 在 commonMain 中实现，使用 expect/actual 的 httpGet 获取数据。
 *
 * 数据源：Open-Meteo API（https://open-meteo.com）
 * - 完全免费，无需 API Key
 * - 支持全球城市
 * - 返回当前天气 + 7 天预报
 *
 * 这里演示了 KMP 的核心价值：
 * 网络层（HTTP）由平台实现，业务逻辑（解析、状态管理）完全共享。
 */
class WeatherService {

    private val _weather = MutableStateFlow<WeatherData?>(null)
    val weather: StateFlow<WeatherData?> = _weather.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /**
     * 获取指定城市的当前天气。
     * 使用北京（39.9°N, 116.4°E）作为默认城市。
     */
    fun fetchWeather(
        latitude: Double = 39.9042,
        longitude: Double = 116.4074,
        cityName: String = "北京"
    ) {
        _isLoading.value = true
        _error.value = null

        val url = buildString {
            append("https://api.open-meteo.com/v1/forecast")
            append("?latitude=$latitude")
            append("&longitude=$longitude")
            append("&current=temperature_2m,relative_humidity_2m,apparent_temperature,weather_code,wind_speed_10m,is_day")
            append("&timezone=auto")
        }

        httpGet(url) { result ->
            _isLoading.value = false
            result.fold(
                onSuccess = { body ->
                    try {
                        val data = parseOpenMeteoResponse(body, cityName)
                        _weather.value = data
                    } catch (e: Exception) {
                        _error.value = "解析天气数据失败: ${e.message}"
                    }
                },
                onFailure = { e ->
                    _error.value = "网络请求失败: ${e.message}"
                }
            )
        }
    }

    /** 预设城市列表，方便快速切换 */
    val presetCities = listOf(
        CityInfo("北京", 39.9042, 116.4074),
        CityInfo("上海", 31.2304, 121.4737),
        CityInfo("东京", 35.6762, 139.6503),
        CityInfo("纽约", 40.7128, -74.0060),
        CityInfo("伦敦", 51.5074, -0.1278),
    )

    data class CityInfo(val name: String, val latitude: Double, val longitude: Double)

    companion object {
        /**
         * 解析 Open-Meteo API 的 JSON 响应。
         * 手工解析以避免引入额外依赖，
         * 生产项目推荐使用 kotlinx.serialization。
         */
        private fun parseOpenMeteoResponse(json: String, cityName: String): WeatherData {
            // 提取 "current" 对象
            val currentStart = json.indexOf("\"current\"")
            if (currentStart < 0) throw Exception("Missing 'current' field")

            // 找到 current 对象的 { }
            val braceStart = json.indexOf('{', currentStart)
            var depth = 0
            var braceEnd = braceStart
            for (i in braceStart until json.length) {
                when (json[i]) {
                    '{' -> depth++
                    '}' -> {
                        depth--
                        if (depth == 0) { braceEnd = i; break }
                    }
                }
            }

            val currentJson = json.substring(braceStart, braceEnd + 1)
            val fields = parseJsonObject(currentJson)

            return WeatherData(
                temperature = (fields["temperature_2m"] as? Number)?.toDouble() ?: 0.0,
                feelsLike = (fields["apparent_temperature"] as? Number)?.toDouble() ?: 0.0,
                humidity = (fields["relative_humidity_2m"] as? Number)?.toInt() ?: 0,
                windSpeed = (fields["wind_speed_10m"] as? Number)?.toDouble() ?: 0.0,
                weatherCode = (fields["weather_code"] as? Number)?.toInt() ?: 0,
                isDay = fields["is_day"] as? Int == 1 || fields["is_day"] as? Boolean == true,
                cityName = cityName
            )
        }
    }
}
