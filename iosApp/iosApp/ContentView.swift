import UIKit
import SwiftUI
import shared

/**
 * iOS 宿主视图 —— 使用 ComposeUIViewController 桥接 Compose Multiplatform UI。
 *
 * ComposeUIViewController 是 JetBrains 提供的桥接组件，
 * 它把 Compose UI 嵌入到 UIKit/SwiftUI 的视图层级中。
 * App() 是 shared 模块中的 Composable 入口函数。
 */
struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea(.all)
    }
}
