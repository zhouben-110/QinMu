[README.md](https://github.com/user-attachments/files/30453589/README.md)
# 沁目 (QinMu) — 智能科学护眼与沉浸放松音频 App

[![Version](https://img.shields.io/badge/Version-v2.0.2-2368A4.svg)](https://github.com/zhouben-110/QinMu)
[![Platform](https://img.shields.io/badge/Platform-Android-34C759.svg)](https://developer.android.com/)
[![Language](https://img.shields.io/badge/Language-Kotlin-7F52FF.svg)](https://kotlinlang.org/)
[![UI Framework](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4.svg)](https://developer.android.com/jetpack/compose)
[![Style](https://img.shields.io/badge/Style-Soft%20Neumorphism-B1D6EA.svg)](#-视觉与美学设计-design-system)

**沁目 (QinMu)** 是一款基于 **Kotlin** 与 **Jetpack Compose** 打造的高颜值、现代化的智能科学护眼与放松音频 Android 应用。项目采用全新的 **Soft Neumorphism Light Blue（柔和新拟物天空蓝）** 视觉美学，结合实体感黑胶唱片倒计时、悬浮胶囊交互控件以及高效的后台用眼健康管理功能，为用户提供沉浸式的护眼与理疗放松体验。

---

## 🌟 核心功能亮点

- 🎨 **柔和新拟物化 UI 视觉 (Soft Neumorphism Light Blue)**
  - 突破传统扁平化设计，采用轻盈的天空蓝渐变与 Tactile 3D 浮雕光影效果。
  - 双重阴影（左上高光 + 右下柔和阴影）构筑极具触感的物理材质界面。
- 🎵 **黑胶唱片护眼计时器 (Vinyl Progress Gauge)**
  - 拟真旋转黑胶唱片搭配珊瑚红（`#FF3B30`）环形进度弧线，实时显示用眼计时与倒计时状态。
- 💊 **悬浮胶囊控制与导航 (Floating Capsule & Pill Bar)**
  - 侧边悬浮胶囊快捷控制栏（播放/暂停、重置、跳过、收藏）。
  - 底部 Neumorphic Pill 悬浮导航栏，支持 3D 顺滑切屏动画。
- 👁️ **智能用眼提醒与后台持久化**
  - 支持连屏用眼计时、休息状态后台持久化存储，应用重启/切换后台不丢失秒数。
  - 智能游戏/会议免打扰识别防抖，保障使用过程顺畅不被打扰。
- 📖 **科学护眼看板与放松音乐**
  - 包含精选助眠放松音乐播放列表及科学护眼知识卡片。
  - 极致功耗优化与横屏自适应双列布局。

---

## 🎨 视觉与美学设计 (Design System)

### 色彩规范 (Color Tokens)

| 色彩类型 | 色值 | 视觉说明 |
| :--- | :--- | :--- |
| **Sky Gradient Start** | `#B1D6EA` | 天空蓝渐变起点 |
| **Sky Gradient End** | `#D5EAF5` | 天空蓝渐变终点 |
| **Neumorphic Surface** | `#E1F0F7` | 3D 新拟物基础容器表面色 |
| **Text Primary** | `#1A365D` | 高对比度深蓝黑字体 |
| **Accent Royal Blue** | `#2368A4` | 皇家蓝品牌强调色 |
| **Accent Coral Red** | `#FF3B30` | 珊瑚红倒计时进度弧线 |

### 自定义拟物组件 (UI Architecture)
- `Modifier.neumorphicShadow(...)`: 绘制双光源高光与柔和阴影。
- `NeumorphicCard`: 3D 浮雕微渐变边框容器。
- `NeumorphicPillButton`: 胶囊状圆角交互按钮。
- `NeumorphicIconButton`: 圆形拟物按压反馈按钮。

---

## 📁 项目结构 (Project Structure)

```text
沁目/
├── app/
│   ├── build.gradle.kts           # App 模块 Gradle 构建配置
│   └── src/main/java/com/qinmu/eyecare/
│       ├── QinMuApplication.kt     # 全局 Application 入口
│       ├── data/                   # 数据层 (Repository, Models, Preferences)
│       ├── service/                # 后台护眼服务与用眼计时器
│       ├── ui/                     # Jetpack Compose UI 视图与新拟物组件
│       └── util/                   # 系统工具类 (通知、自启动、权限)
├── design.md                      # 项目 UI/UX 设计规范文档
├── version.json                   # 应用版本信息与更新配置 (v2.0.2)
├── build.gradle.kts               # 顶层 Gradle 脚本
└── settings.gradle.kts            # Gradle 设置
```

---

## 🛠️ 技术栈与依赖 (Tech Stack)

- **语言**: Kotlin 1.9+
- **构建系统**: Gradle (KTS Kotlin DSL)
- **UI 框架**: Jetpack Compose (Material 3 + Custom Neumorphism Canvas & Modifiers)
- **并发处理**: Kotlin Coroutines & Flow
- **架构模式**: MVVM / Clean Architecture
- **最低支持**: Android 7.0 (API Level 24)

---

## 🚀 构建与运行 (Getting Started)

### 前置要求
- Android Studio Ladybug / Jellyfish 或更高版本
- JDK 17
- Android SDK 34

### 编译运行步骤

1. **克隆项目到本地**:
   ```bash
   git clone https://github.com/zhouben-110/QinMu.git
   cd QinMu
   ```

2. **使用 Gradle 编译项目**:
   ```bash
   ./gradlew assembleDebug
   ```

3. **安装到连接的设备**:
   ```bash
   ./gradlew installDebug
   ```

---

## 📝 更新日志 (v2.0.2)

- ⚡ **后台持久化**: 新增连屏用眼计时与休息状态后台存储，重启不丢秒。
- 🛡️ **智能防抖**: 优化自动游戏/会议免打扰识别防抖逻辑。
- 📱 **横屏适配**: 新增横屏模式全屏遮罩自适应双列布局。
- 📊 **健康看板**: 新增科学护眼知识卡片与极致功耗优化。
- ✨ **动效提升**: 导航面板新增 3D 顺滑切屏动画，支持各厂商自启动配置。

---

## 📄 开源许可 (License)

本项目采用 [MIT License](LICENSE) 许可证。
