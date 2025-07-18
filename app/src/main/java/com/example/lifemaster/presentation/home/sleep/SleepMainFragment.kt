package com.example.lifemaster.presentation.home.sleep

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.lifemaster.R
import com.example.lifemaster.databinding.FragmentSleepMainBinding

class SleepMainFragment : Fragment(R.layout.fragment_sleep_main) {

    private lateinit var binding: FragmentSleepMainBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSleepMainBinding.bind(view)
        binding.ivSleepMainNavigateToPlaylist.setOnClickListener {
            findNavController().navigate(R.id.action_sleepMainFragment_to_sleepPlaylistFragment)
        }
    }

}