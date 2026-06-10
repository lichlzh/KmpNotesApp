## KMP Notes App — 动态化功能设计方案

### 设计目标

基于现有 KMP Notes 项目，设计三个功能来展示 KMP 的核心优势：**跨平台代码共享 + 动态化能力**。所谓"动态化"，指的是应用能够在不发版的情况下，通过配置或远程数据改变界面内容、样式和行为。

每个功能都精准对应一个 KMP 优势：

- **功能一 → 业务逻辑高度复用**：一套解析/渲染逻辑，Android 和 iOS 行为完全一致
- **功能二 → Compose Multiplatform 动态 UI**：共享 UI 框架响应式渲染动态内容
- **功能三 → expect/actual 平台抽象**：平台原生能力被干净地封装，上层逻辑无感知

---

### 功能一：服务端驱动的智能首页（Server-Driven Dashboard）

**一句话**：将笔记列表页改造为可配置的 Dashboard，首页展示哪些卡片、用什么布局，全部由一份 JSON 配置决定。

**展示优势**：业务逻辑 100% 共享——JSON 解析、布局引擎、数据加载全部在 commonMain，Android 和 iOS 零行平台代码即可获得完全相同的动态化能力。

#### 设计方案

**配置协议**（服务端下发，也可本地缓存）：

```json
{
  "version": 1,
  "layout": "staggered_grid",
  "columns": 2,
  "sections": [
    {
      "id": "weather_overview",
      "type": "weather",
      "title": "今日天气",
      "size": "full_width",
      "config": { "defaultCity": "Beijing" }
    },
    {
      "id": "recent_notes",
      "type": "note_list",
      "title": "最近笔记",
      "size": "half_width",
      "config": { "limit": 6, "sortBy": "updatedAt" }
    },
    {
      "id": "quick_actions",
      "type": "actions",
      "title": "快捷操作",
      "size": "full_width",
      "config": {
        "items": [
          { "icon": "add", "label": "新建笔记", "action": "create_note" },
          { "icon": "search", "label": "搜索", "action": "open_search" },
          { "icon": "palette", "label": "切换主题", "action": "toggle_theme" }
        ]
      }
    },
    {
      "id": "pinned_notes",
      "type": "note_list",
      "title": "置顶笔记",
      "size": "half_width",
      "config": { "filter": "pinned", "limit": 4 }
    }
  ]
}
```

**卡片类型**（可扩展，无需修改渲染引擎）：

| 类型 | 描述 | 数据来源 |
|---|---|---|
| `weather` | 天气卡片，复用现有 WeatherService | Open-Meteo API |
| `note_list` | 笔记列表（支持置顶、最近、颜色筛选等） | NoteRepository |
| `actions` | 快捷操作按钮栏 | 配置中的 items 数组 |
| `stats` | 笔记统计（总数、本周新建、置顶数） | NoteRepository 计算 |
| `quote` | 每日一言（名言/诗句） | 远程 API 或配置内嵌 |

**架构**：

```
DashboardConfig.kt          ← 数据模型（commonMain）
DashboardService.kt         ← 加载/缓存配置（commonMain）
DashboardViewModel.kt       ← 状态管理（commonMain）
DashboardScreen.kt          ← Compose UI 渲染引擎（commonMain）
  ├── WeatherCard.kt         ← 已有组件
  ├── NoteListCard.kt        ← 新增
  ├── ActionsCard.kt         ← 新增
  ├── StatsCard.kt           ← 新增
  └── QuoteCard.kt           ← 新增
```

所有新增代码都在 `commonMain`，无需任何 expect/actual 声明。

**动态化体现**：
- 服务端改配置 → 首页布局立即变化（增减卡片、改排列、改列数）
- 新增卡片类型只需：① 定义 type 字符串 ② 添加 Composable 渲染函数
- 旧版本 app 遇到未知 type 时优雅降级（显示占位卡片）

---

### 功能二：远程主题引擎（Dynamic Theme Engine）

**一句话**：主题配置是一份 JSON，可以从服务器加载、本地导入、或扫码分享。切换主题时，整个 app 的颜色、字体、圆角、阴影全部动态变化。

**展示优势**：Compose Multiplatform 的声明式 UI 让动态主题在 Android 和 iOS 上表现完全一致——同一套 MaterialTheme 配置代码，零平台适配。

#### 设计方案

**主题协议**：

```json
{
  "name": "深海蓝",
  "id": "ocean_blue",
  "version": 1,
  "colors": {
    "primary": "#1565C0",
    "onPrimary": "#FFFFFF",
    "primaryContainer": "#D1E4FF",
    "secondary": "#546E7A",
    "background": "#FAFAFA",
    "surface": "#FFFFFF",
    "surfaceVariant": "#E3E8ED"
  },
  "shapes": {
    "cornerRadius": 16,
    "cardElevation": 2
  },
  "typography": {
    "titleScale": 1.0,
    "bodyLineHeight": 1.5
  }
}
```

**内置主题**（至少 4 套，可热切换）：

| 主题名 | 风格 | 说明 |
|---|---|---|
| 暖阳橙（默认） | 明亮温暖 | 当前项目的 orange 配色 |
| 深海蓝 | 沉稳专业 | 蓝色系 Material3 |
| 森林绿 | 自然清新 | 绿色系 Material3 |
| 暗夜紫 | 暗黑模式 | 深色主题，紫色点缀 |

**主题分享方式**：
- 导出为 JSON 文本，可复制到剪贴板
- 生成包含 JSON 的二维码（用 expect/actual 调用平台二维码生成）
- 从剪贴板导入主题 JSON

**架构**：

```
ThemeConfig.kt              ← JSON → data class（commonMain）
ThemeEngine.kt              ← 主题管理/缓存/切换（commonMain）
DynamicAppTheme.kt          ← 替代原 Theme.kt（commonMain）
ThemeSettingsScreen.kt      ← 主题选择/预览页面（commonMain）
ThemeCard.kt                ← 主题预览卡片组件（commonMain）
```

**动态化体现**：
- 切换主题 = 替换一个 JSON → Compose 自动 recompose 整个 UI 树
- 新主题从服务端下发 → 用户在设置页面一键应用
- 主题 JSON schema 可演进，新增字段（如渐变、动画配置）不需要改渲染代码

---

### 功能三：平台感知笔记（Platform-Aware Rich Notes）

**一句话**：给笔记添加"富媒体附件"能力——录音、拍照、位置信息。每种能力用 expect/actual 抽象，上层 UI 和业务逻辑完全共享。

**展示优势**：expect/actual 机制让平台原生能力（相机、麦克风、GPS）被封装成统一的接口，ViewModel 和 Compose UI 完全不感知自己在 Android 还是 iOS 上运行。

#### 设计方案

**附件类型**：

| 附件类型 | Android actual | iOS actual | 共享层处理 |
|---|---|---|---|
| 语音备忘 | MediaRecorder | AVAudioRecorder | 播放控制 UI、时长显示、波形可视化 |
| 拍照/图片 | CameraX / Intent | UIImagePickerController | 缩略图展示、图片压缩、列表显示 |
| 位置标记 | FusedLocationProvider | CLLocationManager | 地址解析、地图卡片展示 |

**数据模型**：

```kotlin
// commonMain
sealed class NoteAttachment {
    data class Audio(val fileName: String, val durationMs: Long) : NoteAttachment()
    data class Image(val fileName: String, val width: Int, val height: Int) : NoteAttachment()
    data class Location(val latitude: Double, val longitude: Double, val address: String?) : NoteAttachment()
}

data class Note(
    // ... 现有字段
    val attachments: List<NoteAttachment> = emptyList()
)
```

**expect/actual 声明**：

```kotlin
// commonMain - Platform.kt 新增
expect class PlatformMediaManager() {
    fun startRecording(onComplete: (String, Long) -> Unit)
    fun stopRecording()
    fun pickImage(onResult: (String?) -> Unit)
    fun getCurrentLocation(onResult: (Double, Double, String?) -> Unit)
    fun getFileBytes(fileName: String): ByteArray?
}
```

```kotlin
// androidMain - Platform.android.kt
actual class PlatformMediaManager {
    actual fun startRecording(onComplete: (String, Long) -> Unit) {
        // MediaRecorder API
    }
    // ...
}
```

```kotlin
// iosMain - Platform.ios.kt
actual class PlatformMediaManager {
    actual fun startRecording(onComplete: (String, Long) -> Unit) {
        // AVAudioRecorder
    }
    // ...
}
```

**架构**：

```
NoteAttachment.kt            ← 附件数据模型（commonMain）
AttachmentViewModel.kt       ← 附件管理逻辑（commonMain）
AttachmentPickerBar.kt       ← 附件选择工具栏 UI（commonMain）
AudioPlayerCard.kt           ← 语音播放卡片（commonMain）
ImagePreviewCard.kt          ← 图片预览卡片（commonMain）
LocationCard.kt              ← 位置信息卡片（commonMain）

PlatformMediaManager.kt      ← expect 声明（commonMain）
Platform.android.kt          ← actual：MediaRecorder + CameraX + FusedLocation
Platform.ios.kt              ← actual：AVAudioRecorder + UIImagePicker + CLLocationManager
```

**动态化体现**：
- 附件组合完全动态——一条笔记可以同时包含语音 + 图片 + 位置
- 新增附件类型只需：① 定义 sealed class 子类 ② 实现 expect/actual ③ 添加 Composable 渲染
- 笔记编辑器根据平台能力动态显示可用的附件按钮（如设备无摄像头则隐藏拍照）

---

### 推荐实施顺序

**第一步：功能二（远程主题引擎）**— 改动最小，见效最快

- 原因：只需改造 Theme.kt + 新增 2 个文件，不涉及数据模型变更
- 效果：立即可见——4 套主题一键切换，视觉冲击力强
- 预估工作量：3-4 小时

**第二步：功能一（智能首页）**— 架构升级，展示复用

- 原因：在现有笔记功能基础上新增 Dashboard 层，不改现有代码
- 效果：配置驱动的动态布局，展示"一份代码驱动两端首页"
- 预估工作量：4-5 小时

**第三步：功能三（平台感知笔记）**— 深度集成，展示原生能力

- 原因：涉及真实平台 API 调用，是 expect/actual 机制的完整演示
- 效果：语音、拍照、定位，每个功能都是"一份业务逻辑 + 两套平台实现"
- 预估工作量：1-2 天

---

### 三个功能如何共同展示 KMP 全貌

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

三个功能分别从 UI 层、逻辑层、平台层展示了 KMP 的动态化能力，组合起来就是一个完整的"KMP 为什么适合现代跨平台开发"的答案。
