package com.example.kmpapp

import androidx.compose.ui.window.ComposeUIViewController
import com.example.kmpapp.ui.App
import platform.UIKit.UIViewController

/**
 * iOS 入口工厂函数 —— 创建 ComposeUIViewController。
 *
 * 这个函数被 iOS 的 Swift 层调用（ContentView.swift），
 * 它把 shared 模块的 Compose App() 包装成 UIKit UIViewController。
 *
 * 这是 KMP iOS 集成的标准模式：
 * Kotlin/Native 编译 → .framework → Swift import → 嵌入 SwiftUI
 */
fun MainViewController(): UIViewController = ComposeUIViewController {
    App()
}
