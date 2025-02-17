package com.example.lifemaster.presentation.group.model

data class AlarmItem(
    val id: Int,
    val title: String,
    val time: Triple<String, Int, Int>,
    val randomMissions: List<RandomMissionType>,
    val randomMissionMathLevel: MathProblemLevel,
    val alarmRepeatDays: List<String>,
    val isDelaySet: Boolean,
    val delayMinute: Int = 0,
    val delayCount: Int = 0
)

enum class RandomMissionType {
    MATHEMATICAL_PROBLEM_SOLVING,
    TOUCH_ALONG,
    WRITE_ALONG,
}

enum class MathProblemLevel {
    HIGH,
    MEDIUM,
    LOW,
    NONE
}
