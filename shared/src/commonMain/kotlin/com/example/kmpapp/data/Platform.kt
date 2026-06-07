package com.example.kmpapp.data

/**
 * expect 声明 —— KMP 跨平台桥接的关键机制。
 *
 * 这里声明「存在一个叫 PlatformKeyValueStorage 的类」，
 * 具体实现由 androidMain / iosMain 各自提供 actual。
 *
 * 同样声明了获取当前平台名称的函数，用于 UI 层展示。
 */

expect class PlatformKeyValueStorage() {
    fun putString(key: String, value: String)
    fun getString(key: String): String?
    fun remove(key: String)
}

/** 返回当前运行平台名称，例如 "Android" / "iOS" */
expect fun getPlatformName(): String

/**
 * 跨平台 HTTP GET 请求 —— expect/actual 机制的又一典型应用。
 *
 * commonMain 声明接口，androidMain 用 java.net.HttpURLConnection 实现，
 * iosMain 用 NSURLSession 实现。上层 WeatherService 完全无感知。
 *
 * 回调方式：onResult(Result<String>)
 * - Result.success(body) → 请求成功，body 为响应体字符串
 * - Result.failure(exception) → 请求失败
 */
expect fun httpGet(url: String, onResult: (Result<String>) -> Unit)
