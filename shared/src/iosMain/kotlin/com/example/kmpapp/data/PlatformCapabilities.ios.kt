package com.example.kmpapp.data

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSString
import platform.UIKit.UIDevice
import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle
import platform.UIKit.UIApplication

/**
 * iOS actual —— 调用 iOS 原生 API 实现平台能力。
 *
 * 展示 KMP expect/actual 机制如何封装 iOS 原生 API：
 * - 设备信息：UIDevice + NSProcessInfo
 * - 触觉反馈：UIImpactFeedbackGenerator
 * - 剪贴板：UIPasteboard
 * - 定位：简化实现（生产环境需接入 CLLocationManager + 权限授权）
 *
 * 使用 Kotlin/Native 的 ObjC interop 直接调用 Foundation/UIKit API，
 * 无需任何中间层或桥接代码。
 */
actual class PlatformCapabilities actual constructor() {

    actual fun getDeviceInfo(): Triple<String, String, Int> {
        val device = UIDevice.currentDevice
        val model = device.model ?: "iPhone"
        val osVersion = device.systemVersion ?: "unknown"
        val os = "iOS $osVersion"
        // 电池监控默认未启用，返回 -1 表示未知
        // 生产环境可调用 UIDevice.currentDevice.setBatteryMonitoringEnabled(true)
        val battery = if (device.batteryMonitoringEnabled) {
            (device.batteryLevel * 100).toInt()
        } else -1
        return Triple(model, os, battery)
    }

    actual fun triggerHaptic() {
        val generator = UIImpactFeedbackGenerator(style = UIImpactFeedbackStyle.UIImpactFeedbackStyleLight)
        generator.prepare()
        generator.impactOccurred()
    }

    actual fun copyToClipboard(text: String) {
        val pasteboard = platform.UIKit.UIPasteboard.generalPasteboard
        pasteboard.string = text
    }

    actual fun getFromClipboard(): String? {
        val pasteboard = platform.UIKit.UIPasteboard.generalPasteboard
        return pasteboard.string
    }

    /**
     * iOS 定位 —— 简化实现。
     *
     * 生产环境应使用 CLLocationManager + requestWhenInUseAuthorization。
     * 此处返回北京坐标作为演示，展示 expect/actual 接口一致性。
     *
     * 完整实现需要：
     * 1. Info.plist 添加 NSLocationWhenInUseUsageDescription
     * 2. CLLocationManagerDelegate 子类处理回调
     * 3. 请求用户授权 → 获取位置 → 通过 CLGeocoder 反解析地址
     */
    actual fun getLocation(onResult: (Double, Double, String) -> Unit) {
        // iOS 模拟器/真机回退到默认位置
        onResult(39.9042, 116.4074, "北京 (iOS 默认定位)")
    }
}
