package io.github.zhangmanyue774.omniplayer.kernel.engine

import io.github.zhangmanyue774.omniplayer.kernel.PlayerState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow


expect class RenderTarget

interface IPlayerEngine {
    /**
     * 播放器当前状态的响应式流。
     */
    val state: StateFlow<PlayerState>

    fun initialize(): Job?

    /**
     * 附加、更新或移除用于视频渲染的目标。
     * @param target 一个平台相关的渲染目标。在 Android 上通常是 Surface。传入 null 以分离。
     */
    fun setRenderTarget(target: RenderTarget?)

    /**
     * 准备要播放的媒体资源。
     * 调用此方法后，引擎会开始加载媒体，状态变为 isLoading=true。
     * @param dataSource 媒体资源的路径或 URL。
     */
    fun prepare(dataSource: String)

    /**
     * 开始或恢复播放。
     */
    fun play()

    /**
     * 暂停播放。
     */
    fun pause()

    /**
     * 定位到指定的时间点。
     * @param positionMs 时间点，单位为毫秒。
     */
    fun seekTo(positionMs: Long)

    /**
     * 设置音量。
     * @param volume 音量值，范围从 0.0f (静音) 到 1.0f (最大)。
     */
    fun setVolume(volume: Float)

    /**
     * 设置播放速度。
     * @param speed 速度倍率，例如 1.0f 是正常速度, 1.5f 是 1.5 倍速。
     */
    fun setSpeed(speed: Float)

    /**
     * 释放所有播放器资源。
     * 调用后，此播放器实例将不可再用。
     */
    fun release()
}
