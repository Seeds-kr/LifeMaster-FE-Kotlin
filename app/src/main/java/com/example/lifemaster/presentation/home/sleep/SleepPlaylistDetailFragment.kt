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
    private var mediaPlayer: MediaPlayer ?= null
    private var isPlaying: Boolean = false
    private var songDuration: Int = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSleepPlaylistDetailBinding.bind(view)
        initListeners()
    }

    private fun initListeners() = with(binding) {
        llSleepMainPlaylist.setOnClickListener { findNavController().navigate(R.id.action_sleepPlaylistDetailFragment_to_sleepPlaylistFragment) }
        ivSleepMainPlayToggle.setOnClickListener {
            if(mediaPlayer == null) {
                // 맨 처음 재생
                mediaPlayer = MediaPlayer.create(context, R.raw.sleep_classic_gymnopedie_no1).apply {
                    songDuration = duration
                    progressSleepMain.max = duration
                }
                mediaPlayer?.start()
                isPlaying = true
                ivSleepMainPlayToggle.setImageResource(R.drawable.ic_pause)
                // 프로그래스바 UI 변경
                val handler = Handler(Looper.getMainLooper())
                val songStartTime = System.currentTimeMillis() // 노래 처음 시작 시간 (단위: ms) → 정적
                val updateProgressBarTask = object: Runnable {
                    override fun run() {
                        val songElapsedTime = System.currentTimeMillis() - songStartTime // 노래 누적 재생 시간 (단위: ms) → 동적
                        if(songElapsedTime <= songDuration) {
                            progressSleepMain.setProgress(songElapsedTime.toInt(), true)
                            handler.postDelayed(this, 100L) // 100ms 마다 실행
                        } else {
                            // 초기화
                            mediaPlayer?.stop()
                            mediaPlayer?.release()
                            mediaPlayer = null
                            progressSleepMain.progress = 0
                            ivSleepMainPlayToggle.setImageResource(R.drawable.ic_play_no_background)
                        }
                    }
                }
                handler.post(updateProgressBarTask)
            } else {
                if(isPlaying) {
                    mediaPlayer?.pause()
                    isPlaying = false
                    ivSleepMainPlayToggle.setImageResource(R.drawable.ic_play_no_background)
                } else {
                    mediaPlayer?.start()
                    isPlaying = true
                    ivSleepMainPlayToggle.setImageResource(R.drawable.ic_pause)
                }
            }
        }
    }

}