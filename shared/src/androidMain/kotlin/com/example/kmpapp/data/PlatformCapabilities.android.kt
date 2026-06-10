package com.example.kmpapp.data

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.BatteryManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Android actual —— 调用 Android 原生 API 实现平台能力。
 *
 * 展示 KMP expect/actual 机制如何干净地封装平台特性：
 * - 设备信息：Build.MODEL + Build.VERSION
 * - 触觉反馈：Vibrator / VibrationEffect
 * - 剪贴板：ClipboardManager
 * - 定位：简化实现（生产环境需接入 FusedLocationProvider + 权限管理）
 *
 * 所有 Android 特有代码都封装在这里，上层 UI 和业务逻辑完全无感知。
 */
actual class PlatformCapabilities actual constructor() {

    private val context: Context get() = PlatformKeyValueStorage.context

    actual fun getDeviceInfo(): Triple<String, String, Int> {
        val device = "${Build.MANUFACTURER} ${Build.MODEL}"
        val os = "Android ${Build.VERSION.RELEASE}"
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        val battery = batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: -1
        return Triple(device, os, battery)
    }

    actual fun triggerHaptic() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                val vibrator = vibratorManager?.defaultVibrator
                vibrator?.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(30)
                }
            }
        } catch (_: Exception) {
            // 振动不可用时静默忽略
        }
    }

    actual fun copyToClipboard(text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        clipboard?.setPrimaryClip(ClipData.newPlainText("KMP Notes", text))
    }

    actual fun getFromClipboard(): String? {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clip = clipboard?.primaryClip
        return if (clip != null && clip.itemCount > 0) {
            clip.getItemAt(0).text?.toString()
        } else null
    }

    /**
     * Android 定位 —— 简化实现。
     *
     * 生产环境应使用 FusedLocationProviderClient + ACCESS_FINE_LOCATION 权限。
     * 此处返回北京坐标作为演示，展示 expect/actual 的接口一致性。
     */
    actual fun getLocation(onResult: (Double, Double, String) -> Unit) {
        // 尝试获取粗略位置（无需权限的方案）
        try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? android.location.LocationManager
            val lastKnown = locationManager?.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER)
            if (lastKnown != null) {
                onResult(lastKnown.latitude, lastKnown.longitude, "Android 定位 (${lastKnown.latitude.format(2)}, ${lastKnown.longitude.format(2)})")
                return
            }
        } catch (_: Exception) {
            // 权限未授予或定位不可用
        }
        // 回退到默认位置
        onResult(39.9042, 116.4074, "北京 (Android 默认定位)")
    }
}

private fun Double.format(decimals: Int): String {
    val factor = Math.pow(10.0, decimals.toDouble())
    val rounded = Math.round(this * factor) / factor
    return rounded.toString()
}
