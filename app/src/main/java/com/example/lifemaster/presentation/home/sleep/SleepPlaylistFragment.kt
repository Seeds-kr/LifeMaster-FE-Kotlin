package com.example.lifemaster.presentation.home.sleep

import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.lifemaster.R
import com.example.lifemaster.databinding.FragmentSleepPlaylistBinding
import com.example.lifemaster.databinding.LayoutSleepPlaylistBinding

class SleepPlaylistFragment : Fragment(R.layout.fragment_sleep_playlist) {

    lateinit var binding: FragmentSleepPlaylistBinding

    private val sampleWhiteNoiseMusic = mutableListOf<SleepItem>()
    private val sampleNatureSoundMusic = mutableListOf<SleepItem>()
    private val sampleClassicMusic = mutableListOf<SleepItem>()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        initSampleData()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!SleepFeatureGate.IS_ENABLED) {
            SleepFeatureGate.showUnavailableToast(requireContext())
            findNavController().popBackStack()
            return
        }
        binding = FragmentSleepPlaylistBinding.bind(view)
        initViews()
    }

    private fun initSampleData() {
        sampleWhiteNoiseMusic.clear()
        sampleNatureSoundMusic.clear()
        sampleClassicMusic.clear()

        sampleWhiteNoiseMusic.addAll(
            listOf(
                SleepItem(
                    id = 1,
                    genre = MusicGenre.WHITE_NOISE,
                    title = "잔잔한 빗소리",
                    audio = R.raw.rain_sound,
                    duration = getMusicDuration(R.raw.rain_sound),
                    thumbnail = R.drawable.tmp_sleep_playlist_white_noise_rain,
                    description = "부드럽고 잔잔한 빗소리가 마음을 차분하게 가라앉히고, 외부 소음을 덮어 깊은 집중과 편안한 수면을 유도합니다."
                ),
                SleepItem(
                    id = 2,
                    genre = MusicGenre.WHITE_NOISE,
                    title = "장작 타는 소리",
                    audio = R.raw.fire_sound,
                    duration = getMusicDuration(R.raw.fire_sound),
                    thumbnail = R.drawable.tmp_sleep_playlist_white_noise_fire,
                    description = "따스하게 타오르는 장작 소리가 아늑한 분위기를 조성하며, 심신의 긴장을 풀고 편안함을 더해줍니다."
                ),
                SleepItem(
                    id = 3,
                    genre = MusicGenre.WHITE_NOISE,
                    title = "비행기 기내 소리",
                    audio = R.raw.airplane_sound,
                    duration = getMusicDuration(R.raw.airplane_sound),
                    thumbnail = R.drawable.tmp_sleep_playlist_white_noise_plane,
                    description = "낮고 부드럽게 퍼지는 비행기 엔진 소리가 일정한 리듬을 만들어, 불안한 생각을 가라앉히고 안정적인 수면 환경을 조성합니다."
                )
            )
        )

        sampleNatureSoundMusic.addAll(
            listOf(
                SleepItem(
                    id = 4,
                    genre = MusicGenre.NATURE_SOUND,
                    title = "맑은 낮의 새소리와 풍경 소리",
                    audio = R.raw.morning_forest_sound,
                    duration = getMusicDuration(R.raw.morning_forest_sound),
                    thumbnail = R.drawable.tmp_sleep_playlist_nature_sound_birds,
                    description = "햇살 가득한 숲속에서 들려오는 맑은 새소리와 풍경 소리가 기분을 상쾌하게 해주며 마음에 생기를 불어넣습니다."
                ),
                SleepItem(
                    id = 5,
                    genre = MusicGenre.NATURE_SOUND,
                    title = "잔잔한 저녁의 풀벌레 소리",
                    audio = R.raw.night_forest_sound,
                    duration = getMusicDuration(R.raw.night_forest_sound),
                    thumbnail = R.drawable.tmp_sleep_playlist_nature_sound_crickets,
                    description = "밤이 찾아오면 들리는 풀벌레들의 규칙적인 울음소리는 자연의 리듬처럼 마음을 편안하게 진정시켜줍니다."
                )
            )
        )

        sampleClassicMusic.addAll(
            listOf(
                SleepItem(
                    id = 6,
                    genre = MusicGenre.CLASSIC,
                    title = "Erik Satie – Gymnopédie No.1",
                    audio = R.raw.gymnopedie_no1,
                    duration = getMusicDuration(R.raw.gymnopedie_no1),
                    thumbnail = R.drawable.tmp_sleep_playlist_classic_gymnopedie_no1,
                    description = "편안한 수면을 위한 선율, 느리고 잔잔한 리듬으로 마음을 안정시키고 깊은 휴식을 유도합니다."
                ),
                SleepItem(
                    id = 7,
                    genre = MusicGenre.CLASSIC,
                    title = "Bach - Cello Suite No.1 Prelude",
                    audio = R.raw.bach_cello_suite_no1,
                    duration = getMusicDuration(R.raw.bach_cello_suite_no1),
                    thumbnail = R.drawable.tmp_sleep_playlist_classic_cello_suite_no_1_prelude,
                    description = "간결하면서도 우아한 선율이 마음을 차분하게 만들고, 부드럽고 반복적인 리듬이 안정감을 주어 긴장을 풀고 깊은 휴식과 평화를 선사합니다."
                )
            )
        )
    }

    private fun initViews() = with(binding) {
        llSleepPlaylistWhiteNoise.removeAllViews()
        llSleepPlaylistNatureSounds.removeAllViews()
        llSleepPlaylistClassic.removeAllViews()

        // "더보기" 버튼 숨기기
        llSleepPlaylistWhiteNoiseViewMore.visibility = View.GONE
        llSleepPlaylistNatureSoundsViewMore.visibility = View.GONE
        llSleepPlaylistClassicViewMore.visibility = View.GONE

        // 모든 아이템을 리스트에 추가
        sampleWhiteNoiseMusic.forEach { addPlaylistItem(llSleepPlaylistWhiteNoise, it) }
        sampleNatureSoundMusic.forEach { addPlaylistItem(llSleepPlaylistNatureSounds, it) }
        sampleClassicMusic.forEach { addPlaylistItem(llSleepPlaylistClassic, it) }
    }

    private fun addPlaylistItem(container: LinearLayout, item: SleepItem) {
        val playlistView = LayoutSleepPlaylistBinding.inflate(
            LayoutInflater.from(requireContext()),
            container,
            false
        )
        with(playlistView) {
            ivSleepPlaylistItemThumbnail.setImageResource(item.thumbnail)
            tvSleepPlaylistItemTitle.text = item.title
            tvSleepPlaylistItemDuration.text = item.duration
            tvSleepPlaylistItemDescription.text = item.description

            val bundle = Bundle().apply {
                putString("title", item.title)
                putInt("audio", item.audio)
            }

            ivSleepPlaylistItemPlay.setOnClickListener {
                findNavController().navigate(
                    R.id.action_sleepPlaylistFragment_to_sleepMainFragment,
                    bundle
                )
            }
        }
        container.addView(playlistView.root)
    }

    private fun getMusicDuration(musicResource: Int): String {
        val mediaPlayer = MediaPlayer.create(requireContext(), musicResource) ?: return "00:00:00"

        return try {
            val totalSeconds = mediaPlayer.duration / 1000
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } catch (e: Exception) {
            "00:00:00"
        } finally {
            mediaPlayer.release()
        }
    }
}