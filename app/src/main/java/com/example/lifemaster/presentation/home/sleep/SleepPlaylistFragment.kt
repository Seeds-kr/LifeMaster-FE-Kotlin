package com.example.lifemaster.presentation.home.sleep

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.Fragment
import com.example.lifemaster.R
import com.example.lifemaster.databinding.FragmentSleepPlaylistBinding
import com.example.lifemaster.databinding.ItemSleepPlaylistBinding


class SleepPlaylistFragment : Fragment(R.layout.fragment_sleep_playlist) {

    lateinit var binding: FragmentSleepPlaylistBinding
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
        )
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSleepPlaylistBinding.bind(view)
        initViews()
        initListeners()

    }

    private fun initListeners() = with(binding) {
        llSleepPlaylistWhiteNoiseViewMore.setOnClickListener {

            if (tvSleepPlaylistWhiteNoiseViewMore.text == resources.getString(R.string.sleep_playlist_view_more)) {
                // step 1. 뷰 상태 변경
                tvSleepPlaylistWhiteNoiseViewMore.text = resources.getString(R.string.sleep_playlist_view_less)
                ivSleepPlaylistWhiteNoiseViewMore.rotation = 180f // TODO: rotation, rotationX, rotationY 차이 이해하기
                // step 2. 뷰 추가
                for (i in 2 until sampleMusic.size) {
                    val playlistView = ItemSleepPlaylistBinding.inflate(
                        LayoutInflater.from(context),
                        llSleepPlaylistWhiteNoise,
                        false
                    )
                    playlistView.ivSleepPlaylistItemThumbnail.setImageResource(sampleMusic[i].thumbnail)
                    playlistView.tvSleepPlaylistItemTitle.text = sampleMusic[i].title
                    playlistView.tvSleepPlaylistItemDuration.text = sampleMusic[i].duration
                    playlistView.tvSleepPlaylistItemDescription.text = sampleMusic[i].description
                    llSleepPlaylistWhiteNoise.addView(playlistView.root)
                }
            } else {
                // step 1. 뷰 상태 변경
                tvSleepPlaylistWhiteNoiseViewMore.text = resources.getString(R.string.sleep_playlist_view_more)
                ivSleepPlaylistWhiteNoiseViewMore.rotation = 180f
                // step 2. 뷰 제거
                llSleepPlaylistWhiteNoise.removeViews(2, sampleMusic.size-2)
            }
        }
    }

    private fun initViews() = with(binding) {

        // 2개만 추가 (i = 0 → 1)
        for (i in 0..1) {
            val playlistView = ItemSleepPlaylistBinding.inflate(
                LayoutInflater.from(context),
                llSleepPlaylistWhiteNoise,
                false
            )
            playlistView.ivSleepPlaylistItemThumbnail.setImageResource(sampleMusic[i].thumbnail)
            playlistView.tvSleepPlaylistItemTitle.text = sampleMusic[i].title
            playlistView.tvSleepPlaylistItemDuration.text = sampleMusic[i].duration
            playlistView.tvSleepPlaylistItemDescription.text = sampleMusic[i].description
            llSleepPlaylistWhiteNoise.addView(playlistView.root)
        }
    }
}