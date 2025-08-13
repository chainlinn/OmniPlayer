<<<<<<< HEAD
# OmniPlayer

A multiplatform video player kernel library compatible with multiple platforms.


---

## 📜 Licensing

AuraPlayer is available under a dual-license model, designed to meet the needs of different users.

### Open Source License (AGPL v3.0)

For open source projects, personal use, and academic research, AuraPlayer is licensed under the **GNU Affero General Public License v3.0 (AGPL-3.0)**. We are committed to supporting the open source community. Under this license, you are free to use, modify, and distribute the software, but you must also **open source your own application's full source code** under the same AGPL-3.0 license.

You can find the full license text in the [LICENSE](LICENSE) file.

### Commercial License

If you wish to use AuraPlayer in a proprietary, closed-source commercial application, you must purchase a **Commercial License**. The commercial license grants you the right to use AuraPlayer without the copyleft restrictions of the AGPL-3.0, and includes dedicated support.

**To inquire about a commercial license, please contact us at [your-email@example.com] or visit our website at [your-website.com/pricing].**
=======
好的，这是一个为 OmniPlayer 项目精心编写的 README.md 文件。它包含了项目介绍、架构、特性、如何安装和使用等关键部分，旨在清晰、专业地展示项目。

---

# OmniPlayer - 您的全能 KMP 媒体播放器

[![Kotlin Version](https://img.shields.io/badge/Kotlin-1.9.22-blue.svg?style=for-the-badge&logo=kotlin)](https://kotlinlang.org)
[![License](https://img.shields.io/badge/License-Apache%202.0-orange.svg?style=for-the-badge)](https://www.apache.org/licenses/LICENSE-2.0)
[![Build Status](https://img.shields.io/github/actions/workflow/status/your-username/omniplayer/build.yml?branch=main&style=for-the-badge&logo=githubactions)](https://github.com/your-username/omniplayer/actions)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.your-username/omniplayer-core?style=for-the-badge)](https://search.maven.org/artifact/io.github.your-username/omniplayer-core)

**OmniPlayer** 是一个为 [Kotlin Multiplatform (KMP)](https://kotlinlang.org/docs/multiplatform-mobile-getting-started.html) 设计的强大、灵活的媒体播放器依赖库。它的核心目标是提供一套统一、简洁的 API，让开发者可以在不同平台（iOS, Android, macOS, Windows, Linux）上轻松实现音视频播放功能，而无需关心底层的具体实现。

[English](./README.en.md) | **中文**

---

## ✨ 特性

- ♊️ **KMP 核心**：一次编码，多端运行。在 `commonMain` 中编写你的播放逻辑。
- 🧩 **可插拔后端**：根据需求自由选择播放器后端。
  - **🚀 MPV 后端**：基于强大的 [libmpv](https://mpv.io/manual/master/#embedding-with-libmpv)，支持广泛的媒体格式和高级功能（如字幕、音轨切换、硬件解码等）。
  - **🌿 原生后端**：利用各平台自带的原生播放器（iOS/macOS 的 `AVPlayer`，Android 的 `ExoPlayer`），实现最佳的系统集成和最低的包体积增量。
- 統一 **API 设计**：无论使用哪个后端，都通过同一套 API (`Player`, `MediaSource`, `PlayerState` 等) 进行控制，无缝切换。
- 📺 **UI 无关**：核心逻辑与 UI 分离，可轻松集成到 [Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform)、SwiftUI、XML 等任何 UI 框架中。
- 🪶 **轻量级**：核心模块非常小巧，你只需引入你需要的后端依赖。

## 🏛️ 架构设计

OmniPlayer 遵循典型的 KMP 架构，将共享逻辑与平台特定实现分离。

```
          +--------------------------------+
          |         Your KMP App           |
          | (Compose, SwiftUI, etc.)       |
          +--------------------------------+
                      | depends on
          +--------------------------------+
          |       omniplayer-core          |  <-- 核心模块 (commonMain)
          | (Player, MediaSource, etc.)    |  - 定义接口和通用逻辑
          +--------------------------------+
                      |
      +---------------------+---------------------+
      | depends on          | depends on          |
+-----------------+   +-----------------+   +-----------------+
| omniplayer-mpv  |   | omniplayer-native |   |    (更多后端...)  |
+-----------------+   +-----------------+   +-----------------+
| - iOS (libmpv)  |   | - iOS (AVPlayer)  |
| - Android (mpv) |   | - Android(ExoPlayer)|
| - Desktop (mpv) |   | - macOS (AVPlayer)  |
+-----------------+   +-----------------+
```

- **`omniplayer-core`**: 定义了所有播放器共享的接口（如 `Player`）、数据类（如 `MediaSource`）和状态（如 `PlayerState`）。这是纯 Kotlin 模块，是所有实现的基础。
- **`omniplayer-mpv`**: `Player` 接口的 MPV 实现。它通过 C-Interop 与 `libmpv` 交互，在各个平台上提供一致的高级播放能力。
- **`omniplayer-native`**: `Player` 接口的原生实现。它在 `actual` 代码中调用每个平台的推荐播放器（`AVPlayer`, `ExoPlayer`），以获得最佳的性能和系统兼容性。

##  platforms 支持平台

| 平台 | 原生后端 (omniplayer-native) | MPV 后端 (omniplayer-mpv) |
| :--- | :---: | :---: |
| **Android** | ✅ (ExoPlayer) | ✅ |
| **iOS** | ✅ (AVPlayer) | ✅ |
| **macOS (JVM/Native)** | ✅ (AVPlayer) | ✅ |
| **Windows (JVM)** | ⏳ 待定 | ✅ |
| **Linux (JVM)** | ⏳ 待定 | ✅ |

> **Note**: 桌面端的原生播放器支持仍在探索中，因为在 JVM 上统一封装原生播放器较为复杂。目前推荐在桌面端使用功能更强大的 MPV 后端。

## 🚀 快速开始

### 1. 添加依赖

在你的 KMP 项目的 `build.gradle.kts` 的 `commonMain` `sourceSets` 中添加依赖。

首先，请确保你的根 `build.gradle.kts` 或 `settings.gradle.kts` 中已添加 `mavenCentral()`。

```kotlin
// settings.gradle.kts
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
```

然后，在模块的 `build.gradle.kts` 中添加依赖：

```kotlin
// build.gradle.kts of your shared module
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                // 核心库是必须的
                implementation("io.github.your-username:omniplayer-core:1.0.0")

                // --- 根据你的需求，选择一个后端实现 ---

                // 选项 A: 使用 MPV 后端 (推荐用于桌面和高级功能需求)
                implementation("io.github.your-username:omniplayer-mpv:1.0.0")

                // 选项 B: 使用原生后端 (推荐用于移动端，以减小包体积)
                // implementation("io.github.your-username:omniplayer-native:1.0.0")
            }
        }
    }
}
```
> **重要**: 请将 `io.github.your-username` 和 `1.0.0` 替换为实际的 Group ID 和最新版本。

### 2. 创建播放器实例

我们提供一个简单的工厂方法来创建播放器实例。你可以在 `commonMain` 中调用它。

```kotlin
// 在你的 ViewModel 或业务逻辑层 (commonMain)
import io.github.your_username.omniplayer.core.Player
import io.github.your_username.omniplayer.factory.PlayerFactory // 假设工厂在这个包下

// PlayerFactory 会根据你引入的依赖自动选择合适的实现
val player: Player = PlayerFactory.create()
```

### 3. 使用播放器 (Compose Multiplatform 示例)

下面是一个在 Compose Multiplatform 中使用的简单示例：

```kotlin
// commonMain
import androidx.compose.runtime.*
import io.github.your_username.omniplayer.core.MediaSource
import io.github.your_username.omniplayer.ui.VideoPlayerView // 假设提供了一个UI组件

@Composable
fun MyPlayerScreen() {
    // 1. 创建并记住播放器实例
    val player = remember { PlayerFactory.create() }

    // 2. 监听播放器状态
    val playerState by player.state.collectAsState()

    LaunchedEffect(Unit) {
        // 3. 设置媒体源并准备播放
        val mediaSource = MediaSource.Url("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4")
        player.setMedia(mediaSource)
        player.prepare()
    }

    // 在组件销毁时释放播放器资源
    DisposableEffect(Unit) {
        onDispose {
            player.release()
        }
    }

    Column {
        // 4. 将播放器画面渲染到UI上
        // VideoPlayerView 是一个 expect/actual 的 Composable，用于桥接原生视图
        VideoPlayerView(
            modifier = Modifier.fillMaxWidth().aspectRatio(16 / 9f),
            player = player
        )

        // 5. 添加播放控制按钮
        Row(horizontalArrangement = Arrangement.Center) {
            Button(onClick = { player.play() }) {
                Text("Play")
            }
            Button(onClick = { player.pause() }) {
                Text("Pause")
            }
            Text("Status: ${playerState.playbackState}")
        }
    }
}
```

## ⚙️ 配置

### MPV 后端

使用 MPV 后端时，你可能需要在你的应用中打包 `libmpv` 的动态链接库。具体的步骤取决于目标平台。我们将在未来的文档中提供详细的打包指南。

### 原生后端 (Android)

使用 `omniplayer-native` 时，请确保在 `AndroidManifest.xml` 中添加网络权限：

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

## 🤝 贡献

我们非常欢迎社区的贡献！无论是提交 Bug 报告、功能需求还是代码 PR，都对项目有巨大帮助。

1.  **Fork** 本仓库。
2.  创建你的功能分支 (`git checkout -b feature/AmazingFeature`)。
3.  提交你的更改 (`git commit -m 'Add some AmazingFeature'`)。
4.  推送到分支 (`git push origin feature/AmazingFeature`)。
5.  打开一个 **Pull Request**。

请在 [Issues](https://github.com/your-username/omniplayer/issues) 页面查看待办事项或报告问题。

## 📜 许可证

本项目基于 **Apache 2.0 License**。详情请见 [LICENSE](LICENSE) 文件。
>>>>>>> origin/main
