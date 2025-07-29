package com.example.lifemaster.presentation.home

object HomeConfig {
    val EDITABLE_SERVICE = listOf(
        "수면", "디톡스", "그룹 바로가기", "자아성찰 바로가기", "알람 추가", "챌린지"
    )
    val SERVICE_KEY_MAP = mapOf(
        "수면" to "sleep",
        "디톡스" to "detox",
        "그룹 바로가기" to "group",
        "자아성찰 바로가기" to "introspection",
        "알람 추가" to "alarm",
        "챌린지" to "challenge"
    )
    val KEY_TO_NAME = SERVICE_KEY_MAP.entries.associate { it.value to it.key }
    val DEFAULT_VISIBLE = setOf("sleep", "group", "introspection")
    val DEFAULT_ORDER = listOf("sleep", "detox", "group", "introspection", "alarm", "challenge")
}
