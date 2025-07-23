package com.example.lifemaster.presentation.home.sleep

import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.lifemaster.R
import com.example.lifemaster.databinding.FragmentSleepPlaylistDetailBinding

class SleepPlaylistDetailFragment : Fragment(R.layout.fragment_sleep_playlist_detail) {

    private lateinit var binding: FragmentSleepPlaylistDetailBinding
    private var mediaPlayer: MediaPlayer ?= null
    private var isPlaying: Boolean = false

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
                mediaPlayer = MediaPlayer.create(context, R.raw.sleep_classic_gymnopedie_no1)
                mediaPlayer?.start()
                isPlaying = true
                ivSleepMainPlayToggle.setImageResource(R.drawable.ic_pause)
            } else {
                if(isPlaying) {
                    // 멈추기
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