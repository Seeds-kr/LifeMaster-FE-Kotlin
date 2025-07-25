package com.example.lifemaster.presentation.home.sleep

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.lifemaster.R
import com.example.lifemaster.databinding.FragmentSleepPlaylistDetailBinding

class SleepPlaylistDetailFragment : Fragment(R.layout.fragment_sleep_playlist_detail) {

    private lateinit var binding: FragmentSleepPlaylistDetailBinding
    private lateinit var handler: Handler
    private lateinit var updateProgressBarTask: Runnable
    private var songAudioResource: Int = 0
    private var mediaPlayer: MediaPlayer? = null
    private var isAudioPlaying: Boolean = false
    private var songTotalTime: Int = 0
    private var songCurrentStartTime = 0L // 가장 최근에 재생 버튼을 누른 시간
    private var accumulatedPlaybackTime = 0L // 가장 최근 재생 이전의 누적된 시간

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSleepPlaylistDetailBinding.bind(view)
        songAudioResource = arguments?.getInt("audio") ?: -1
        initViews()
        initListeners()
    }

    private fun initViews() = with(binding) {
        tvSleepMainMusicTitle.text = arguments?.getString("title")
        handler = Handler(Looper.getMainLooper())
        updateProgressBarTask = object : Runnable {
            override fun run() {
                val currentPlaybackTime = System.currentTimeMillis() - songCurrentStartTime + accumulatedPlaybackTime // 현재 업데이트 되는 총 음악 누적 시간
                progressSleepMain.setProgress(currentPlaybackTime.toInt(), true)
                handler.postDelayed(this, 100L) // 100ms 마다 실행
            }
        }
    }

    private fun initListeners() = with(binding) {
        llSleepMainPlaylist.setOnClickListener { findNavController().navigate(R.id.action_sleepPlaylistDetailFragment_to_sleepPlaylistFragment) }
        ivSleepMainPlayToggle.setOnClickListener {
            if (isAudioPlaying) {
                // 멈추기
                mediaPlayer?.pause()
                isAudioPlaying = false
                ivSleepMainPlayToggle.setImageResource(R.drawable.ic_play_no_background)
                accumulatedPlaybackTime += System.currentTimeMillis() - songCurrentStartTime
                handler.removeCallbacks(updateProgressBarTask)
            } else {
                // 재생하기
                if(mediaPlayer == null) {
                    mediaPlayer = MediaPlayer.create(context, songAudioResource).apply {
                        songTotalTime = duration
                        progressSleepMain.max = duration
                        setOnCompletionListener {
                            handler.removeCallbacks(updateProgressBarTask)
                            mediaPlayer?.stop()
                            mediaPlayer?.release()
                            mediaPlayer = null
                            progressSleepMain.progress = 0
                            ivSleepMainPlayToggle.setImageResource(R.drawable.ic_play_no_background)
                            isAudioPlaying = false
                            songCurrentStartTime = 0L
                            accumulatedPlaybackTime = 0L
                        }
                    }
                }
                mediaPlayer?.start()
                isAudioPlaying = true
                ivSleepMainPlayToggle.setImageResource(R.drawable.ic_pause)
                songCurrentStartTime = System.currentTimeMillis()
                handler.post(updateProgressBarTask)
            }
        }
    }

}