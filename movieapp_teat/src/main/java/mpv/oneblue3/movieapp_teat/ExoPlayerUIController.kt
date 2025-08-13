package mpv.oneblue3.movieapp_teat

import android.content.Context
import android.view.View
import android.widget.SeekBar
import com.oneblue3.movieapp_teat.R
import com.oneblue3.movieapp_teat.databinding.PlayerControlsExoplayerBinding
import io.github.zhangmanyue774.omniplayer.kernel.PlayerState

class ExoPlayerUIController(context: Context) : BaseUIController(context, R.layout.player_controls_exoplayer) {

    private val binding = PlayerControlsExoplayerBinding.bind(rootView)

    override fun setupClickListeners() {
        binding.btnPlayPauseExo.setOnClickListener {
            playerEngine?.pause()
        }
        binding.seekBarExo.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    binding.tvCurrentTimeExo.text = formatDuration(progress.toLong())
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                playerEngine?.seekTo(seekBar?.progress?.toLong() ?: 0)
            }
        })
    }

    override fun onPlayerStateChanged(state: PlayerState) {
        binding.seekBarExo.max = state.duration.takeIf { it > 0 }?.toInt() ?: 100
        binding.seekBarExo.progress = state.currentPosition.toInt()
        binding.tvCurrentTimeExo.text = formatDuration(state.currentPosition)
        binding.tvTotalTimeExo.text = formatDuration(state.duration)

        if (state.isPlaying) {
            binding.btnPlayPauseExo.setImageResource(R.drawable.ic_pause)
        } else {
            binding.btnPlayPauseExo.setImageResource(R.drawable.ic_play_arrow_24)
        }
    }

    // 重写 show/hide 以控制不同组件
    override fun show() {
        binding.btnPlayPauseExo.visibility = View.VISIBLE
        binding.bottomControls.visibility = View.VISIBLE
    }

    override fun hide() {
        binding.btnPlayPauseExo.visibility = View.GONE
        binding.bottomControls.visibility = View.GONE
    }
}