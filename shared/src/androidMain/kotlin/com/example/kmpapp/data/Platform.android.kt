package com.example.kmpapp.data

import android.content.Context
import android.content.SharedPreferences
import java.net.HttpURLConnection
import java.net.URL

/**
 * Android actual 实现 —— 使用 SharedPreferences 持久化键值对。
 *
 * 通过全局 Context 获取 SharedPreferences 实例，
 * 这是 Android 平台上最轻量的本地存储方案。
 */
actual class PlatformKeyValueStorage actual constructor() {

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("kmp_notes_prefs", Context.MODE_PRIVATE)
    }

    actual fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    actual fun getString(key: String): String? = prefs.getString(key, null)

    actual fun remove(key: String) {
        prefs.edit().remove(key).apply()
    }

    companion object {
        /** 由 Android Application 初始化时注入 */
        lateinit var context: Context
    }
}

actual fun getPlatformName(): String = "Android"

/**
 * Android HTTP 实现 —— 使用 java.net.HttpURLConnection。
 * 在后台线程执行网络请求，避免阻塞主线程。
 * 这是零依赖方案，生产项目建议使用 Ktor。
 */
actual fun httpGet(url: String, onResult: (Result<String>) -> Unit) {
    Thread {
        try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10_000
            connection.readTimeout = 10_000
            connection.setRequestProperty("Accept", "application/json")

            val responseCode = connection.responseCode
            if (responseCode in 200..299) {
                val body = connection.inputStream.bufferedReader().readText()
                onResult(Result.success(body))
            } else {
                onResult(Result.failure(Exception("HTTP $responseCode")))
            }
            connection.disconnect()
        } catch (e: Exception) {
            onResult(Result.failure(e))
        }
    }.start()
}
