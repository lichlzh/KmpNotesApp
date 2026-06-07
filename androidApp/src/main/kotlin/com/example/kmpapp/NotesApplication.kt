package com.example.kmpapp

import android.app.Application
import com.example.kmpapp.data.PlatformKeyValueStorage

/**
 * Android Application 子类 —— 为 shared 模块注入 Context。
 *
 * KMP 的 expect/actual 机制中，Android 端的 PlatformKeyValueStorage
 * 需要一个 Context 来访问 SharedPreferences。
 * 这里在应用启动时完成注入。
 */
class NotesApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        PlatformKeyValueStorage.context = this
    }
}
