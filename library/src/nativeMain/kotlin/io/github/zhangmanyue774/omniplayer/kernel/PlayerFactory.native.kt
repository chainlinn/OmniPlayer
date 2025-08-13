package io.github.zhangmanyue774.omniplayer.kernel

import io.github.zhangmanyue774.omniplayer.kernel.engine.IPlayerEngine


actual interface PlatformContext



actual fun createPlayerEngine(
    context: PlatformContext,
    config: PlayerEngineConfig
): IPlayerEngine {
    TODO("Not yet implemented")
}

actual object PlatformDefaults {
    actual fun getAppCacheDirectory(context: PlatformContext): String {
        TODO("Not yet implemented")
    }
}