package mpv.oneblue3.movieapp_teat

import android.content.Context
import com.oneblue3.movieapp_teat.R
import com.oneblue3.movieapp_teat.databinding.PlayerControlsMpvBinding
import io.github.zhangmanyue774.omniplayer.kernel.PlayerState

class MpvPlayerUIController(context: Context) : BaseUIController(context, R.layout.player_controls_mpv) {

    private val binding = PlayerControlsMpvBinding.bind(rootView)

    override fun setupClickListeners() {
        binding.btnTogglePlayMpv.setOnClickListener {
            playerEngine?.pause()
        }
    }

    override fun onPlayerStateChanged(state: PlayerState) {
        val duration = state.duration.takeIf { it > 0 }?.toInt() ?: 100
        binding.progressBarMpv.max = duration
        binding.progressBarMpv.progress = state.currentPosition.toInt()

        val currentTimeStr = formatDuration(state.currentPosition)
        val totalTimeStr = formatDuration(state.duration)
        binding.tvTimeMpv.text = "$currentTimeStr / $totalTimeStr"

        // 根据缓冲状态更新按钮文本（只是一个例子）
        if (state.isBuffering) {
            binding.btnTogglePlayMpv.text = "缓冲中..."
        } else if (state.isPlaying) {
            binding.btnTogglePlayMpv.text = "暂停"
        } else {
            binding.btnTogglePlayMpv.text = "播放"
        }
    }
}