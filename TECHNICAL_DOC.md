## KmpNotesApp 技术文档

### 项目概述

KmpNotesApp 是一个基于 Kotlin Multiplatform（KMP）和 Compose Multiplatform（CMP）构建的跨平台本地笔记应用，支持 Android 和 iOS 双端运行。项目的核心目标是展示 KMP 在实际工程中的代码共享能力——从数据层、业务逻辑到 UI 层，同一套 Kotlin 代码在两个平台上零改动共享，仅在操作系统级能力（网络请求、持久化存储、设备信息、触觉反馈、剪贴板、定位）上通过 expect/actual 机制提供平台原生实现。

项目无需后端服务，所有数据存储在设备本地。在此基础上，项目实现了三个核心功能来展示 KMP 的动态化能力：服务端驱动的智能仪表盘首页（JSON 配置决定 UI 布局）、远程主题引擎（运行时切换整套配色方案）、平台感知笔记（富媒体附件通过 expect/actual 调用原生能力）。这三个功能分别从 UI 层、逻辑层、平台层展示了 KMP 在跨平台开发中的完整优势。

### 技术栈与版本

项目基于以下核心依赖构建：

Kotlin 2.0.21，Compose Multiplatform 1.7.1，Gradle 8.11.1，AGP（Android Gradle Plugin）8.7.3，kotlinx-coroutines 1.9.0，kotlinx-datetime 0.6.1，AndroidX Lifecycle ViewModel 2.8.4。Android 端最低 SDK 为 24（Android 7.0），targetSdk 为 35；iOS 端最低部署目标为 iOS 16.0，编译目标为 iosX64、iosArm64 和 iosSimulatorArm64 三个架构。iOS 项目通过 xcodegen 从 project.yml 生成 Xcode 工程文件。

### 项目结构

```
KmpNotesApp/
├── settings.gradle.kts              # 根项目配置，包含 shared 和 androidApp 两个模块
├── build.gradle.kts                  # 根构建脚本，声明所有插件（apply false）
├── gradle/libs.versions.toml         # 版本目录，集中管理依赖版本
├── TECHNICAL_DOC.md                  # 技术文档（本文件）
├── KMP_FEATURE_DESIGN.md             # 三个动态化功能的设计方案
├── shared/                           # KMP 共享模块（核心）
│   ├── build.gradle.kts              # KMP 插件配置，声明 iOS Framework 输出
│   └── src/
│       ├── commonMain/kotlin/com/example/kmpapp/
│       │   ├── data/
│       │   │   ├── Platform.kt               # expect 声明（存储、HTTP、平台名）
│       │   │   ├── PlatformCapabilities.kt   # expect 声明（设备信息、触觉、剪贴板、定位）★
│       │   │   ├── Note.kt                   # 笔记数据模型 + 手写 JSON 序列化（含附件）
│       │   │   ├── NoteAttachment.kt         # 附件密封类（位置/设备/清单/链接）+ JSON ★
│       │   │   ├── NoteRepository.kt         # 笔记 CRUD 仓库（object 单例）
│       │   │   ├── NoteStorage.kt            # 存储接口 + 平台 KV 桥接
│       │   │   ├── DashboardConfig.kt        # 仪表盘配置模型 + JSON 解析/序列化 ★
│       │   │   ├── DashboardService.kt       # 仪表盘配置持久化服务 ★
│       │   │   ├── ThemeConfig.kt            # 主题配置模型 + 4 套内置主题 ★
│       │   │   ├── ThemeEngine.kt            # 主题引擎（加载/保存/切换/导出）★
│       │   │   ├── WeatherData.kt            # 天气数据模型 + WMO 代码映射
│       │   │   └── WeatherService.kt         # 天气服务（Open-Meteo API）
│       │   ├── domain/
│       │   │   ├── NotesViewModel.kt         # ViewModel（StateFlow 驱动 UI）
│       │   │   ├── DashboardViewModel.kt     # 仪表盘 ViewModel（统计/问候/配置）★
│       │   │   └── AttachmentManager.kt      # 附件管理器（添加/删除/序列化）★
│       │   └── ui/
│       │       ├── App.kt                    # 应用入口 + 页面导航（含 Dashboard）
│       │       ├── theme/Theme.kt            # Material3 主题配置（含 DynamicAppTheme）
│       │       ├── screens/
│       │       │   ├── DashboardScreen.kt       # 智能仪表盘首页（配置驱动渲染）★
│       │       │   ├── NoteListScreen.kt        # 笔记列表页（交错网格 + FAB）
│       │       │   ├── NoteEditScreen.kt        # 笔记编辑页（表单 + 天气 + 附件）
│       │       │   └── ThemeSettingsScreen.kt   # 主题设置页（预览/切换/导出）★
│       │       └── components/
│       │           ├── NoteCard.kt              # 笔记卡片（含天气标签 + 附件指示）
│       │           ├── WeatherCard.kt           # 天气卡片（渐变背景 + 城市切换）
│       │           ├── ActionsCard.kt           # 快捷操作卡片（3列网格）★
│       │           ├── StatsCard.kt             # 数据统计卡片（2×2网格）★
│       │           ├── QuoteCard.kt             # 每日一言卡片（渐变 + 动画）★
│       │           ├── RecentNotesCard.kt       # 最近笔记横向滚动卡片 ★
│       │           ├── AttachmentPickerBar.kt   # 附件选择工具栏 ★
│       │           └── AttachmentCard.kt        # 4种附件卡片渲染器 ★
│       ├── androidMain/kotlin/.../data/
│       │   ├── Platform.android.kt              # Android actual（SP + HttpURLConnection）
│       │   └── PlatformCapabilities.android.kt  # Android actual（Build + Vibrator + Clipboard）★
│       └── iosMain/kotlin/.../
│           ├── MainViewController.kt            # ComposeUIViewController 工厂
│           ├── data/Platform.ios.kt             # iOS actual（NSUserDefaults + NSURLSession）
│           └── data/PlatformCapabilities.ios.kt # iOS actual（UIDevice + Haptic + Pasteboard）★
├── androidApp/                       # Android 宿主应用
│   ├── build.gradle.kts              # Android Application 配置
│   └── src/main/
│       ├── AndroidManifest.xml       # 权限声明（INTERNET + VIBRATE + LOCATION）
│       └── kotlin/.../
│           ├── MainActivity.kt       # setContent { App() }
│           └── NotesApplication.kt   # 注入 Context 到 PlatformKeyValueStorage
└── iosApp/                           # iOS 宿主应用
    ├── project.yml                   # xcodegen 项目描述
    └── iosApp/
        ├── iOSApp.swift              # SwiftUI @main 入口
        ├── ContentView.swift         # ComposeView 桥接容器
        └── Info.plist                # CADisable + NSLocationWhenInUseUsageDescription
```

> 标注 ★ 的文件为三个新功能新增的文件。

### 架构设计

项目采用分层架构，从下至上依次为：平台层（Platform）、数据层（Data）、领域层（Domain）、UI 层。各层之间的依赖方向为单向的——UI 依赖 Domain，Domain 依赖 Data，Data 依赖 Platform。

**平台层** 通过 expect/actual 机制实现跨平台桥接。commonMain 中声明两组 expect：第一组是基础能力——`PlatformKeyValueStorage` 类提供键值存储的 put/get/remove 操作，`httpGet` 函数提供异步 HTTP GET 请求，`getPlatformName` 返回平台标识字符串；第二组是平台原生能力——`PlatformCapabilities` 类封装了设备信息获取、触觉反馈、剪贴板读写和 GPS 定位四种能力。androidMain 使用 SharedPreferences、HttpURLConnection、Build API、Vibrator、ClipboardManager、LocationManager 实现，iosMain 使用 NSUserDefaults、NSURLSession delegate、UIDevice、UIImpactFeedbackGenerator、UIPasteboard 实现。

**数据层** 包含数据模型和服务两类组件。数据模型方面，Note 模型带手写 JSON 序列化/反序列化（支持附件字段）、NoteAttachment 密封类定义了四种附件类型（Location、DeviceInfo、Checklist、ShareLink）、DashboardConfig 和 ThemeConfig 分别描述仪表盘布局和主题配色。服务方面，NoteRepository（object 单例）提供笔记增删改查、DashboardService 管理仪表盘配置的持久化、ThemeEngine 管理主题的加载和切换、WeatherService 对接 Open-Meteo API。所有数据模型和服务均实现了 JSON 序列化/反序列化，为远程配置下发提供基础。

**领域层** 包含 NotesViewModel、DashboardViewModel 和 AttachmentManager。NotesViewModel 使用 StateFlow 管理笔记列表、搜索、分类筛选等状态；DashboardViewModel（object 单例）管理仪表盘配置、笔记统计数据和时间问候语；AttachmentManager 管理笔记附件的添加、删除、状态切换和序列化。ViewModel 与 UI 之间通过 StateFlow + collectAsState 实现响应式数据绑定。

**UI 层** 完全由 Compose Multiplatform 实现，所有 Composable 函数在 Android 和 iOS 上共用同一份代码。页面导航使用手动管理的 sealed class Screen 状态（Dashboard / List / Edit / ThemeSettings）配合 AnimatedContent 实现页面切换动画。DynamicAppTheme 从 ThemeEngine 的 StateFlow 读取当前主题配置，构建 Material3 ColorScheme，切换主题时 Compose 自动 recompose 整棵 UI 树。DashboardScreen 实现了一个配置驱动的渲染引擎——根据 DashboardConfig 的 sections 列表，按 type 字段分发到对应的卡片 Composable 组件。

### expect/actual 机制详解

expect/actual 是 KMP 的核心跨平台桥接机制。commonMain 中的 expect 声明定义了"接口契约"——告诉编译器这些符号在编译期存在，具体实现由各平台的 actual 提供。本项目中有四组 expect/actual：

**存储层**：commonMain 声明 `expect class PlatformKeyValueStorage`，Android 端用 SharedPreferences 实现（通过 Application context 获取），iOS 端用 NSUserDefaults.standardUserDefaults 实现。上层 PlatformNoteStorage 完全不知道底层用的是什么存储引擎。

**网络层**：commonMain 声明 `expect fun httpGet(url: String, onResult: (Result<String>) -> Unit)`。Android 端使用 java.net.HttpURLConnection 在独立线程中执行请求。iOS 端面临 Kotlin/Native 对 ObjC 绑定的限制——NSURLSession 的 completion handler 重载在 Kotlin/Native 中不可用，因此采用 delegate 模式：定义 `HttpHelper` 类继承 NSObject 并实现 `NSURLSessionDataDelegateProtocol` 和 `NSURLSessionDelegateProtocol`，在 `URLSession(session:, dataTask:, didReceiveData:)` 回调中通过 `kotlinx.cinterop` 的 `usePinned` + `memcpy` 将 NSData 字节拷贝到 Kotlin ByteArray，最后在 `URLSession(session:, task:, didCompleteWithError:)` 回调中拼接所有数据块并 decodeToString。

**平台标识**：简单的 `expect fun getPlatformName(): String`，各端返回 "Android" 或 "iOS"，在列表页副标题展示。

**平台能力**（Feature 3 新增）：commonMain 声明 `expect class PlatformCapabilities`，提供四种原生能力的统一接口。这是项目中最复杂的 expect/actual 应用，展示了如何将 Android 和 iOS 的系统 API 干净地封装到共享层：

- `getDeviceInfo()` 返回 Triple(设备型号, 系统版本, 电量百分比)。Android 端通过 `Build.MANUFACTURER + Build.MODEL` 获取设备型号，`Build.VERSION.RELEASE` 获取系统版本，`BatteryManager.getIntProperty(BATTERY_PROPERTY_CAPACITY)` 获取电量。iOS 端通过 `UIDevice.currentDevice.model` 获取设备型号，`UIDevice.currentDevice.systemVersion` 获取系统版本，`UIDevice.currentDevice.batteryLevel` 获取电量。

- `triggerHaptic()` 触发轻触觉反馈。Android 端使用 `VibratorManager`（API 31+）或回退到旧版 `Vibrator` + `VibrationEffect.createOneShot`，兼容不同 API 版本。iOS 端使用 `UIImpactFeedbackGenerator` 配合 `UIImpactFeedbackStyleLight`，这是 iOS 标准的触觉反馈 API。

- `copyToClipboard(text)` 和 `getFromClipboard()` 操作剪贴板。Android 端使用 `ClipboardManager` + `ClipData.newPlainText`。iOS 端使用 `UIPasteboard.generalPasteboard` 的 string 属性，Kotlin/Native 对 UIPasteboard 的绑定非常直接。

- `getLocation(onResult)` 获取当前位置。Android 端尝试通过 `LocationManager.getLastKnownLocation(NETWORK_PROVIDER)` 获取粗略位置，权限不足时回退到北京坐标。iOS 端简化实现为返回默认坐标（完整实现需 CLLocationManager + 授权流程）。两端返回相同签名的回调，上层 AttachmentManager 完全不感知定位的实现差异。

### 功能模块

**笔记管理**：支持笔记的创建、查看和删除。每条笔记包含标题、正文、背景颜色、置顶标记、创建时间、天气快照和附件列表字段。数据以 JSON 格式序列化为字符串，通过 PlatformKeyValueStorage 持久化到本地。列表页使用 LazyVerticalStaggeredGrid 实现交错网格布局，每张 NoteCard 显示笔记摘要，若有天气快照则在卡片底部展示天气标签，若有附件则展示附件类型图标（位置/设备/清单/链接）和数量。

**天气查询**：集成 Open-Meteo 免费天气 API（无需 API Key），默认查询北京天气。WeatherService 使用 StateFlow 管理 weather、isLoading、error 三个状态，WeatherCard 组件以蓝色渐变为背景展示温度、体感温度、天气状况图标、湿度、风速。支持从五个预设城市（北京、上海、东京、纽约、伦敦）中切换，每次切换或点击刷新按钮时自动重新请求数据。天气数据使用 WMO Weather Code 标准映射到中文描述和 emoji 图标。

**天气快照**：在笔记编辑页进入时自动获取当前天气并显示在 WeatherCard 中。用户保存笔记时，WeatherData 的 JSON 序列化结果作为 weatherSnapshot 字段存入 Note 对象，后续在列表页的 NoteCard 上以标签形式展示创建时的天气信息。

**智能仪表盘首页**（Feature 1）：应用启动后进入 DashboardScreen，这是整个动态化能力的核心展示。页面布局完全由 DashboardConfig 的 JSON 配置驱动——配置中定义了一个 sections 数组，每个 section 包含 id、type、title、size 和 config 属性。DashboardScreen 的渲染引擎遍历 sections 列表，根据 type 字段分发到对应的 Composable 组件：

- `weather` 类型渲染 WeatherCard，复用现有的天气卡片组件。
- `actions` 类型渲染 ActionsCard，一个 3 列等宽网格的快捷操作按钮栏，按钮列表从 config.items JSON 数组解析，支持 add、list、search、palette、star 五种图标。
- `stats` 类型渲染 StatsCard，2×2 网格展示四个核心指标（笔记总数、置顶数、近 7 天更新数、含天气快照数），数据由 DashboardViewModel 从 NoteRepository 实时计算。
- `quote` 类型渲染 QuoteCard，渐变背景 + 名言/诗句展示，支持 AnimatedContent 动画切换。
- `note_list` 类型渲染 RecentNotesCard，横向滚动的笔记预览卡片列表，点击跳转到编辑页。
- 未知类型优雅降级为一个占位提示，保证旧版本 App 遇到新配置时不会崩溃。

配置通过 DashboardService 持久化到平台 Key-Value 存储（SharedPreferences / NSUserDefaults），首次安装使用 DashboardConfig.default() 提供的默认配置。支持从 JSON 字符串加载新配置（模拟远程下发），TopAppBar 提供刷新按钮重新加载配置。整个 Dashboard 的解析和渲染逻辑 100% 在 commonMain 中实现，新增卡片类型只需添加一个 type 字符串和对应的 Composable 渲染函数。

**远程主题引擎**（Feature 2）：App 的配色方案由 ThemeConfig 的 JSON 配置驱动，包含完整的 Material3 色彩体系（primary、onPrimary、primaryContainer、secondary、tertiary、background、surface、surfaceVariant 等 18 个色值）以及圆角和阴影参数。ThemeEngine 作为 object 单例管理主题状态，通过 StateFlow 暴露 currentTheme。DynamicAppTheme Composable 在顶层订阅这个 StateFlow，将 ThemeConfig 转为 Material3 的 lightColorScheme 或 darkColorScheme，传入 MaterialTheme。切换主题时只需调用 ThemeEngine.applyTheme()，StateFlow 更新触发 Compose recompose，整个 UI 树自动刷新颜色——无需重新编译、无需重启应用。

内置四套主题：暖阳橙（浅色，项目默认）、深海蓝（浅色，沉稳专业）、森林绿（浅色，自然清新）、暗夜紫（深色模式）。ThemeSettingsScreen 提供主题选择界面，包含渐变色预览条、色块展示、选中状态指示、主题详情（主色调和背景色的色块预览），以及当前主题的完整 JSON 导出（可直接复制到其他设备导入使用）。DashboardScreen 的 TopAppBar 提供调色板图标快速入口。主题选择持久化到本地存储，下次启动自动恢复。

**平台感知笔记**（Feature 3）：笔记编辑页新增富媒体附件系统，支持四种附件类型。附件选择通过 AttachmentPickerBar 工具栏实现（横滚按钮栏），附件展示通过 AttachmentCard 渲染器根据类型分发到四种专用卡片组件。AttachmentManager 管理附件列表的状态（添加、删除、清单项切换、链接复制），并提供序列化/反序列化接口。

四种附件类型的设计各有侧重：Location 附件展示 expect/actual 调用平台定位 API 的能力，卡片以蓝色渐变背景展示经纬度和地址描述；DeviceInfo 附件展示 expect/actual 读取设备硬件信息的能力，卡片以紫色调展示设备型号、系统版本和电量进度条；Checklist 附件是纯共享代码，展示 Compose 的交互式状态管理能力（Checkbox 勾选、进度条联动），不涉及任何平台 API；ShareLink 附件展示 expect/actual 剪贴板集成，卡片以橙色调展示 URL 标题并提供一键复制功能。

附件数据通过 NoteAttachment.listToJson / listFromJson 序列化为 JSON 数组字符串，作为 Note.attachmentsJson 字段持久化。NoteCard 在列表页通过图标 + 数量的方式展示笔记包含的附件类型。

### iOS 集成要点

iOS 宿主应用是一个标准的 SwiftUI 项目，入口 `iOSApp.swift` 使用 `@main` 标记的 App 结构体，在 WindowGroup 中嵌入 ContentView。ContentView 包含一个 ComposeView，它是 `UIViewControllerRepresentable` 的实现，在 `makeUIViewController` 中调用 `MainViewControllerKt.MainViewController()` 获取 Compose Multiplatform 提供的 UIViewController。

shared 模块编译为静态 iOS Framework（isStatic = true），通过 xcodegen 生成的 Xcode 工程在 pre-build script 中调用 `./gradlew :shared:embedAndSignAppleFrameworkForXcode` 自动编译 Kotlin/Native 代码。Info.plist 中必须包含 `CADisableMinimumFrameDurationOnPhone = true`，否则 Compose Multiplatform 在初始化时会抛出 IllegalStateException。Feature 3 新增的 `NSLocationWhenInUseUsageDescription` 权限说明为将来接入 CLLocationManager 做准备。

PlatformCapabilities 的 iOS actual 实现直接使用 Kotlin/Native 的 ObjC interop 调用 Foundation 和 UIKit API。UIImpactFeedbackGenerator 提供触觉反馈，UIPasteboard.generalPasteboard 提供剪贴板读写，UIDevice.currentDevice 提供设备型号和系统版本。这些 API 在 Kotlin/Native 中的调用方式与 Swift 几乎一致，体现了 KMP 对 iOS 原生 API 的良好互操作性。

### 测试工具与步骤

#### Android 端测试

Android 端的测试使用 Android Studio 自带的模拟器完成。具体步骤如下：

第一步，通过 `./gradlew :androidApp:assembleDebug` 构建 Debug APK，构建产物位于 `androidApp/build/outputs/apk/debug/androidApp-debug.apk`。

第二步，使用 Android Debug Bridge（adb）将 APK 安装到 Pixel 7 模拟器：`adb install androidApp-debug.apk`。

第三步，通过 adb 启动应用：`adb shell am start -n com.example.kmpapp/.MainActivity`，验证仪表盘首页正常渲染（问候语、天气卡片、统计数据、快捷操作按钮、最近笔记列表），点击快捷操作按钮可跳转到笔记列表或编辑页，点击调色板图标进入主题设置页并可切换主题。

第四步，验证富媒体附件功能：进入笔记编辑页，点击附件选择栏的各个按钮（位置、设备信息、清单、链接），验证各类型附件卡片正确渲染，清单的 Checkbox 可交互勾选，链接的复制按钮可调用剪贴板。

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

#### 编译验证

三个新功能均通过双平台编译验证：

```bash
# Android（Kotlin JVM 编译）
DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer ./gradlew :shared:compileDebugKotlinAndroid

# iOS（Kotlin/Native 编译）
DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer ./gradlew :shared:compileKotlinIosSimulatorArm64

# 完整 Android APK 构建
DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer ./gradlew :androidApp:assembleDebug
```

#### 开发环境注意事项

构建环境中 `xcode-select` 指向 `/Library/Developer/CommandLineTools` 而非 Xcode.app，导致 `xcrun simctl` 命令无法找到模拟器设备。解决方案是在所有 simctl 和 xcodebuild 命令前添加环境变量 `DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer`，永久修复需要 `sudo xcode-select -s /Applications/Xcode.app/Contents/Developer`。

Gradle 8.11.1 的下载在某些网络环境下会超时，可以通过腾讯镜像 `mirrors.cloud.tencent.com/gradle/` 手动下载并配置 Gradle wrapper 缓存。

### 三个功能如何共同展示 KMP 全貌

三个功能分别从不同维度展示了 KMP 的核心优势，组合起来构成了完整的跨平台开发能力展示：

```
                    ┌─────────────────────────────┐
                    │   Compose Multiplatform UI   │
                    │   (100% shared, dynamic)     │
                    ├──────────┬──────────────────┤
    功能二            │          │                    │
  远程主题引擎 ──→ │  共享业务  │   功能一           │
  (动态配色/字体)   │  逻辑层    │   智能首页         │
                    │  (ViewModel│   (配置驱动布局)   │
                    │   + Repo)  │                    │
                    ├──────────┴──────────────────┤
                    │   expect/actual 平台抽象层    │
                    ├──────────┬──────────────────┤
    功能三            │          │                    │
  平台感知笔记 ──→ │  Android   │      iOS           │
  (相机/麦克风/GPS) │  actual    │      actual        │
                    └──────────┴──────────────────┘
```

功能一（智能首页）展示了**业务逻辑高度复用**——JSON 解析、布局引擎、数据加载全部在 commonMain，Android 和 iOS 零行平台代码即可获得完全相同的动态化能力。功能二（远程主题引擎）展示了**Compose Multiplatform 的动态 UI 能力**——同一套 Material3 主题配置代码在两端渲染效果一致，切换主题时 StateFlow 驱动 recompose 自动刷新。功能三（平台感知笔记）展示了**expect/actual 的深度平台集成**——设备信息、触觉反馈、剪贴板、定位等原生能力被干净封装，上层 ViewModel 和 UI 完全不感知平台差异。
