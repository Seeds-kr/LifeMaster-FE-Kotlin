package com.example.lifemaster.presentation.home

object HomeConfig {
    /**
     * 홈 편집(HomeEditAdapter)과 동일. 항목별 아이콘 drawable은 ic_logo_star, 여기 값은 ColorFilter용 HEX.
     */
    val HOME_EDIT_ICON_TINT_BY_NAME: Map<String, String> = mapOf(
        "수면" to "#333333",
        "디톡스" to "#B4D775",
        "그룹 바로가기" to "#AC87CC",
        "자아성찰 바로가기" to "#FFB943",
        "챌린지" to "#6DABD9",
        "알람" to "#BBAB94"
    )

    val SERVICE_KEY_MAP = mapOf(
        "수면" to "sleep",
        "디톡스" to "detox",
        "그룹 바로가기" to "group",
        "자아성찰 바로가기" to "introspection",
        "알람" to "alarm",
        "챌린지" to "challenge"
    )
    val KEY_TO_NAME = SERVICE_KEY_MAP.entries.associate { it.value to it.key }
    val DEFAULT_VISIBLE = setOf("sleep", "group", "introspection")
    val DEFAULT_ORDER = listOf("sleep", "detox", "group", "introspection", "alarm", "challenge")
}
