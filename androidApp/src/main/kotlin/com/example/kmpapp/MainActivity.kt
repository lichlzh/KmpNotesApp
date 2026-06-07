package com.example.kmpapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.kmpapp.ui.App

/**
 * Android 入口 Activity —— 唯一需要写的 Android 特有代码。
 *
 * enableEdgeToEdge() 启用沉浸式状态栏，
 * setContent 直接调用 shared 模块的 App() Composable。
 * 整个 UI 和业务逻辑都在 shared 中，Android 层只做壳。
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            App()
        }
    }
}
