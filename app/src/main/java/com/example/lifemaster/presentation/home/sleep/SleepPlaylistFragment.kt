package com.example.lifemaster.presentation.home.sleep

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.lifemaster.R
import com.example.lifemaster.databinding.FragmentSleepPlaylistBinding


class SleepPlaylistFragment : Fragment(R.layout.fragment_sleep_playlist) {

    lateinit var binding: FragmentSleepPlaylistBinding
    private val sleepPlaylistAdapter = SleepPlaylistAdapter()
    private val sampleMusic = listOf(
        SleepItem(
            id = 1,
            title = "꿀잠 자는 백색소음",
            duration = "01:10:32",
            thumbnail = R.drawable.tmp_sleep_playlist,
            description = "포브스 선정 잘 때 틀면 꿀잠 자는 백색소음 Top 100"
        ),
        SleepItem(
            id = 2,
            title = "꿀잠 자는 백색소음 2",
            duration = "00:45:20",
            thumbnail = R.drawable.tmp_sleep_playlist,
            description = "포브스 선정 잘 때 틀면 꿀잠 자는 백색소음 Top 100 중에 101위"
        ),
        SleepItem(
            id = 3,
            title = "꿀잠 자는 백색소음 3",
            duration = "02:00:00",
            thumbnail = R.drawable.tmp_sleep_playlist,
            description = "포브스 선정 잘 때 틀면 꿀잠 자는 백색소음 Top 100 중에 102위"
        ),
        SleepItem(
            id = 4,
            title = "꿀잠 자는 백색소음 4",
            duration = "01:30:45",
            thumbnail = R.drawable.tmp_sleep_playlist,
            description = "포브스 선정 잘 때 틀면 꿀잠 자는 백색소음 Top 100 중에 103위"
        ),
        SleepItem(
            id = 5,
            title = "꿀잠 자는 백색소음 5",
            duration = "00:55:10",
            thumbnail = R.drawable.tmp_sleep_playlist,
            description = "포브스 선정 잘 때 틀면 꿀잠 자는 백색소음 Top 100 중에 104위"
        )
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSleepPlaylistBinding.bind(view)
        binding.recyclerviewSleepPlaylistWhiteNoise.adapter = sleepPlaylistAdapter
        sleepPlaylistAdapter.submitList(sampleMusic)
    }
}