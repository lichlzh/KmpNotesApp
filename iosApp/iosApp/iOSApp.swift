import SwiftUI
import shared

/**
 * iOS 应用入口 —— AppDelegate 模式。
 *
 * Compose Multiplatform 在 iOS 上使用 ComposeUIViewController，
 * 通过 Kotlin/Native 编译的 shared 框架提供 UI。
 * 这里 iOS 原生层只负责启动和宿主容器。
 */
@main
struct iOSApp: App {
    var body: some Scene {
        WindowGroup {
            ContentView()
                .ignoresSafeArea(.all) // Compose UI 自行处理安全区域
        }
    }
}
