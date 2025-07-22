package com.example.lifemaster.presentation.home.sleep

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.lifemaster.R
import com.example.lifemaster.databinding.FragmentSleepPlaylistBinding
import com.example.lifemaster.databinding.LayoutSleepPlaylistBinding


class SleepPlaylistFragment : Fragment(R.layout.fragment_sleep_playlist) {

    lateinit var binding: FragmentSleepPlaylistBinding
    private val sampleWhiteNoiseMusic = listOf(
        SleepItem(
            id = 1,
            genre = MusicGenre.WHITE_NOISE,
            title = "꿀잠 자는 백색소음",
            duration = "01:10:32",
            thumbnail = R.drawable.tmp_sleep_playlist_white_noise,
            description = "포브스 선정 잘 때 틀면 꿀잠 자는 백색소음 Top 100"
        ),
        SleepItem(
            id = 2,
            genre = MusicGenre.WHITE_NOISE,
            title = "꿀잠 자는 백색소음 2",
            duration = "00:45:20",
            thumbnail = R.drawable.tmp_sleep_playlist_white_noise,
            description = "포브스 선정 잘 때 틀면 꿀잠 자는 백색소음 Top 100 중에 101위"
        ),
        SleepItem(
            id = 3,
            genre = MusicGenre.WHITE_NOISE,
            title = "꿀잠 자는 백색소음 3",
            duration = "02:00:00",
            thumbnail = R.drawable.tmp_sleep_playlist_white_noise,
            description = "포브스 선정 잘 때 틀면 꿀잠 자는 백색소음 Top 100 중에 102위"
        )
    )
    private val sampleNatureSoundMusic = listOf(
        SleepItem(
            id = 4,
            genre = MusicGenre.NATURE_SOUND,
            title = "꿀잠 자는 자연의 소리",
            duration = "02:13:32",
            thumbnail = R.drawable.tmp_sleep_playlist_nature_sound,
            description = "포브스 선정 잘 때 틀면 꿀잠 자는 자연의 소리 Top 100"
        ),
        SleepItem(
            id = 5,
            genre = MusicGenre.NATURE_SOUND,
            title = "꿀잠 자는 자연의 소리 2",
            duration = "01:25:20",
            thumbnail = R.drawable.tmp_sleep_playlist_nature_sound,
            description = "포브스 선정 잘 때 틀면 꿀잠 자는 자연의 소리 Top 100 중에 101위"
        ),
        SleepItem(
            id = 6,
            genre = MusicGenre.NATURE_SOUND,
            title = "꿀잠 자는 자연의 소리 3",
            duration = "03:20:14",
            thumbnail = R.drawable.tmp_sleep_playlist_nature_sound,
            description = "포브스 선정 잘 때 틀면 꿀잠 자는 자연의 소리 Top 100 중에 102위"
        )
    )
    private val sampleClassicMusic = listOf(
        SleepItem(
            id = 7,
            genre = MusicGenre.CLASSIC,
            title = "꿀잠 자는 클래식 음악",
            duration = "02:13:32",
            thumbnail = R.drawable.tmp_sleep_playlist_classic,
            description = "포브스 선정 잘 때 틀면 꿀잠 자는 클래식 음악 Top 100"
        ),
        SleepItem(
            id = 8,
            genre = MusicGenre.CLASSIC,
            title = "꿀잠 자는 클래식 음악 2",
            duration = "01:25:20",
            thumbnail = R.drawable.tmp_sleep_playlist_classic,
            description = "포브스 선정 잘 때 틀면 꿀잠 자는 클래식 음악 Top 100 중에 101위"
        ),
        SleepItem(
            id = 9,
            genre = MusicGenre.CLASSIC,
            title = "꿀잠 자는 클래식 음악 3",
            duration = "03:20:14",
            thumbnail = R.drawable.tmp_sleep_playlist_classic,
            description = "포브스 선정 잘 때 틀면 꿀잠 자는 클래식 음악 Top 100 중에 102위"
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
                for (i in 2 until sampleWhiteNoiseMusic.size) {
                    val playlistView = LayoutSleepPlaylistBinding.inflate(
                        LayoutInflater.from(context),
                        llSleepPlaylistWhiteNoise,
                        false
                    )
                    playlistView.ivSleepPlaylistItemThumbnail.setImageResource(sampleWhiteNoiseMusic[i].thumbnail)
                    playlistView.tvSleepPlaylistItemTitle.text = sampleWhiteNoiseMusic[i].title
                    playlistView.tvSleepPlaylistItemDuration.text = sampleWhiteNoiseMusic[i].duration
                    playlistView.tvSleepPlaylistItemDescription.text = sampleWhiteNoiseMusic[i].description
                    playlistView.ivSleepPlaylistItemPlay.setOnClickListener { findNavController().navigate(R.id.action_sleepPlaylistFragment_to_sleepMainFragment) }
                    llSleepPlaylistWhiteNoise.addView(playlistView.root)
                }
            } else {
                // step 1. 뷰 상태 변경
                tvSleepPlaylistWhiteNoiseViewMore.text = resources.getString(R.string.sleep_playlist_view_more)
                ivSleepPlaylistWhiteNoiseViewMore.rotation = 0f
                // step 2. 뷰 제거
                llSleepPlaylistWhiteNoise.removeViews(2, sampleWhiteNoiseMusic.size-2)
            }
        }
        llSleepPlaylistNatureSoundsViewMore.setOnClickListener {
            if (tvSleepPlaylistNatureSoundsViewMore.text == resources.getString(R.string.sleep_playlist_view_more)) {
                // step 1. 뷰 상태 변경
                tvSleepPlaylistNatureSoundsViewMore.text = resources.getString(R.string.sleep_playlist_view_less)
                ivSleepPlaylistNatureSoundsViewMore.rotation = 180f
                // step 2. 뷰 추가
                for (i in 2 until sampleNatureSoundMusic.size) {
                    val playlistView = LayoutSleepPlaylistBinding.inflate(
                        LayoutInflater.from(context),
                        llSleepPlaylistNatureSounds,
                        false
                    )
                    playlistView.ivSleepPlaylistItemThumbnail.setImageResource(sampleNatureSoundMusic[i].thumbnail)
                    playlistView.tvSleepPlaylistItemTitle.text = sampleNatureSoundMusic[i].title
                    playlistView.tvSleepPlaylistItemDuration.text = sampleNatureSoundMusic[i].duration
                    playlistView.tvSleepPlaylistItemDescription.text = sampleNatureSoundMusic[i].description
                    playlistView.ivSleepPlaylistItemPlay.setOnClickListener { findNavController().navigate(R.id.action_sleepPlaylistFragment_to_sleepMainFragment) }
                    llSleepPlaylistNatureSounds.addView(playlistView.root)
                }
            } else {
                // step 1. 뷰 상태 변경
                tvSleepPlaylistNatureSoundsViewMore.text = resources.getString(R.string.sleep_playlist_view_more)
                ivSleepPlaylistNatureSoundsViewMore.rotation = 0f
                // step 2. 뷰 제거
                llSleepPlaylistNatureSounds.removeViews(2, sampleNatureSoundMusic.size-2)
            }
        }
        llSleepPlaylistClassicViewMore.setOnClickListener {
            if (tvSleepPlaylistClassicViewMore.text == resources.getString(R.string.sleep_playlist_view_more)) {
                // step 1. 뷰 상태 변경
                tvSleepPlaylistClassicViewMore.text = resources.getString(R.string.sleep_playlist_view_less)
                ivSleepPlaylistClassicViewMore.rotation = 180f
                // step 2. 뷰 추가
                for (i in 2 until sampleClassicMusic.size) {
                    val playlistView = LayoutSleepPlaylistBinding.inflate(
                        LayoutInflater.from(context),
                        llSleepPlaylistClassic,
                        false
                    )
                    playlistView.ivSleepPlaylistItemThumbnail.setImageResource(sampleClassicMusic[i].thumbnail)
                    playlistView.tvSleepPlaylistItemTitle.text = sampleClassicMusic[i].title
                    playlistView.tvSleepPlaylistItemDuration.text = sampleClassicMusic[i].duration
                    playlistView.tvSleepPlaylistItemDescription.text = sampleClassicMusic[i].description
                    playlistView.ivSleepPlaylistItemPlay.setOnClickListener { findNavController().navigate(R.id.action_sleepPlaylistFragment_to_sleepMainFragment) }
                    llSleepPlaylistClassic.addView(playlistView.root)
                }
            } else {
                // step 1. 뷰 상태 변경
                tvSleepPlaylistClassicViewMore.text = resources.getString(R.string.sleep_playlist_view_more)
                ivSleepPlaylistClassicViewMore.rotation = 0f
                // step 2. 뷰 제거
                llSleepPlaylistClassic.removeViews(2, sampleClassicMusic.size-2)
            }
        }
        btnSleepPlaylistNavigateToSleepReport.setOnClickListener { findNavController().navigate(R.id.action_sleepPlaylistFragment_to_sleepReportFragment) }
    }

    private fun initViews() = with(binding) {
        // 백색소음
        for (i in 0..1) {
            val playlistView = LayoutSleepPlaylistBinding.inflate(
                LayoutInflater.from(context),
                llSleepPlaylistWhiteNoise,
                false
            )
            playlistView.ivSleepPlaylistItemThumbnail.setImageResource(sampleWhiteNoiseMusic[i].thumbnail)
            playlistView.tvSleepPlaylistItemTitle.text = sampleWhiteNoiseMusic[i].title
            playlistView.tvSleepPlaylistItemDuration.text = sampleWhiteNoiseMusic[i].duration
            playlistView.tvSleepPlaylistItemDescription.text = sampleWhiteNoiseMusic[i].description
            playlistView.ivSleepPlaylistItemPlay.setOnClickListener { findNavController().navigate(R.id.action_sleepPlaylistFragment_to_sleepMainFragment) }
            llSleepPlaylistWhiteNoise.addView(playlistView.root)
        }

        // 자연의 소리
        for (i in 0..1) {
            val playlistView = LayoutSleepPlaylistBinding.inflate(LayoutInflater.from(context), llSleepPlaylistNatureSounds, false)
            playlistView.ivSleepPlaylistItemThumbnail.setImageResource(sampleNatureSoundMusic[i].thumbnail)
            playlistView.tvSleepPlaylistItemTitle.text = sampleNatureSoundMusic[i].title
            playlistView.tvSleepPlaylistItemDuration.text = sampleNatureSoundMusic[i].duration
            playlistView.tvSleepPlaylistItemDescription.text = sampleNatureSoundMusic[i].description
            playlistView.ivSleepPlaylistItemPlay.setOnClickListener { findNavController().navigate(R.id.action_sleepPlaylistFragment_to_sleepMainFragment) }
            llSleepPlaylistNatureSounds.addView(playlistView.root)
        }

        // 클래식
        for (i in 0..1) {
            val playlistView = LayoutSleepPlaylistBinding.inflate(LayoutInflater.from(context), llSleepPlaylistClassic, false)
            playlistView.ivSleepPlaylistItemThumbnail.setImageResource(sampleClassicMusic[i].thumbnail)
            playlistView.tvSleepPlaylistItemTitle.text = sampleClassicMusic[i].title
            playlistView.tvSleepPlaylistItemDuration.text = sampleClassicMusic[i].duration
            playlistView.tvSleepPlaylistItemDescription.text = sampleClassicMusic[i].description
            playlistView.ivSleepPlaylistItemPlay.setOnClickListener { findNavController().navigate(R.id.action_sleepPlaylistFragment_to_sleepMainFragment) }
            llSleepPlaylistClassic.addView(playlistView.root)
        }
    }
}