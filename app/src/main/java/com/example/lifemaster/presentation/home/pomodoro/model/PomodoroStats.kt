package com.example.lifemaster.presentation.home.pomodoro.model

data class PomodoroRecentFocusListResponse(
    val items: List<PomodoroRecentFocusResponse>
)

data class PomodoroRecentFocusResponse(
    val date: String,
    val totalFocusMinutes: Int,
    val completedCount: Int,
    val averageFocusMinutes: Int,
    val focusLevel: String?
)

data class PomodoroStatsResponse(
    val date: String,
    val todayTotalFocusMinutes: Int,
    val focusMinutesDiff: Int,
    val completedCount: Int,
    val completedCountDiff: Int,
    val averageFocusMinutes: Int,
    val averageFocusMinutesDiff: Int,
    val weeklyTotalFocusMinutes: Int
)

data class PomodoroFocusLevelRequest(
    val date: String,
    val focusLevel: String
)