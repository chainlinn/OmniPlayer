package io.github.zhangmanyue774.omniplayer.kernel

import io.github.zhangmanyue774.omniplayer.kernel.engine.IPlayerEngine

/**
 * 修正：使用 expect interface 作为平台无关的上下文标记。
 * 这避免了类模态（final vs abstract）的冲突问题。
 */

enum class EngineType {
    NATIVE_PLAYER, MPV_PLAYER ,VLC_PLAYER
}
expect interface PlatformContext


/**
 * 播放器引擎配置的密封接口。
 * 这是所有特定引擎配置的父类。
 * 通过密封接口，我们可以确保在 `when` 语句中处理所有已知的配置类型。
 */
sealed interface PlayerEngineConfig {
    val engineType: EngineType
}

/**
 * 原生播放器（ExoPlayer on Android, AVPlayer on iOS）的配置。
 * @param useSecureDecoding 是否启用安全解码路径。
 * @param customHeaders 用于网络请求的自定义头信息。
 */
data class NativePlayerConfig(
    val useSecureDecoding: Boolean = false,
    val customHeaders: Map<String, String>? = null
) : PlayerEngineConfig {
    override val engineType: EngineType = EngineType.NATIVE_PLAYER
}

/**
 * MPV 播放器的配置。
 * @param configDirectory MPV 配置文件（mpv.conf）所在的目录。
 * @param cacheDirectory 播放器缓存目录。
 * @param hardwareDecodingProfile 硬解配置，例如 "auto-copy"。
 */
data class MPVPlayerConfig(
    val configDirectory: String ?= null,
    val cacheDirectory: String ?= null,
    val hardwareDecodingProfile: String = "auto"
) : PlayerEngineConfig {
    override val engineType: EngineType = EngineType.MPV_PLAYER
}

/**
 * VLC 播放器的配置。
 * @param options VLC 启动时传递的命令行参数列表。
 * @param networkCaching 网络缓存时间（毫秒）。
 */
data class VLCPlayerConfig(
    val options: List<String> = emptyList(),
    val networkCaching: Int = 3000
) : PlayerEngineConfig {
    override val engineType: EngineType = EngineType.VLC_PLAYER
}

/**
 * 平台默认路径提供者。
 * 这是一个 expect object，每个平台都需要提供其具体实现。
 */
expect object PlatformDefaults {
    /**
     * 获取应用专属的、安全的缓存目录根路径。
     * @param context 平台上下文，用于访问系统 API。
     * @return 缓存目录的绝对路径字符串。
     */
    fun getAppCacheDirectory(context: PlatformContext): String
}


/**
 * 通用的播放器引擎工厂函数 expect 声明。
 *
 * 它根据传入的 `PlayerEngineConfig`的具体类型来创建并返回相应的播放器实例。
 *
 * @param context 平台特定的上下文。
 * @param config 包含了创建引擎所需全部信息的配置对象。这是一个密封接口的实例。
 * @return 一个实现了 IPlayerEngine 接口的播放器实例。
 */
expect fun createPlayerEngine(
    context: PlatformContext,
    config: PlayerEngineConfig
): IPlayerEngine


///**
// * 播放器引擎配置的密封接口。
// * 它是所有特定引擎配置的父接口。
// * @param T 引擎配置所产生的数据的泛型类型，用于携带具体配置信息。
// */
//interface PlayerEngineConfig<T> {
//    val engineType : EngineType
//    val data: T
//}
//
///**
// * 通用的播放器引擎工厂函数 `expect` 声明。
// *
// * @param C 具体的配置类型，必须是 PlayerEngineConfig 的子类。
// * @param T 配置所携带的数据类型。
// * @param context 平台特定的上下文。
// * @param config 包含了创建引擎所需全部信息的配置对象。
// * @return 一个实现了 IPlayerEngine 接口的播放器实例。
// */
//expect fun <T, C : PlayerEngineConfig<T>> createPlayerEngine(
//    context: PlatformContext,
//    config: C
//): IPlayerEngine
