package io.github.zhangmanyue774.omniplayer.kernel

/**
 * 一个不可变的数据类，用于描述播放器的所有状态。
 * UI层可以安全地观察这个对象以更新视图。
 */
data class PlayerState(
    val isPlaying: Boolean = false,
    val isLoading: Boolean = true,
    val isBuffering: Boolean = false,
    val currentPosition: Long = 0,
    val duration: Long = 0,
    val error: Throwable? = null,
    val isEnded: Boolean = false,
)