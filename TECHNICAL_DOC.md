## KmpNotesApp 技术文档

### 项目概述

KmpNotesApp 是一个基于 Kotlin Multiplatform（KMP）和 Compose Multiplatform（CMP）构建的跨平台本地笔记应用，支持 Android 和 iOS 双端运行。项目的核心目标是展示 KMP 在实际工程中的代码共享能力——从数据层、业务逻辑到 UI 层，同一套 Kotlin 代码在两个平台上零改动共享，仅在操作系统级能力（网络请求、持久化存储）上通过 expect/actual 机制提供平台原生实现。

项目无需后端服务，所有数据存储在设备本地。额外集成了天气查询功能，通过 Open-Meteo 免费 API 获取实时天气数据，并在创建笔记时自动记录天气快照。

### 技术栈与版本

项目基于以下核心依赖构建：

Kotlin 2.0.21，Compose Multiplatform 1.7.1，Gradle 8.11.1，AGP（Android Gradle Plugin）8.7.3，kotlinx-coroutines 1.9.0，kotlinx-datetime 0.6.1，AndroidX Lifecycle ViewModel 2.8.4。Android 端最低 SDK 为 24（Android 7.0），targetSdk 为 35；iOS 端最低部署目标为 iOS 16.0，编译目标为 iosX64、iosArm64 和 iosSimulatorArm64 三个架构。iOS 项目通过 xcodegen 从 project.yml 生成 Xcode 工程文件。

### 项目结构

```
KmpNotesApp/
├── settings.gradle.kts          # 根项目配置，包含 shared 和 androidApp 两个模块
├── build.gradle.kts              # 根构建脚本，声明所有插件（apply false）
├── gradle/libs.versions.toml     # 版本目录，集中管理依赖版本
├── shared/                       # KMP 共享模块（核心）
│   ├── build.gradle.kts          # KMP 插件配置，声明 iOS Framework 输出
│   └── src/
│       ├── commonMain/kotlin/com/example/kmpapp/
│       │   ├── data/
│       │   │   ├── Platform.kt          # expect 声明（存储、HTTP、平台名）
│       │   │   ├── Note.kt              # 笔记数据模型 + 手写 JSON 序列化
│       │   │   ├── NoteRepository.kt    # 笔记 CRUD 仓库
│       │   │   ├── NoteStorage.kt       # 存储接口 + 平台 KV 桥接
│       │   │   ├── WeatherData.kt       # 天气数据模型 + WMO 代码映射
│       │   │   └── WeatherService.kt    # 天气服务（Open-Meteo API）
│       │   ├── domain/
│       │   │   └── NotesViewModel.kt    # ViewModel（StateFlow 驱动 UI）
│       │   └── ui/
│       │       ├── App.kt               # 应用入口 + 页面导航
│       │       ├── theme/Theme.kt       # Material3 主题配置
│       │       ├── screens/
│       │       │   ├── NoteListScreen.kt   # 笔记列表页（交错网格 + FAB）
│       │       │   └── NoteEditScreen.kt   # 笔记编辑页（表单 + 天气卡片）
│       │       └── components/
│       │           ├── NoteCard.kt         # 笔记卡片（含天气标签）
│       │           └── WeatherCard.kt      # 天气卡片（渐变背景 + 城市切换）
│       ├── androidMain/kotlin/.../data/
│       │   └── Platform.android.kt      # Android actual（SharedPreferences + HttpURLConnection）
│       └── iosMain/kotlin/.../
│           ├── MainViewController.kt    # ComposeUIViewController 工厂
│           └── data/Platform.ios.kt     # iOS actual（NSUserDefaults + NSURLSession delegate）
├── androidApp/                   # Android 宿主应用
│   ├── build.gradle.kts          # Android Application 配置
│   └── src/main/
│       ├── AndroidManifest.xml   # 权限声明（INTERNET）
│       └── kotlin/.../MainActivity.kt  # setContent { App() }
└── iosApp/                       # iOS 宿主应用
    ├── project.yml               # xcodegen 项目描述
    └── iosApp/
        ├── iOSApp.swift          # SwiftUI @main 入口
        ├── ContentView.swift     # ComposeView 桥接容器
        └── Info.plist            # CADisableMinimumFrameDurationOnPhone 等配置
```

### 架构设计

项目采用分层架构，从下至上依次为：平台层（Platform）、数据层（Data）、领域层（Domain）、UI 层。各层之间的依赖方向为单向的——UI 依赖 Domain，Domain 依赖 Data，Data 依赖 Platform。

**平台层** 通过 expect/actual 机制实现跨平台桥接。commonMain 中声明三个 expect：`PlatformKeyValueStorage` 类提供键值存储的 put/get/remove 操作；`httpGet` 函数提供异步 HTTP GET 请求；`getPlatformName` 返回平台标识字符串。androidMain 使用 `SharedPreferences` 和 `java.net.HttpURLConnection` 实现，iosMain 使用 `NSUserDefaults` 和 `NSURLSession` delegate 模式实现。

**数据层** 包含 Note 数据模型（带手写 JSON 序列化/反序列化）、NoteRepository（提供增删改查操作）、NoteStorage 接口及其基于 PlatformKeyValueStorage 的实现 PlatformNoteStorage，以及天气相关的 WeatherData 模型和 WeatherService。Note 模型的 JSON 解析器是手写的，避免了引入 kotlinx.serialization 插件的复杂性，同时处理了字符串边界内的花括号计数和 JSON 转义字符的单遍解码问题。

**领域层** 以 NotesViewModel 为核心，使用 kotlinx.coroutines 的 StateFlow 管理应用状态。ViewModel 持有 NoteRepository 实例，暴露 notes（笔记列表）、isLoading、totalCount 等响应式状态流，UI 层通过 collectAsState 订阅。

**UI 层** 完全由 Compose Multiplatform 实现，所有 Composable 函数在 Android 和 iOS 上共用同一份代码。页面导航使用手动管理的 sealed class Screen 状态配合 AnimatedContent 实现页面切换动画，无需引入 Navigation 库。

### expect/actual 机制详解

expect/actual 是 KMP 的核心跨平台桥接机制。commonMain 中的 expect 声明定义了"接口契约"——告诉编译器这些符号在编译期存在，具体实现由各平台的 actual 提供。本项目中有三组 expect/actual：

**存储层**：commonMain 声明 `expect class PlatformKeyValueStorage`，Android 端用 SharedPreferences 实现（通过 Application context 获取），iOS 端用 NSUserDefaults.standardUserDefaults 实现。上层 PlatformNoteStorage 完全不知道底层用的是什么存储引擎。

**网络层**：commonMain 声明 `expect fun httpGet(url: String, onResult: (Result<String>) -> Unit)`。Android 端使用 java.net.HttpURLConnection 在独立线程中执行请求。iOS 端面临 Kotlin/Native 对 ObjC 绑定的限制——NSURLSession 的 completion handler 重载在 Kotlin/Native 中不可用，因此采用 delegate 模式：定义 `HttpHelper` 类继承 NSObject 并实现 `NSURLSessionDataDelegateProtocol` 和 `NSURLSessionDelegateProtocol`，在 `URLSession(session:, dataTask:, didReceiveData:)` 回调中通过 `kotlinx.cinterop` 的 `usePinned` + `memcpy` 将 NSData 字节拷贝到 Kotlin ByteArray，最后在 `URLSession(session:, task:, didCompleteWithError:)` 回调中拼接所有数据块并 decodeToString。

**平台标识**：简单的 `expect fun getPlatformName(): String`，各端返回 "Android" 或 "iOS"，在列表页副标题展示。

### 功能模块

**笔记管理**：支持笔记的创建、查看和删除。每条笔记包含标题、正文、背景颜色、置顶标记、创建时间和天气快照字段。数据以 JSON 格式序列化为字符串，通过 PlatformKeyValueStorage 持久化到本地。列表页使用 LazyVerticalStaggeredGrid 实现交错网格布局，每张 NoteCard 显示笔记摘要，若有天气快照则在卡片底部展示天气标签（天气图标 + 温度 + 城市名）。

**天气查询**：集成 Open-Meteo 免费天气 API（无需 API Key），默认查询北京天气。WeatherService 使用 StateFlow 管理 weather、isLoading、error 三个状态，WeatherCard 组件以蓝色渐变为背景展示温度、体感温度、天气状况图标、湿度、风速。支持从五个预设城市（北京、上海、东京、纽约、伦敦）中切换，每次切换或点击刷新按钮时自动重新请求数据。天气数据使用 WMO Weather Code 标准映射到中文描述和 Material Icon。

**天气快照**：在笔记编辑页进入时自动获取当前天气并显示在 WeatherCard 中。用户保存笔记时，WeatherData 的 JSON 序列化结果作为 weatherSnapshot 字段存入 Note 对象，后续在列表页的 NoteCard 上以标签形式展示创建时的天气信息。

### iOS 集成要点

iOS 宿主应用是一个标准的 SwiftUI 项目，入口 `iOSApp.swift` 使用 `@main` 标记的 App 结构体，在 WindowGroup 中嵌入 ContentView。ContentView 包含一个 ComposeView，它是 `UIViewControllerRepresentable` 的实现，在 `makeUIViewController` 中调用 `MainViewControllerKt.MainViewController()` 获取 Compose Multiplatform 提供的 UIViewController。

shared 模块编译为静态 iOS Framework（isStatic = true），通过 xcodegen 生成的 Xcode 工程在 pre-build script 中调用 `./gradlew :shared:embedAndSignAppleFrameworkForXcode` 自动编译 Kotlin/Native 代码。Info.plist 中必须包含 `CADisableMinimumFrameDurationOnPhone = true`，否则 Compose Multiplatform 在初始化时会抛出 IllegalStateException。

### 测试工具与步骤

#### Android 端测试

Android 端的测试使用 Android Studio 自带的模拟器完成。具体步骤如下：

第一步，通过 `./gradlew :androidApp:assembleDebug` 构建 Debug APK，构建产物位于 `androidApp/build/outputs/apk/debug/androidApp-debug.apk`。

第二步，使用 Android Debug Bridge（adb）将 APK 安装到 Pixel 7 模拟器：`adb install androidApp-debug.apk`。

第三步，通过 adb 启动应用：`adb shell am start -n com.example.kmpapp/.MainActivity`，验证笔记列表页正常渲染，天气卡片显示北京天气数据（20°C、晴朗、64% 湿度），FAB 按钮可点击创建笔记，编辑页的标题输入、正文输入、颜色选择和保存功能均正常工作。

#### iOS 端测试

iOS 端的测试在 macOS 的 Xcode Simulator 上进行，使用 iPhone 17 Pro 模拟器（iOS 26.5）。由于 iOS 项目依赖 shared 模块的 Kotlin/Native Framework，构建流程比 Android 略复杂：

**构建与安装**：首先通过 xcodegen 生成 Xcode 工程文件（`cd iosApp && xcodegen generate`），然后使用 xcodebuild 编译：`DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer xcodebuild -project iosApp.xcodeproj -scheme iosApp -destination "id=<DEVICE_ID>" build`。构建产物位于 `build/Build/Products/Debug-iphonesimulator/iosApp.app`，通过 `xcrun simctl install <DEVICE_ID> iosApp.app` 安装到模拟器。

**启动应用**：使用 `xcrun simctl launch --console-pty <DEVICE_ID> com.example.kmpapp` 启动应用。`--console-pty` 参数可以捕获应用的实时控制台输出，便于排查启动异常。

**截图验证**：iOS 26.5 模拟器的 `xcrun simctl io screenshot` 命令存在间歇性问题（偶发只截取主屏幕而非应用画面）。作为补充手段，通过 Swift 脚本调用 `CGWindowListCopyWindowInfo` 获取 Simulator.app 窗口的 CGWindowID，再用 macOS 的 `screencapture -l <WINDOW_ID>` 直接捕获窗口内容。

**触摸模拟**：模拟器的 `simctl io` 命令在 iOS 26.5 版本中不支持 `tap` 和 `type` 操作。测试中使用以下替代方案进行 UI 交互：

- `cliclick`（通过 Homebrew 安装）：命令行鼠标模拟工具，用 `cliclick "c:x,y"` 模拟点击、`cliclick "t:text"` 模拟文本输入。
- 自编译的 Swift 触摸工具：使用 `CGEvent` API 编写 `click_tool`（模拟单击）和 `swipe_tool`（模拟拖拽手势），通过 `swiftc` 编译为可执行文件。
- AppleScript `keystroke`：通过 System Events 发送键盘事件，但实测对 Compose Multiplatform 的文本字段无效。
- `osascript` 键码命令：`key code 36`（Return）、`key code 53`（Escape）、`key code 123 using command down`（Cmd+Left）等。

**日志分析**：使用 `xcrun simctl spawn <DEVICE_ID> log show --predicate 'process == "iosApp"' --last 30s --style compact` 查看系统日志。关键日志包括 `UISceneActivationStateForegroundActive`（确认应用在前台运行）、`Sharedandroidx.compose.ui.window.UserInputView7`（确认 Compose UI 已加载）、以及 Metal 和 IOSurface 连接日志（确认 GPU 渲染正常）。

**崩溃诊断**：通过检查 `~/Library/Logs/DiagnosticReports/` 目录下的 `.ips` 崩溃日志文件定位问题。本项目中首次发现 Compose Multiplatform 因缺少 `CADisableMinimumFrameDurationOnPhone` 配置而抛出 Kotlin IllegalStateException 导致应用崩溃，通过查看崩溃堆栈中的 `PlistSanityCheck.performIfNeeded` 定位并修复。

#### 开发环境注意事项

构建环境中 `xcode-select` 指向 `/Library/Developer/CommandLineTools` 而非 Xcode.app，导致 `xcrun simctl` 命令无法找到模拟器设备。解决方案是在所有 simctl 和 xcodebuild 命令前添加环境变量 `DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer`，永久修复需要 `sudo xcode-select -s /Applications/Xcode.app/Contents/Developer`。

Gradle 8.11.1 的下载在某些网络环境下会超时，可以通过腾讯镜像 `mirrors.cloud.tencent.com/gradle/` 手动下载并配置 Gradle wrapper 缓存。
