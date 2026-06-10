package com.example.kmpapp.data

/**
 * 平台能力抽象 —— expect/actual 机制的深度应用。
 *
 * 将 Android/iOS 的原生能力（设备信息、触觉反馈、剪贴板、定位）
 * 封装成统一接口，上层 ViewModel 和 Compose UI 完全不感知
 * 自己在哪个平台上运行。
 *
 * 这是 KMP "平台感知" 的核心：
 * - 上层代码写一次，双平台行为一致
 * - 每个 actual 实现调用各自平台的原生 API
 * - 新增平台（如 Desktop）只需添加一套 actual 实现
 */
expect class PlatformCapabilities() {

    /**
     * 获取设备信息
     * @return Triple(设备型号, 系统版本, 电量百分比 0~100)
     */
    fun getDeviceInfo(): Triple<String, String, Int>

    /** 触发轻触觉反馈（Android 振动 / iOS UIImpactFeedback） */
    fun triggerHaptic()

    /** 复制文本到系统剪贴板 */
    fun copyToClipboard(text: String)

    /** 从系统剪贴板读取文本 */
    fun getFromClipboard(): String?

    /**
     * 获取当前位置（简化实现，演示 expect/actual 模式）
     * 回调参数：(纬度, 经度, 地址描述)
     *
     * 生产环境应使用完整的定位权限流程：
     * - Android: FusedLocationProvider + ACCESS_FINE_LOCATION
     * - iOS: CLLocationManager + WhenInUse 授权
     */
    fun getLocation(onResult: (Double, Double, String) -> Unit)
}
