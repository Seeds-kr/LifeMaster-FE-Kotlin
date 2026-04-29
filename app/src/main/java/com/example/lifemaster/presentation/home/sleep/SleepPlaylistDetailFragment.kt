package com.example.lifemaster.presentation.home.sleep

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.lifemaster.R
import com.example.lifemaster.databinding.FragmentSleepPlaylistDetailBinding
import com.example.lifemaster.network.NetworkService
import com.example.lifemaster.presentation.home.sleep.viewmodel.SleepViewModel
import com.example.lifemaster.presentation.home.sleep.viewmodel.SleepViewModelFactory
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SleepPlaylistDetailFragment : Fragment(R.layout.fragment_sleep_playlist_detail) {

    private lateinit var binding: FragmentSleepPlaylistDetailBinding

    @Inject
    lateinit var networkService: NetworkService

    private val sleepViewModel: SleepViewModel by activityViewModels {
        SleepViewModelFactory(networkService)
    }

    private lateinit var handler: Handler
    private lateinit var updateProgressBarTask: Runnable

    private var songAudioResource: Int = -1
    private var mediaPlayer: MediaPlayer? = null
    private var isAudioPlaying: Boolean = false
    private var songTotalTime: Int = 0
    private var songCurrentStartTime = 0L
    private var accumulatedPlaybackTime = 0L

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSleepPlaylistDetailBinding.bind(view)

        songAudioResource = arguments?.getInt("audio", -1) ?: -1

        initViews()
        initListeners()
    }

    private fun initViews() = with(binding) {

        if (sleepViewModel.isMeasured) {
            tvSleepPlaylistDetailSleepDuration.text =
                "${sleepViewModel.sleepTime} ~ ${sleepViewModel.wakeTime}"
            tvSleepMainHour.text =
                if (sleepViewModel.sleepDurationHour.toString().length == 1) {
                    "0${sleepViewModel.sleepDurationHour}"
                } else {
                    "${sleepViewModel.sleepDurationHour}"
                }
            tvSleepMainMinute.text = sleepViewModel.sleepDurationMinutes.toString()
        } else {
            tvSleepMainTitle.text = "Your Sleep\nIs Not Recorded"
            tvSleepPlaylistDetailSleepDuration.text = "수면 정보가 없습니다"
            tvSleepMainHour.isVisible = false
            tvSleepMainMinute.isVisible = false
            tvSleepMainHourLabel.isVisible = false
            tvSleepMainMinuteLabel.isVisible = false
        }

        tvSleepMainMusicTitle.text = arguments?.getString("title") ?: "선택한 음악이 없습니다"

        handler = Handler(Looper.getMainLooper())
        updateProgressBarTask = object : Runnable {
            override fun run() {
                val currentPlaybackTime =
                    System.currentTimeMillis() - songCurrentStartTime + accumulatedPlaybackTime

                progressSleepMain.setProgress(currentPlaybackTime.toInt(), true)
                handler.postDelayed(this, 100L)
            }
        }
    }

    private fun initListeners() = with(binding) {
        llSleepMainPlaylist.setOnClickListener {
            findNavController().navigate(R.id.action_sleepPlaylistDetailFragment_to_sleepPlaylistFragment)
        }

        ivSleepMainPlayToggle.setOnClickListener {
            if (songAudioResource == -1) {
                Toast.makeText(requireContext(), "선택한 음악이 없습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (isAudioPlaying) {
                pauseAudio()
            } else {
                startAudio()
            }
        }
    }

    private fun startAudio() {
        with(binding) {
            if (mediaPlayer == null) {
                val createdPlayer = try {
                    MediaPlayer.create(requireContext(), songAudioResource)
                } catch (e: Exception) {
                    null
                }

                if (createdPlayer == null) {
                    Toast.makeText(requireContext(), "선택한 음악을 재생할 수 없습니다.", Toast.LENGTH_SHORT).show()
                    return@with
                }

                mediaPlayer = createdPlayer
                songTotalTime = createdPlayer.duration
                progressSleepMain.max = songTotalTime

                createdPlayer.setOnCompletionListener {
                    stopAndResetAudio()
                }
            }

            try {
                mediaPlayer?.start()
                isAudioPlaying = true
                ivSleepMainPlayToggle.setImageResource(R.drawable.ic_pause)
                songCurrentStartTime = System.currentTimeMillis()
                handler.post(updateProgressBarTask)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "음악 재생 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                stopAndResetAudio()
            }
        }
    }

    private fun pauseAudio() = with(binding) {
        try {
            mediaPlayer?.pause()
            isAudioPlaying = false
            ivSleepMainPlayToggle.setImageResource(R.drawable.ic_play_no_background)
            accumulatedPlaybackTime += System.currentTimeMillis() - songCurrentStartTime
            handler.removeCallbacks(updateProgressBarTask)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "음악 일시정지 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            stopAndResetAudio()
        }
    }

    private fun stopAndResetAudio() = with(binding) {
        handler.removeCallbacks(updateProgressBarTask)

        try {
            mediaPlayer?.stop()
        } catch (_: Exception) {
        }

        try {
            mediaPlayer?.release()
        } catch (_: Exception) {
        }

        mediaPlayer = null
        progressSleepMain.progress = 0
        ivSleepMainPlayToggle.setImageResource(R.drawable.ic_play_no_background)
        isAudioPlaying = false
        songCurrentStartTime = 0L
        accumulatedPlaybackTime = 0L
        songTotalTime = 0
    }

    override fun onStop() {
        super.onStop()
        stopAndResetAudio()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(updateProgressBarTask)
    }
}