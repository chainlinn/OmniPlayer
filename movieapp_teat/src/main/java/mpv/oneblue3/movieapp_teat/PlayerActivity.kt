package mpv.oneblue3.movieapp_teat

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.View
import android.widget.PopupMenu
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import com.oneblue3.movieapp_teat.R
import com.oneblue3.movieapp_teat.databinding.ActivityPlayerBinding
import com.oneblue3.movieapp_teat.databinding.PlayerControlsLayoutBinding
import io.github.zhangmanyue774.omniplayer.kernel.AndroidPlatformContext
import io.github.zhangmanyue774.omniplayer.kernel.MPVPlayerConfig
import io.github.zhangmanyue774.omniplayer.kernel.NativePlayerConfig
import io.github.zhangmanyue774.omniplayer.kernel.PlayerEngineConfig
import io.github.zhangmanyue774.omniplayer.kernel.createPlayerEngine
import io.github.zhangmanyue774.omniplayer.kernel.engine.IPlayerEngine
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.Locale

class PlayerActivity : AppCompatActivity() {

    private val TAG = "PlayerActivity"
    private lateinit var activityBinding: ActivityPlayerBinding
    private lateinit var controlsBinding: PlayerControlsLayoutBinding

    private var currentPlayerEngine: IPlayerEngine? = null
    private var stateObserverJob: Job? = null

    private var isControlsVisible = true
    private var hideControlsJob: Job? = null
    private var isSeeking = false
    private var isPlaying = false // 追踪播放状态的标志

    private val videoUrl = "https://cms.chenjh.site/d/cganyawyl2evidx65?/灵笼 - S02E11 - 第 11 集.mp4"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityBinding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(activityBinding.root)

        // 手动加载控制器布局并添加到容器中
        val controlsView = LayoutInflater.from(this).inflate(R.layout.player_controls_layout, activityBinding.controlsContainer, false)
        activityBinding.controlsContainer.addView(controlsView)
        controlsBinding = PlayerControlsLayoutBinding.bind(controlsView)

        // 设置边到边渲染
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setupWindowInsets()

        setupClickListeners()
        activityBinding.surfaceView.holder.addCallback(surfaceHolderCallback)

        // 默认使用ExoPlayer启动
        startPlayback(NativePlayerConfig())
    }

    private fun startPlayback(config: PlayerEngineConfig) {
        releaseCurrentPlayer() // 会重置 isPlaying = false
        activityBinding.progressBarLoading.visibility = View.VISIBLE

        Log.d(TAG, "Starting playback with config: ${config::class.simpleName}")
        val engineType = if (config is MPVPlayerConfig) "MPV" else "ExoPlayer"
        controlsBinding.tvTitle.text = "灵笼 (内核: $engineType)"

        val newEngine = createPlayerEngine(AndroidPlatformContext(applicationContext), config)
        this.currentPlayerEngine = newEngine

        lifecycleScope.launch {
            try {
                newEngine.initialize()?.join()
                if (activityBinding.surfaceView.holder.surface.isValid) {
                    newEngine.setRenderTarget(activityBinding.surfaceView.holder.surface)
                }
                newEngine.prepare(videoUrl)
                newEngine.play()
                observePlayerState(newEngine)

                // 确保控件初始状态是可见的
                controlsBinding.topControls.alpha = 1.0f
                controlsBinding.bottomControls.alpha = 1.0f
                toggleControls(true)

            } catch (e: Exception) {
                Log.e(TAG, "Failed to start playback", e)
                Toast.makeText(this@PlayerActivity, "播放失败: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                activityBinding.progressBarLoading.visibility = View.GONE
            }
        }
    }

    private fun setupClickListeners() {
        controlsBinding.root.setOnClickListener {
            toggleControls(!isControlsVisible)
        }
        controlsBinding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        // 使用 isPlaying 标志来手动实现 toggle 功能
        controlsBinding.btnPlayPause.setOnClickListener {
            if (isPlaying) {
                currentPlayerEngine?.pause()
            } else {
                currentPlayerEngine?.play()
            }
        }
        controlsBinding.btnFullscreen.setOnClickListener {
            toggleFullscreen()
        }
        controlsBinding.btnMore.setOnClickListener { view ->
            val popup = PopupMenu(this, view)
            popup.menuInflater.inflate(R.menu.player_options, popup.menu)
            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.option_exo_player -> {
                        Toast.makeText(this, "切换到 ExoPlayer 内核", Toast.LENGTH_SHORT).show()
                        startPlayback(NativePlayerConfig())
                        true
                    }
                    R.id.option_mpv_player -> {
                        Toast.makeText(this, "切换到 MPV 内核", Toast.LENGTH_SHORT).show()
                        startPlayback(MPVPlayerConfig())
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
        controlsBinding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    controlsBinding.tvCurrentTime.text = formatDuration(progress.toLong())
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isSeeking = true
                hideControlsJob?.cancel()
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                isSeeking = false
                currentPlayerEngine?.seekTo(seekBar?.progress?.toLong() ?: 0)
                hideControlsWithDelay()
            }
        })
    }

    private fun toggleControls(show: Boolean) {
        isControlsVisible = show
        hideControlsJob?.cancel()
        val controller = WindowCompat.getInsetsController(window, activityBinding.root)
        if (show) {
            controlsBinding.topControls.animate().alpha(1.0f).withStartAction { controlsBinding.topControls.visibility = View.VISIBLE }.setDuration(200).start()
            controlsBinding.bottomControls.animate().alpha(1.0f).withStartAction { controlsBinding.bottomControls.visibility = View.VISIBLE }.setDuration(200).start()
            controller.show(WindowInsetsCompat.Type.systemBars())
            hideControlsWithDelay()
        } else {
            controlsBinding.topControls.animate().alpha(0.0f).withEndAction { controlsBinding.topControls.visibility = View.GONE }.setDuration(200).start()
            controlsBinding.bottomControls.animate().alpha(0.0f).withEndAction { controlsBinding.bottomControls.visibility = View.GONE }.setDuration(200).start()
            controller.hide(WindowInsetsCompat.Type.systemBars())
        }
    }

    private fun hideControlsWithDelay() {
        hideControlsJob = lifecycleScope.launch {
            delay(4000)
            if (isControlsVisible) {
                toggleControls(false)
            }
        }
    }

    private fun observePlayerState(engine: IPlayerEngine) {
        stateObserverJob = engine.state.onEach { state ->
            this.isPlaying = state.isPlaying
            if (!isSeeking) {
                controlsBinding.seekBar.max = state.duration.takeIf { it > 0 }?.toInt() ?: 100
                controlsBinding.seekBar.progress = state.currentPosition.toInt()
                controlsBinding.tvCurrentTime.text = formatDuration(state.currentPosition)
                controlsBinding.tvTotalTime.text = formatDuration(state.duration)
            }
            if (state.isPlaying) {
                controlsBinding.btnPlayPause.setImageResource(R.drawable.ic_pause_24)
            } else {
                controlsBinding.btnPlayPause.setImageResource(R.drawable.ic_play_arrow_24)
            }
            if (state.isBuffering && !state.isPlaying) {
                activityBinding.progressBarLoading.visibility = View.VISIBLE
            } else {
                activityBinding.progressBarLoading.visibility = View.GONE
            }
            state.error?.let {
                Toast.makeText(this, "播放错误: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }.launchIn(lifecycleScope)
    }

    private fun toggleFullscreen() {
        requestedOrientation = if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT
        } else {
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateFullscreenButton()
        toggleControls(true)
    }

    private fun updateFullscreenButton() {
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            controlsBinding.btnFullscreen.setImageResource(R.drawable.outline_chip_extraction_24)
        } else {
            controlsBinding.btnFullscreen.setImageResource(R.drawable.fullscreen)
        }
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(activityBinding.root) { _, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            controlsBinding.topControls.updatePadding(top = insets.top)
            controlsBinding.bottomControls.updatePadding(bottom = insets.bottom)
            WindowInsetsCompat.CONSUMED
        }
    }

    private val surfaceHolderCallback = object : SurfaceHolder.Callback {
        override fun surfaceCreated(holder: SurfaceHolder) {
            Log.d(TAG, "Surface created.")
            currentPlayerEngine?.setRenderTarget(holder.surface)
        }

        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            Log.d(TAG, "Surface changed to: ${width}x${height}")
            currentPlayerEngine?.setRenderTarget(holder.surface)
        }

        override fun surfaceDestroyed(holder: SurfaceHolder) {
            Log.d(TAG, "Surface destroyed.")
            currentPlayerEngine?.setRenderTarget(null)
        }
    }

    private fun releaseCurrentPlayer() {
        hideControlsJob?.cancel()
        stateObserverJob?.cancel()
        stateObserverJob = null
        currentPlayerEngine?.release()
        currentPlayerEngine = null
        isPlaying = false
    }

    override fun onPause() {
        super.onPause()
        currentPlayerEngine?.pause()
    }

    override fun onResume() {
        super.onResume()
        updateFullscreenButton()
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseCurrentPlayer()
    }

    private fun formatDuration(millis: Long): String {
        if (millis < 0) return "--:--"
        val totalSeconds = millis / 1000
        val seconds = totalSeconds % 60
        val minutes = (totalSeconds / 60) % 60
        val hours = totalSeconds / 3600
        return if (hours > 0) {
            String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        }
    }
}