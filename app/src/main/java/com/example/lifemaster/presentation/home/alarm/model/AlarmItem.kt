package com.example.lifemaster.presentation.home.alarm.model

data class AlarmItem(
    val id: Int, // 1, 2, 3 ...
    val title: String, // 알람 제목
    val hour: Int, // 0 ~ 23
    val minute: Int, // 0 ~ 59
    var onOff: Boolean = true, // 처음 알람 등록하면 켜지도록 설정 (기본값)
    val randomMissions: List<RandomMissionType> = emptyList<RandomMissionType>(),
    val randomMissionMathLevel: MathProblemLevel = MathProblemLevel.NONE,
    val alarmRepeatDays: List<String> = emptyList<String>(),
    val isDelaySet: Boolean = false,
    val delayMinute: Int = 0,
    val delayCount: Int = 0
) {
    val timeText: String
        get() {
            val h = "%02d".format(
                if(hour < 12) hour else hour - 12
            )
            val m = "%02d".format(minute)
            return "$h:$m" // ex) 9:31
        }

    val ampmText: String
        get() {
            return if(hour < 12) "AM" else "PM"
        }

    fun makeDataForDB(): String {
        return "$hour:$minute" // ex) 15:31
    }
}

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
