package io.github.zhangmanyue774.omniplayer.kernel.plugin

import io.github.zhangmanyue774.omniplayer.kernel.KmpVideoPlayer
import io.github.zhangmanyue774.omniplayer.kernel.PlayerState


interface IPlayerPlugin {
    fun onInstall(player: KmpVideoPlayer)
    fun onStateChanged(newState: PlayerState)
    fun onUninstall()
}