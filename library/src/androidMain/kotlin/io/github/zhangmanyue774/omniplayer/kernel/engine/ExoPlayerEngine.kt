// file: androidMain/kotlin/com/your/package/engine/ExoPlayerEngine.kt

package io.github.zhangmanyue774.omniplayer.kernel.engine
import android.content.Context
import android.view.Surface
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.Player.Listener
import androidx.media3.common.VideoSize
import androidx.media3.exoplayer.ExoPlayer
import io.github.zhangmanyue774.omniplayer.kernel.NativePlayerConfig
import io.github.zhangmanyue774.omniplayer.kernel.PlayerState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 一个使用 Media3 ExoPlayer 的 IPlayerEngine 实现。
 * 它被设计为完全独立的、无 UI 的播放内核。
 *
 * @param context Android Application Context.
 * @param config ExoPlayer 的特定配置 (当前未使用，但为未来扩展保留).
 */
class ExoPlayerEngine(
    context: Context,
    config: NativePlayerConfig
) : IPlayerEngine, Listener { // 直接实现 Listener 接口

    // 1. 核心播放器实例
    private val exoPlayer: ExoPlayer = ExoPlayer.Builder(context).build()

    // 2. 状态管理
    private val _state = MutableStateFlow(PlayerState())
    override val state = _state.asStateFlow()
    override fun initialize(): Job? {
        return null; // ExoPlayer 不需要额外的初始化步骤，直接使用即可
    }

    // 3. 协程作用域，用于进度更新等后台任务
    private val engineScope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())
    private var progressTrackerJob: Job? = null

    /**
     * 在引擎被创建时，立即进行内部设置。
     */
    init {
        // 将自身注册为 ExoPlayer 的监听器
        exoPlayer.addListener(this)

        // （可选）根据传入的 config 进行一些初始设置
        // exoPlayer.videoScalingMode = ...
    }

    // --- IPlayerEngine 接口实现 ---

    override fun setRenderTarget(target: RenderTarget?) {
        // ExoPlayer 可以通过 setVideoSurface 直接处理 Surface 对象
        exoPlayer.setVideoSurface(target)
    }

    override fun prepare(dataSource: String) {
        val mediaItem = MediaItem.fromUri(dataSource)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
    }

    override fun play() {
        exoPlayer.play()
    }

    override fun pause() {
        exoPlayer.pause()
    }

    override fun seekTo(positionMs: Long) {
        exoPlayer.seekTo(positionMs)
    }

    override fun setVolume(volume: Float) {
        exoPlayer.volume = volume.coerceIn(0f, 1f)
    }

    override fun setSpeed(speed: Float) {
        val params = PlaybackParameters(speed)
        exoPlayer.playbackParameters = params
    }

    override fun release() {
        engineScope.cancel() // 取消所有后台协程
        exoPlayer.removeListener(this) // 注销监听器
        exoPlayer.release() // 释放 ExoPlayer 核心资源
    }

    // --- Player.Listener 接口实现 (用于驱动状态更新) ---

    override fun onPlaybackStateChanged(playbackState: Int) {
        val currentState = _state.value
        val newState = currentState.copy(
            // ExoPlayer 的 STATE_IDLE 表示刚准备好或已停止，可视为非加载状态
            isLoading = playbackState == Player.STATE_BUFFERING,
            isEnded = playbackState == Player.STATE_ENDED,
        )
        if (newState != currentState) {
            _state.value = newState
        }
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        if (isPlaying) {
            startProgressTracker()
        } else {
            stopProgressTracker()
        }

        val currentState = _state.value
        val newState = currentState.copy(isPlaying = isPlaying)
        if (newState != currentState) {
            _state.value = newState
        }
    }

    override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
        _state.value = _state.value.copy(error = error)
    }

    // (可以按需实现 Listener 的其他方法, 例如 onVideoSizeChanged)
    // override fun onVideoSizeChanged(videoSize: VideoSize) { ... }

    // --- 私有辅助方法 ---

    private fun startProgressTracker() {
        stopProgressTracker() // 确保只有一个在运行
        progressTrackerJob = engineScope.launch {
            while (isActive) {
                // 当播放器处于活动状态时才更新进度
                if (exoPlayer.playbackState == Player.STATE_READY || exoPlayer.playbackState == Player.STATE_BUFFERING) {
                    _state.value = _state.value.copy(
                        currentPosition = exoPlayer.currentPosition,
                        duration = exoPlayer.duration.coerceAtLeast(0)
                    )
                }
                delay(500) // 每 500 毫秒更新一次
            }
        }
    }

    private fun stopProgressTracker() {
        progressTrackerJob?.cancel()
        progressTrackerJob = null
    }
}