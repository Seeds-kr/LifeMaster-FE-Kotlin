package com.example.lifemaster.presentation.home.sleep

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.lifemaster.R
import com.example.lifemaster.databinding.FragmentSleepPlaylistDetailBinding

class SleepPlaylistDetailFragment : Fragment(R.layout.fragment_sleep_playlist_detail) {

    private lateinit var binding: FragmentSleepPlaylistDetailBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSleepPlaylistDetailBinding.bind(view)
        binding.llSleepMainPlaylist.setOnClickListener {
            findNavController().navigate(R.id.action_sleepPlaylistDetailFragment_to_sleepPlaylistFragment)
        }
    }

}