package io.github.zhangmanyue774.omniplayer.kernel

import android.content.Context
import android.util.Log
import io.github.zhangmanyue774.omniplayer.kernel.engine.ExoPlayerEngine
import io.github.zhangmanyue774.omniplayer.kernel.engine.IPlayerEngine
import io.github.zhangmanyue774.omniplayer.kernel.engine.MPVPlayerEngine
import `is`.xyz.mpv.MPVLib
import `is`.xyz.mpv.testMpvLibCoreFunctionality
import java.io.File


/**
 * 1. 创建一个 Android 平台的具体上下文类，它实现了 commonMain 的 PlatformContext 接口。
 *    这个类包装了真正的 Android Context。
 */
class AndroidPlatformContext(val context: Context) : PlatformContext

actual interface PlatformContext

//actual fun <T, C : PlayerEngineConfig<T>> createPlayerEngine(
//    context: PlatformContext,
//    config: C
//): IPlayerEngine {
//    // 假设您确实需要这个 require 检查
//    require(context is AndroidPlatformContext) { "Context must be of type AndroidPlatformContext" }
//
//    return when (config.engineType) {
//        NATIVE_PLAYER -> ExoPlayerEngine(context.context)
//        MPV_PLAYER -> MPVPlayerEngine(context.context, "mpv_config", "mpv_cache")
//        VLC_PLAYER ->
//            throw UnsupportedOperationException("VLC_PLAYER engine is not supported on this platform.")
//    }
//}
actual fun createPlayerEngine(
    context: PlatformContext,
    config: PlayerEngineConfig
): IPlayerEngine {
    require(context is AndroidPlatformContext){ "Context must be of type AndroidPlatformContext" }
    return when (config) {
        // 在这个分支中，编译器知道 `config` 是 `NativePlayerConfig` 类型
        is NativePlayerConfig -> {
            // 你可以安全地访问 NativePlayerConfig 的所有属性
            ExoPlayerEngine(context.context, config) // 将整个 config 对象传递过去
        }

        // 在这个分支中，编译器知道 `config` 是 `MPVPlayerConfig` 类型
        is MPVPlayerConfig -> {
            // 1. 获取平台默认的缓存根目录，这是所有路径的唯一基础。
            val baseCacheDir = PlatformDefaults.getAppCacheDirectory(context)

            // 2. 确定配置目录的子目录名：要么是用户指定的，要么是 "mpv_config"。
            val configSubDirName = config.configDirectory ?: "mpv_config"

            // 3. 确定缓存目录的子目录名：要么是用户指定的，要么是 "mpv_cache"。
            val cacheSubDirName = config.cacheDirectory ?: "mpv_cache"

            // 4. 使用 File(parent, child) 来安全地拼接路径。
            //    这会自动处理路径分隔符，比手动拼接更健壮。
            val finalConfigDir  = File(baseCacheDir, configSubDirName).absolutePath
            val finalCacheDir = File(baseCacheDir, cacheSubDirName).absolutePath

            val configNew: MPVPlayerConfig = config.copy(
                configDirectory = finalConfigDir,
                cacheDirectory = finalCacheDir
            )

            MPVPlayerEngine(context.context, configNew)
        }

        // 在这个分支中，编译器知道 `config` 是 `VLCPlayerConfig` 类型
        is VLCPlayerConfig -> {
            // 如果不支持，可以像之前一样抛出异常
            throw UnsupportedOperationException("VLC_PLAYER engine is not yet supported on Android.")
        }
    }
}

actual object PlatformDefaults {
    /**
     * 在 Android 上，返回应用专属的缓存目录路径。
     * 例如：/data/user/0/com.your.app/cache
     */
    actual fun getAppCacheDirectory(context: PlatformContext): String {
        require(context is AndroidPlatformContext)
        return context.context.cacheDir.absolutePath;
    }
}