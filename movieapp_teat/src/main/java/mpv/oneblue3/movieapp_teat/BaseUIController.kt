package mpv.oneblue3.movieapp_teat

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import io.github.zhangmanyue774.omniplayer.kernel.engine.IPlayerEngine
import java.util.Locale

abstract class BaseUIController(
    protected val context: Context,
    layoutId: Int,
) : PlayerUIManager {
    protected var playerEngine: IPlayerEngine? = null
    protected var rootView: View = LayoutInflater.from(context).inflate(layoutId, null)

    override fun setup(parent: ViewGroup, playerEngine: IPlayerEngine) {
        this.playerEngine = playerEngine
        // 如果视图已存在，先移除
        (rootView.parent as? ViewGroup)?.removeView(rootView)
        // 添加到容器
        parent.addView(rootView)
        setupClickListeners()
    }

    abstract fun setupClickListeners()

    override fun show() {
        rootView.visibility = View.VISIBLE
    }

    override fun hide() {
        rootView.visibility = View.GONE
    }

    override fun showError(message: String) {
        Toast.makeText(context, "播放错误: $message", Toast.LENGTH_LONG).show()
    }

    override fun release() {
        (rootView.parent as? ViewGroup)?.removeView(rootView)
        playerEngine = null
    }

    protected fun formatDuration(millis: Long): String {
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