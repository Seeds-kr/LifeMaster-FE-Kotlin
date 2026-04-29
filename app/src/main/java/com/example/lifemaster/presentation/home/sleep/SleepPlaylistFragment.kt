package com.example.lifemaster.presentation.home.sleep

import android.content.Context
import android.media.MediaPlayer
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

    private val sampleWhiteNoiseMusic = mutableListOf<SleepItem>()
    private val sampleNatureSoundMusic = mutableListOf<SleepItem>()
    private val sampleClassicMusic = mutableListOf<SleepItem>()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        initSampleData()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSleepPlaylistBinding.bind(view)
        initViews()
        initListeners()
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
                    audio = R.raw.sleep_test_music,
                    duration = getMusicDuration(R.raw.sleep_test_music),
                    thumbnail = R.drawable.tmp_sleep_playlist_white_noise_rain,
                    description = "부드럽고 잔잔한 빗소리가 마음을 차분하게 가라앉히고, 외부 소음을 덮어 깊은 집중과 편안한 수면을 유도합니다."
                ),
                SleepItem(
                    id = 2,
                    genre = MusicGenre.WHITE_NOISE,
                    title = "장작 타는 소리",
                    audio = R.raw.sleep_test_music,
                    duration = getMusicDuration(R.raw.sleep_test_music),
                    thumbnail = R.drawable.tmp_sleep_playlist_white_noise_fire,
                    description = "따스하게 타오르는 장작 소리가 아늑한 분위기를 조성하며, 심신의 긴장을 풀고 편안함을 더해줍니다."
                ),
                SleepItem(
                    id = 3,
                    genre = MusicGenre.WHITE_NOISE,
                    title = "비행기 기내 소리",
                    audio = R.raw.sleep_test_music,
                    duration = getMusicDuration(R.raw.sleep_test_music),
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
                    title = "맑은 낮의 새소리",
                    audio = R.raw.sleep_test_music,
                    duration = getMusicDuration(R.raw.sleep_test_music),
                    thumbnail = R.drawable.tmp_sleep_playlist_nature_sound_birds,
                    description = "햇살 가득한 숲속에서 들려오는 맑고 경쾌한 새소리가 기분을 상쾌하게 해주며 마음에 생기를 불어넣습니다."
                ),
                SleepItem(
                    id = 5,
                    genre = MusicGenre.NATURE_SOUND,
                    title = "고요한 저녁의 부엉이 소리",
                    audio = R.raw.sleep_test_music,
                    duration = getMusicDuration(R.raw.sleep_test_music),
                    thumbnail = R.drawable.tmp_sleep_playlist_nature_sound_owls,
                    description = "조용한 밤 숲속에서 들려오는 부엉이의 낮고 깊은 울음소리가 은은한 긴장감 속에 고요함과 차분함을 전해줍니다."
                ),
                SleepItem(
                    id = 6,
                    genre = MusicGenre.NATURE_SOUND,
                    title = "잔잔한 저녁의 풀벌레 소리",
                    audio = R.raw.sleep_test_music,
                    duration = getMusicDuration(R.raw.sleep_test_music),
                    thumbnail = R.drawable.tmp_sleep_playlist_nature_sound_crickets,
                    description = "밤이 찾아오면 들리는 풀벌레들의 규칙적인 울음소리는 자연의 리듬처럼 마음을 편안하게 진정시켜줍니다."
                )
            )
        )

        sampleClassicMusic.addAll(
            listOf(
                SleepItem(
                    id = 7,
                    genre = MusicGenre.CLASSIC,
                    title = "Erik Satie – Gymnopédie No.1",
                    audio = R.raw.sleep_test_music,
                    duration = getMusicDuration(R.raw.sleep_test_music),
                    thumbnail = R.drawable.tmp_sleep_playlist_classic_gymnopedie_no1,
                    description = "편안한 수면을 위한 선율, 느리고 잔잔한 리듬으로 마음을 안정시키고 깊은 휴식을 유도합니다."
                ),
                SleepItem(
                    id = 8,
                    genre = MusicGenre.CLASSIC,
                    title = "Claude Debussy – Clair de Lune",
                    audio = R.raw.sleep_test_music,
                    duration = getMusicDuration(R.raw.sleep_test_music),
                    thumbnail = R.drawable.tmp_sleep_playlist_classic_clair_de_lune,
                    description = "프랑스어로 ‘달빛’을 뜻하며, 달빛 아래 고요한 감성을 담아내고 은은한 선율로 긴장을 풀며 깊은 휴식을 유도합니다."
                ),
                SleepItem(
                    id = 9,
                    genre = MusicGenre.CLASSIC,
                    title = "Bach - Cello Suite No.1 Prelude",
                    audio = R.raw.sleep_test_music,
                    duration = getMusicDuration(R.raw.sleep_test_music),
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

        // 백색소음
        for (i in 0..1) {
            val playlistView = LayoutSleepPlaylistBinding.inflate(
                LayoutInflater.from(requireContext()),
                llSleepPlaylistWhiteNoise,
                false
            )
            playlistView.ivSleepPlaylistItemThumbnail.setImageResource(sampleWhiteNoiseMusic[i].thumbnail)
            playlistView.tvSleepPlaylistItemTitle.text = sampleWhiteNoiseMusic[i].title
            playlistView.tvSleepPlaylistItemDuration.text = sampleWhiteNoiseMusic[i].duration
            playlistView.tvSleepPlaylistItemDescription.text = sampleWhiteNoiseMusic[i].description

            val bundle = Bundle().apply {
                putString("title", sampleWhiteNoiseMusic[i].title)
                putInt("audio", sampleWhiteNoiseMusic[i].audio)
            }

            playlistView.ivSleepPlaylistItemPlay.setOnClickListener {
                findNavController().navigate(
                    R.id.action_sleepPlaylistFragment_to_sleepMainFragment,
                    bundle
                )
            }
            llSleepPlaylistWhiteNoise.addView(playlistView.root)
        }

        // 자연의 소리
        for (i in 0..1) {
            val playlistView = LayoutSleepPlaylistBinding.inflate(
                LayoutInflater.from(requireContext()),
                llSleepPlaylistNatureSounds,
                false
            )
            playlistView.ivSleepPlaylistItemThumbnail.setImageResource(sampleNatureSoundMusic[i].thumbnail)
            playlistView.tvSleepPlaylistItemTitle.text = sampleNatureSoundMusic[i].title
            playlistView.tvSleepPlaylistItemDuration.text = sampleNatureSoundMusic[i].duration
            playlistView.tvSleepPlaylistItemDescription.text = sampleNatureSoundMusic[i].description

            val bundle = Bundle().apply {
                putString("title", sampleNatureSoundMusic[i].title)
                putInt("audio", sampleNatureSoundMusic[i].audio)
            }

            playlistView.ivSleepPlaylistItemPlay.setOnClickListener {
                findNavController().navigate(
                    R.id.action_sleepPlaylistFragment_to_sleepMainFragment,
                    bundle
                )
            }
            llSleepPlaylistNatureSounds.addView(playlistView.root)
        }

        // 클래식
        for (i in 0..1) {
            val playlistView = LayoutSleepPlaylistBinding.inflate(
                LayoutInflater.from(requireContext()),
                llSleepPlaylistClassic,
                false
            )
            playlistView.ivSleepPlaylistItemThumbnail.setImageResource(sampleClassicMusic[i].thumbnail)
            playlistView.tvSleepPlaylistItemTitle.text = sampleClassicMusic[i].title
            playlistView.tvSleepPlaylistItemDuration.text = sampleClassicMusic[i].duration
            playlistView.tvSleepPlaylistItemDescription.text = sampleClassicMusic[i].description

            val bundle = Bundle().apply {
                putString("title", sampleClassicMusic[i].title)
                putInt("audio", sampleClassicMusic[i].audio)
            }

            playlistView.ivSleepPlaylistItemPlay.setOnClickListener {
                findNavController().navigate(
                    R.id.action_sleepPlaylistFragment_to_sleepMainFragment,
                    bundle
                )
            }
            llSleepPlaylistClassic.addView(playlistView.root)
        }
    }

    private fun initListeners() = with(binding) {
        llSleepPlaylistWhiteNoiseViewMore.setOnClickListener {
            if (tvSleepPlaylistWhiteNoiseViewMore.text == resources.getString(R.string.sleep_playlist_view_more)) {
                tvSleepPlaylistWhiteNoiseViewMore.text =
                    resources.getString(R.string.sleep_playlist_view_less)
                ivSleepPlaylistWhiteNoiseViewMore.rotation = 180f

                for (i in 2 until sampleWhiteNoiseMusic.size) {
                    val playlistView = LayoutSleepPlaylistBinding.inflate(
                        LayoutInflater.from(requireContext()),
                        llSleepPlaylistWhiteNoise,
                        false
                    )
                    playlistView.ivSleepPlaylistItemThumbnail.setImageResource(sampleWhiteNoiseMusic[i].thumbnail)
                    playlistView.tvSleepPlaylistItemTitle.text = sampleWhiteNoiseMusic[i].title
                    playlistView.tvSleepPlaylistItemDuration.text = sampleWhiteNoiseMusic[i].duration
                    playlistView.tvSleepPlaylistItemDescription.text = sampleWhiteNoiseMusic[i].description

                    val bundle = Bundle().apply {
                        putString("title", sampleWhiteNoiseMusic[i].title)
                        putInt("audio", sampleWhiteNoiseMusic[i].audio)
                    }

                    playlistView.ivSleepPlaylistItemPlay.setOnClickListener {
                        findNavController().navigate(
                            R.id.action_sleepPlaylistFragment_to_sleepMainFragment,
                            bundle
                        )
                    }
                    llSleepPlaylistWhiteNoise.addView(playlistView.root)
                }
            } else {
                tvSleepPlaylistWhiteNoiseViewMore.text =
                    resources.getString(R.string.sleep_playlist_view_more)
                ivSleepPlaylistWhiteNoiseViewMore.rotation = 0f
                llSleepPlaylistWhiteNoise.removeViews(2, sampleWhiteNoiseMusic.size - 2)
            }
        }

        llSleepPlaylistNatureSoundsViewMore.setOnClickListener {
            if (tvSleepPlaylistNatureSoundsViewMore.text == resources.getString(R.string.sleep_playlist_view_more)) {
                tvSleepPlaylistNatureSoundsViewMore.text =
                    resources.getString(R.string.sleep_playlist_view_less)
                ivSleepPlaylistNatureSoundsViewMore.rotation = 180f

                for (i in 2 until sampleNatureSoundMusic.size) {
                    val playlistView = LayoutSleepPlaylistBinding.inflate(
                        LayoutInflater.from(requireContext()),
                        llSleepPlaylistNatureSounds,
                        false
                    )
                    playlistView.ivSleepPlaylistItemThumbnail.setImageResource(sampleNatureSoundMusic[i].thumbnail)
                    playlistView.tvSleepPlaylistItemTitle.text = sampleNatureSoundMusic[i].title
                    playlistView.tvSleepPlaylistItemDuration.text = sampleNatureSoundMusic[i].duration
                    playlistView.tvSleepPlaylistItemDescription.text = sampleNatureSoundMusic[i].description

                    val bundle = Bundle().apply {
                        putString("title", sampleNatureSoundMusic[i].title)
                        putInt("audio", sampleNatureSoundMusic[i].audio)
                    }

                    playlistView.ivSleepPlaylistItemPlay.setOnClickListener {
                        findNavController().navigate(
                            R.id.action_sleepPlaylistFragment_to_sleepMainFragment,
                            bundle
                        )
                    }
                    llSleepPlaylistNatureSounds.addView(playlistView.root)
                }
            } else {
                tvSleepPlaylistNatureSoundsViewMore.text =
                    resources.getString(R.string.sleep_playlist_view_more)
                ivSleepPlaylistNatureSoundsViewMore.rotation = 0f
                llSleepPlaylistNatureSounds.removeViews(2, sampleNatureSoundMusic.size - 2)
            }
        }

        llSleepPlaylistClassicViewMore.setOnClickListener {
            if (tvSleepPlaylistClassicViewMore.text == resources.getString(R.string.sleep_playlist_view_more)) {
                tvSleepPlaylistClassicViewMore.text =
                    resources.getString(R.string.sleep_playlist_view_less)
                ivSleepPlaylistClassicViewMore.rotation = 180f

                for (i in 2 until sampleClassicMusic.size) {
                    val playlistView = LayoutSleepPlaylistBinding.inflate(
                        LayoutInflater.from(requireContext()),
                        llSleepPlaylistClassic,
                        false
                    )
                    playlistView.ivSleepPlaylistItemThumbnail.setImageResource(sampleClassicMusic[i].thumbnail)
                    playlistView.tvSleepPlaylistItemTitle.text = sampleClassicMusic[i].title
                    playlistView.tvSleepPlaylistItemDuration.text = sampleClassicMusic[i].duration
                    playlistView.tvSleepPlaylistItemDescription.text = sampleClassicMusic[i].description

                    val bundle = Bundle().apply {
                        putString("title", sampleClassicMusic[i].title)
                        putInt("audio", sampleClassicMusic[i].audio)
                    }

                    playlistView.ivSleepPlaylistItemPlay.setOnClickListener {
                        findNavController().navigate(
                            R.id.action_sleepPlaylistFragment_to_sleepMainFragment,
                            bundle
                        )
                    }
                    llSleepPlaylistClassic.addView(playlistView.root)
                }
            } else {
                tvSleepPlaylistClassicViewMore.text =
                    resources.getString(R.string.sleep_playlist_view_more)
                ivSleepPlaylistClassicViewMore.rotation = 0f
                llSleepPlaylistClassic.removeViews(2, sampleClassicMusic.size - 2)
            }
        }
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