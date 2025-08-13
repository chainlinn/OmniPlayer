//package mpv.oneblue3.movieapp_teat
//
//import android.os.Bundle
//import android.util.Log
//import android.view.SurfaceHolder
//import androidx.appcompat.app.AppCompatActivity
//import com.oneblue3.movieapp_teat.databinding.ActivityPlayerBinding
//import io.github.zhangmanyue774.omniplayer.kernel.AndroidPlatformContext
//import io.github.zhangmanyue774.omniplayer.kernel.MPVPlayerConfig
//import io.github.zhangmanyue774.omniplayer.kernel.NativePlayerConfig
//import io.github.zhangmanyue774.omniplayer.kernel.PlayerEngineConfig
//import io.github.zhangmanyue774.omniplayer.kernel.createPlayerEngine
//import io.github.zhangmanyue774.omniplayer.kernel.engine.IPlayerEngine
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.Job
//import kotlinx.coroutines.flow.launchIn
//import kotlinx.coroutines.flow.onEach
//import `is`.xyz.mpv.testMpvLibCoreFunctionality
//
//class PlayerActivity : AppCompatActivity() {
//
//    private lateinit var binding: ActivityPlayerBinding
//
//    // 我们用一个可空的 IPlayerEngine 变量来持有当前的播放器实例
//    private var currentPlayerEngine: IPlayerEngine? = null
//
//    // 用于监听状态的协程 Job，方便在切换播放器时取消旧的监听
//    private var stateObserverJob: Job? = null
//    private val scope = CoroutineScope(Dispatchers.Main)
//
//    // 要播放的视频 URL
//    private val videoUrl = "https://cms.chenjh.site/d/cganyawyl2evidx65?/灵笼 - S02E11 - 第 11 集.mp4"
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityPlayerBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//        testMpvLibCoreFunctionality(applicationContext)
//
//        // 设置 SurfaceHolder 的回调，它对于两个播放器都是通用的
//        binding.surfaceView.holder.addCallback(surfaceHolderCallback)
//
//        // 设置按钮点击事件
//        binding.btnPlayExoplayer.setOnClickListener {
//            // 当点击按钮时，我们启动一个使用 NativePlayerConfig 的播放流程
//            startPlayback(NativePlayerConfig())
//        }
//
//        binding.btnPlayMpv.setOnClickListener {
//            // 当点击按钮时，我们启动一个使用 MPVPlayerConfig 的播放流程
//            startPlayback(MPVPlayerConfig())
//        }
//    }
//
//    /**
//     * 核心的播放启动和切换方法。
//     * @param config 决定了要创建哪种播放器的配置对象。
//     */
//    private fun startPlayback(config: PlayerEngineConfig) {
//        // --- 切换逻辑的核心 ---
//        // 步骤 1: 释放当前正在运行的播放器（如果有的话）
//        releaseCurrentPlayer()
//
//        Log.d("PlayerActivity", "Starting playback with config: ${config::class.simpleName}")
//
//        // 步骤 2: 调用工厂函数创建新的播放器实例
//        // **【关键修复】**: 使用 `applicationContext` 而不是 `this` 来匹配 PlatformContext (Context) 类型
//        val newEngine = createPlayerEngine(AndroidPlatformContext(applicationContext), config)
//        this.currentPlayerEngine = newEngine
//
//        // 步骤 3: 为新的播放器设置渲染目标
//        // 如果 Surface 已经创建好了，就立即设置；否则，等 surfaceCreated 回调时再设置
//        if (binding.surfaceView.holder.surface.isValid) {
//            newEngine.setRenderTarget(binding.surfaceView.holder.surface)
//        }
//
//        // 步骤 4: 准备并播放
//        newEngine.prepare(videoUrl)
//        newEngine.play()
//
//        // 步骤 5: 开始监听新播放器的状态
//        observePlayerState(newEngine)
//    }
//
//    /**
//     * SurfaceHolder 的回调实现。
//     * 这个回调对于任何播放器都是一样的。
//     */
//    private val surfaceHolderCallback = object : SurfaceHolder.Callback {
//        override fun surfaceCreated(holder: SurfaceHolder) {
//            Log.d("PlayerActivity", "Surface created.")
//            // 当 Surface 创建时，为当前存在的播放器设置它
//            currentPlayerEngine?.setRenderTarget(holder.surface)
//        }
//
//        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
//            // 通常无需处理
//        }
//
//        override fun surfaceDestroyed(holder: SurfaceHolder) {
//            Log.d("PlayerActivity", "Surface destroyed.")
//            // 当 Surface 销毁时，通知当前播放器分离它
//            currentPlayerEngine?.setRenderTarget(null)
//        }
//    }
//
//    /**
//     * 监听指定播放器引擎的状态，并更新 UI。
//     */
//    private fun observePlayerState(engine: IPlayerEngine) {
//        stateObserverJob?.cancel() // 取消对旧播放器的监听
//        stateObserverJob = engine.state
//                .onEach { state ->
//                // 在主线程更新 UI
//                binding.progressBar.max = state.duration.toInt()
//            binding.progressBar.progress = state.currentPosition.toInt()
//
//            // 可以在这里处理加载状态、错误状态等
//            Log.v("PlayerState", "State updated: isPlaying=${state.isPlaying}, pos=${state.currentPosition}")
//        }
//            .launchIn(scope)
//    }
//
//    /**
//     * 安全地释放当前播放器并清理相关资源。
//     */
//    private fun releaseCurrentPlayer() {
//        stateObserverJob?.cancel()
//        stateObserverJob = null
//
//        currentPlayerEngine?.release()
//        currentPlayerEngine = null
//        Log.d("PlayerActivity", "Current player released.")
//    }
//
//    override fun onPause() {
//        super.onPause()
//        // 当 Activity 不可见时，暂停播放
//        currentPlayerEngine?.pause()
//    }
//
//    override fun onResume() {
//        super.onResume()
//        // 当 Activity 可见时，恢复播放 (如果之前是播放状态)
//        // 这个逻辑可以根据产品需求调整，例如不自动恢复
//        currentPlayerEngine?.play()
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        // **【重要】**: Activity 销毁时，必须彻底释放播放器
//        releaseCurrentPlayer()
//        // 移除 SurfaceHolder 回调，防止内存泄漏
//        binding.surfaceView.holder.removeCallback(surfaceHolderCallback)
//    }
//}