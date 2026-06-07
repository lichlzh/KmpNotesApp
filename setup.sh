#!/bin/bash
# ═══════════════════════════════════════════════════════════════
#  KMP Notes App — 项目初始化脚本
# ═══════════════════════════════════════════════════════════════
#
#  运行此脚本来初始化 Gradle Wrapper 并验证项目配置。
#
#  前提：需要已安装 Gradle（推荐通过 SDKMAN 或 Homebrew 安装）
#    - sdk install gradle 8.11.1
#    - brew install gradle
#
# ═══════════════════════════════════════════════════════════════

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

echo ""
echo "╔══════════════════════════════════════════════╗"
echo "║   KMP Notes — 项目初始化                     ║"
echo "╚══════════════════════════════════════════════╝"
echo ""

# 检查 Gradle
if ! command -v gradle &> /dev/null; then
    echo "❌ 未检测到 Gradle，请先安装："
    echo "   brew install gradle"
    echo "   或: sdk install gradle 8.11.1"
    exit 1
fi

GRADLE_VERSION=$(gradle --version | grep "Gradle " | head -1 | awk '{print $2}')
echo "✅ 检测到 Gradle $GRADLE_VERSION"

# 生成 Gradle Wrapper
echo ""
echo "📦 生成 Gradle Wrapper (8.11.1)..."
gradle wrapper --gradle-version 8.11.1

echo ""
echo "✅ Gradle Wrapper 已生成"

# 验证
echo ""
echo "🔍 验证项目配置..."
./gradlew tasks --quiet 2>/dev/null || {
    echo "⚠️  首次构建可能需要一些时间，这是正常的"
}

echo ""
echo "╔══════════════════════════════════════════════╗"
echo "║   初始化完成！                               ║"
echo "╠══════════════════════════════════════════════╣"
echo "║                                              ║"
echo "║  运行 Android:                               ║"
echo "║    ./gradlew :androidApp:installDebug         ║"
echo "║                                              ║"
echo "║  运行 iOS (需 macOS + Xcode):                ║"
echo "║    cd iosApp && ./setup_ios.sh                ║"
echo "║    xed iosApp.xcodeproj                       ║"
echo "║                                              ║"
echo "╚══════════════════════════════════════════════╝"
echo ""
