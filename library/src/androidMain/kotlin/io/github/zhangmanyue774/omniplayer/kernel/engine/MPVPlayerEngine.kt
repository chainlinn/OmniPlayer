package io.github.zhangmanyue774.omniplayer.kernel.engine

import android.content.Context
import android.util.Log
import android.view.Surface
import `is`.xyz.mpv.MPVLib
import io.github.zhangmanyue774.omniplayer.kernel.MPVPlayerConfig
import io.github.zhangmanyue774.omniplayer.kernel.PlayerState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import kotlin.math.roundToLong


class MPVPlayerEngine(
    private val context: Context,
    private val config: MPVPlayerConfig
) : IPlayerEngine, MPVLib.EventObserver {

    private val TAG = "OmniPlayer_MPVEngine"

    // 使用 SupervisorJob，一个子协程的失败不会影响其他子协程
    private val engineScope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())
    private val _state = MutableStateFlow(PlayerState(isLoading = true)) // 创建时就设为加载中

    @Volatile
    private var isReleased = false
    @Volatile
    private var isInitialized = false

    private var pendingDataSource: String? = null
    private var positionUpdaterJob: Job? = null

    init {
        // 构造函数现在非常干净，只打印日志，不做任何耗时操作
        Log.d(TAG, "MPVPlayerEngine instance created. Waiting for initialize().")
    }

    override fun initialize(): Job {
        return engineScope.launch {
            if (isInitialized || isReleased) return@launch
            Log.d(TAG, "Starting MPVPlayerEngine initialization...")

            // 使用 withContext 将重量级操作切换到后台IO线程
            withContext(Dispatchers.IO) {
                try {
                    // 1. 创建目录 (文件操作，放在IO线程)
                    config.configDirectory?.let { File(it).mkdirs() }
                    config.cacheDirectory?.let { File(it).mkdirs() }

                    // 2. 初始化 MPVLib (核心耗时操作)
                    Log.d(TAG, "[IO Thread] Calling MPVLib.create...")
                    MPVLib.create(context)
                    Log.d(TAG, "[IO Thread] MPVLib.create finished.")

                    Log.d(TAG, "[IO Thread] Setting options...")
                    MPVLib.setOptionString("config", "yes")
                    config.configDirectory?.let { MPVLib.setOptionString("config-dir", it) }
                    config.cacheDirectory?.let { MPVLib.setOptionString("gpu-shader-cache-dir", it) }
                    MPVLib.setOptionString("hwdec", config.hardwareDecodingProfile)
                    MPVLib.setOptionString("idle", "once")
                    MPVLib.setOptionString("vo", "gpu")
                    MPVLib.setOptionString("ao", "audiotrack,opensles")
                    MPVLib.setOptionString("force-window", "no")
                    Log.d(TAG, "[IO Thread] Options set.")

                    Log.d(TAG, "[IO Thread] Calling MPVLib.init...")
                    MPVLib.init()
                    Log.d(TAG, "[IO Thread] MPVLib.init finished.")

                    // 标记为已初始化
                    isInitialized = true

                    // 3. 注册观察者 (这个操作通常需要和事件循环在同一个线程，切换回主线程更安全)
                    withContext(Dispatchers.Main) {
                        if (isReleased) return@withContext // 再次检查，防止在切换过程中被释放
                        MPVLib.addObserver(this@MPVPlayerEngine)
                        observeProperties()
                        // 初始化完成，更新状态
                        updateState { copy(isLoading = false, error = null) }
                        Log.i(TAG, "[Main Thread] MPVPlayerEngine initialization complete.")
                    }

                } catch (e: Throwable) {
                    Log.e(TAG, "Failed to initialize MPVPlayerEngine", e)
                    // 发生错误，切换回主线程更新UI状态
                    withContext(Dispatchers.Main) {
                        updateState { copy(isLoading = false, error = e) }
                    }
                    release() // 初始化失败，立即释放所有资源
                    throw e   // 重新抛出异常，让调用方的 try-catch 块能捕获到
                }
            }
        }
    }

    // --- IPlayerEngine 接口实现 ---

    override val state = _state.asStateFlow()

    override fun setRenderTarget(target: RenderTarget?) {
        if (isReleased || !isInitialized) return
        // “发射后不管”：在后台执行，不阻塞当前线程
        engineScope.launch(Dispatchers.IO) {
            if (target != null && target.isValid) {
                MPVLib.attachSurface(target)
                MPVLib.setOptionString("force-window", "yes")
                pendingDataSource?.let {
                    MPVLib.command(arrayOf("loadfile", it))
                    pendingDataSource = null
                }
            } else {
                MPVLib.setOptionString("force-window", "no")
                MPVLib.detachSurface()
            }
        }
    }

    override fun prepare(dataSource: String) {
        if (isReleased || !isInitialized) return
        updateState { PlayerState(isLoading = true) }
        engineScope.launch(Dispatchers.IO) {
            if (MPVLib.getPropertyString("force-window") == "yes") {
                MPVLib.command(arrayOf("loadfile", dataSource))
            } else {
                pendingDataSource = dataSource
            }
        }
    }

    override fun play() {
        if (isReleased || !isInitialized) return
        MPVLib.setPropertyBoolean("pause", false)
    }

    override fun pause() {
        if (isReleased || !isInitialized) return
        MPVLib.setPropertyBoolean("pause", true)
    }

    override fun seekTo(positionMs: Long) {
        if (isReleased || !isInitialized) return
        MPVLib.command(arrayOf("seek", (positionMs / 1000.0).toString(), "absolute"))
    }

    override fun setVolume(volume: Float) {
        if (isReleased || !isInitialized) return
        val mpvVolume = (volume.coerceIn(0.0f, 1.0f) * 100).toDouble()
        MPVLib.setPropertyDouble("volume", mpvVolume)
    }

    override fun setSpeed(speed: Float) {
        if (isReleased || !isInitialized) return
        MPVLib.setPropertyDouble("speed", speed.coerceAtLeast(0.01f).toDouble())
    }

    override fun release() {
        if (isReleased) return
        isReleased = true
        isInitialized = false
        Log.d(TAG, "Releasing MPVPlayerEngine...")

        engineScope.cancel()

        Thread {
            try {
                // 确保 removeObserver 和 destroy 在同一个线程，并且是在 MPV 实例还存在时调用
                MPVLib.removeObserver(this)
                MPVLib.command(arrayOf("stop"))
                MPVLib.destroy()
                Log.i(TAG, "MPVLib.destroy() successful.")
            } catch (e: Throwable) {
                Log.e(TAG, "Error during MPVLib.destroy()", e)
            }
        }.start()

        _state.value = PlayerState(isEnded = true)
    }

    // --- MPVLib.EventObserver 实现 ---

    override fun event(eventId: Int) {
        engineScope.launch { // 确保事件处理在正确的协程上下文中
            when (eventId) {
                MPVLib.mpvEventId.MPV_EVENT_FILE_LOADED -> {
                    Log.d(TAG, "Event: FILE_LOADED")
                    val duration = withContext(Dispatchers.IO) { MPVLib.getPropertyDouble("duration") } ?: 0.0
                    updateState {
                        copy(
                            isLoading = false,
                            isEnded = false,
                            duration = (duration * 1000).roundToLong()
                        )
                    }
                }
                MPVLib.mpvEventId.MPV_EVENT_END_FILE -> {
                    Log.d(TAG, "Event: END_FILE")
                    stopPositionUpdates()
                    updateState {
                        copy(
                            isPlaying = false,
                            isEnded = true,
                            currentPosition = this.duration
                        )
                    }
                }
            }
        }
    }

    override fun eventProperty(property: String, value: Boolean) {
        engineScope.launch {
            when (property) {
                "pause" -> {
                    val isPlaying = !value
                    updateState { copy(isPlaying = isPlaying) }
                    if (isPlaying) startPositionUpdates() else stopPositionUpdates()
                }
                "paused-for-cache" -> updateState { copy(isBuffering = value) }
            }
        }
    }

    // 省略其他空的 eventProperty 回调...
    override fun eventProperty(property: String) {}
    override fun eventProperty(property: String, value: Long) {}
    override fun eventProperty(property: String, value: String) {}
    override fun eventProperty(property: String, value: Double) {}

    private fun observeProperties() {
        MPVLib.observeProperty("pause", MPVLib.mpvFormat.MPV_FORMAT_FLAG)
        MPVLib.observeProperty("paused-for-cache", MPVLib.mpvFormat.MPV_FORMAT_FLAG)
    }

    private fun startPositionUpdates() {
        stopPositionUpdates()
        positionUpdaterJob = engineScope.launch {
            while (isActive) {
                val position = withContext(Dispatchers.IO) { MPVLib.getPropertyDouble("time-pos") }
                if (position != null) {
                    updateState { copy(currentPosition = (position * 1000).roundToLong()) }
                }
                delay(500) // 每 500 毫秒更新一次进度
            }
        }
    }

    private fun stopPositionUpdates() {
        positionUpdaterJob?.cancel()
        positionUpdaterJob = null
    }

    private fun updateState(block: PlayerState.() -> PlayerState) {
        if (!isReleased) {
            _state.value = _state.value.block()
        }
    }
}