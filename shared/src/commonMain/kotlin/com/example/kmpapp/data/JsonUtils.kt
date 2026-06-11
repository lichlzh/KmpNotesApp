package com.example.kmpapp.data

import kotlinx.serialization.json.Json

/**
 * 全局 JSON 配置 —— 项目统一的序列化入口。
 *
 * 替代了之前分散在各文件中的手工 JSON 工具函数
 * （escapeJson、unescapeJson、parseJsonObject、readJsonString）。
 *
 * ignoreUnknownKeys = true 确保旧版数据向前兼容。
 */
val AppJson = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
    coerceInputValues = true
    classDiscriminator = "type"
}
