#!/bin/bash
# ═══════════════════════════════════════════════════════════════
#  KMP Notes App — iOS 项目设置脚本
# ═══════════════════════════════════════════════════════════════
#
#  此脚本用于生成 iOS 的 Xcode 项目文件。
#
#  前提条件：
#    1. 已安装 Xcode（macOS）
#    2. 已安装 xcodegen：brew install xcodegen
#       （或者你也可以直接在 Xcode 中新建项目，把 Swift 文件拖进去）
#
#  使用方法：
#    cd iosApp
#    chmod +x setup_ios.sh
#    ./setup_ios.sh
#
# ═══════════════════════════════════════════════════════════════

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

echo ""
echo "╔══════════════════════════════════════════════╗"
echo "║   KMP Notes — iOS 项目生成器                ║"
echo "╚══════════════════════════════════════════════╝"
echo ""

# 检查 xcodegen
if ! command -v xcodegen &> /dev/null; then
    echo "⚠️  未检测到 xcodegen"
    echo ""
    echo "请选择安装方式："
    echo "  方式 1: brew install xcodegen  (推荐)"
    echo "  方式 2: 手动在 Xcode 中创建项目"
    echo ""
    echo "如果选择方式 2，请按以下步骤操作："
    echo "  1. 打开 Xcode → File → New → Project"
    echo "  2. 选择 iOS → App"
    echo "  3. Product Name: iosApp"
    echo "  4. Interface: SwiftUI"
    echo "  5. Language: Swift"
    echo "  6. Bundle Identifier: com.example.kmpapp"
    echo "  7. 将生成的项目保存到 iosApp/ 目录"
    echo "  8. 替换 iOSApp.swift 和 ContentView.swift"
    echo "  9. 添加 Build Phase 脚本编译 Kotlin 框架"
    echo ""
    echo "安装 xcodegen 后重新运行此脚本即可自动完成以上步骤。"
    exit 1
fi

echo "✅ 检测到 xcodegen，开始生成项目..."
cd "$SCRIPT_DIR"
xcodegen generate --spec project.yml

echo ""
echo "✅ Xcode 项目已生成: iosApp.xcodeproj"
echo ""
echo "接下来的步骤："
echo "  1. 在项目根目录运行: ./gradlew :shared:linkDebugFrameworkIosSimulatorArm64"
echo "  2. 打开 iosApp.xcodeproj"
echo "  3. 选择模拟器 → Run (⌘R)"
echo ""
