package com.example.lifemaster.presentation.home.sleep

data class SleepItem(
    val id: Int, // 곡 id
    val genre: MusicGenre,
    val thumbnail: Int,
    val title: String,
    val duration: String,
    val description: String
)

enum class MusicGenre {
    WHITE_NOISE,
    NATURE_SOUND,
    CLASSIC
}