package mpv.oneblue3.movieapp_teat

import android.view.ViewGroup
import io.github.zhangmanyue774.omniplayer.kernel.PlayerState
import io.github.zhangmanyue774.omniplayer.kernel.engine.IPlayerEngine
interface PlayerUIManager {
    fun setup(parent: ViewGroup, playerEngine: IPlayerEngine)
    fun onPlayerStateChanged(state: PlayerState)
    fun show()
    fun hide()
    fun showError(message: String)
    fun release()
}