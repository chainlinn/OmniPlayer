package io.github.zhangmanyue774.omniplayer.kernel

import io.github.zhangmanyue774.omniplayer.kernel.engine.IPlayerEngine
import io.github.zhangmanyue774.omniplayer.kernel.plugin.IPlayerPlugin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * 功能完备、遵循最佳实践的多平台播放器核心类。
 * 这是UI层和外部模块与之交互的唯一入口。
 */
class KmpVideoPlayer(
    private val engine: IPlayerEngine, // 通过构造函数注入引擎
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {
    // ------------------- 状态与原生实例暴露 -------------------

    /**
     * 响应式的播放器状态流，推荐UI层使用它来更新界面。
     */
    val playerState: StateFlow<PlayerState> = engine.state

    // ------------------- 插件系统 -------------------
    private val plugins = mutableListOf<IPlayerPlugin>()
    private var stateObserverJob: Job? = null

    // ... imports ...

    init {
        // 监听引擎状态，并分发给所有插件
        stateObserverJob = coroutineScope.launch {
            playerState.onEach { newState ->
                // 【关键修正】
                // 在分发前，创建 plugins 列表的一个不可变副本(快照)。
                // 这样即使在 forEach 循环执行时，其他线程修改了原始的 plugins 列表，
                // 也不会导致 ConcurrentModificationException。
                val currentPlugins = plugins.toList()

                currentPlugins.forEach { plugin ->
                    try {
                        plugin.onStateChanged(newState)
                    } catch (e: Exception) {
                        // 增加保护，防止某个插件的异常导致整个分发链中断
                        // 你可以在这里添加日志来记录哪个插件出了问题
                        e.printStackTrace()
                    }
                }
            }.collect {  }
        }
    }

    /**
     * 添加一个插件。
     */
    fun addPlugin(plugin: IPlayerPlugin) {
        plugins.add(plugin)
        plugin.onInstall(this)
    }

    /**
     * 移除一个插件。
     */
    fun removePlugin(plugin: IPlayerPlugin) {
        plugin.onUninstall()
        plugins.remove(plugin)
    }


    // ------------------- 核心播放控制 (委托给引擎) -------------------
    fun prepare(dataSource: String) = engine.prepare(dataSource)
    fun play() = engine.play()
    fun pause() = engine.pause()
    fun seekTo(positionMs: Long) = engine.seekTo(positionMs)


    // ------------------- 高级控制 (委托给引擎) -------------------
    fun setVolume(volume: Float) = engine.setVolume(volume)
    fun setSpeed(speed: Float) = engine.setSpeed(speed)

    // ------------------- 便捷的状态查询方法 -------------------
    fun isPlaying(): Boolean = playerState.value.isPlaying
    fun isLoading(): Boolean = playerState.value.isLoading
    fun getCurrentPosition(): Long = playerState.value.currentPosition
    fun getDuration(): Long = playerState.value.duration


    // ------------------- 生命周期管理 -------------------
    fun release() {
        // 释放引擎资源
        engine.release()

        // 通知并清理插件
        plugins.forEach { it.onUninstall() }
        plugins.clear()

        // 只取消自己启动的作业(Job)，不取消外部传入的Scope
        stateObserverJob?.cancel()
        stateObserverJob = null
    }
}