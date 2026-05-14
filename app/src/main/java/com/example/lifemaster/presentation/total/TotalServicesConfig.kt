package com.example.lifemaster.presentation.total

import android.content.Context
import androidx.core.content.edit
import com.example.lifemaster.R
import com.example.lifemaster.presentation.home.HomeConfig

/**
 * 전체 탭의 서비스 목록 순서. 홈 화면 편집과 별도로 저장한다.
 */
object TotalServicesConfig {

    const val PREF_NAME = "total_pref"
    private const val KEY_ORDER = "total_service_order"

    const val KEY_ALARM = "alarm"
    const val KEY_SLEEP = "sleep"
    const val KEY_CHALLENGE = "challenge"
    const val KEY_INTROSPECTION_DIARY = "introspection_diary"
    const val KEY_INTROSPECTION_THANKS = "introspection_thanks"
    const val KEY_GROUP = "group"
    const val KEY_COMMUNITY = "community"

    val ALL_KEYS: List<String> = listOf(
        KEY_ALARM,
        KEY_SLEEP,
        KEY_CHALLENGE,
        KEY_INTROSPECTION_DIARY,
        KEY_INTROSPECTION_THANKS,
        KEY_GROUP,
        KEY_COMMUNITY
    )

    /** 편집·행 아이콘 색상용 [HomeConfig] 표시명 */
    private val tintNameByKey: Map<String, String> = mapOf(
        KEY_ALARM to "알람",
        KEY_SLEEP to "수면",
        KEY_CHALLENGE to "챌린지",
        KEY_INTROSPECTION_DIARY to "자아성찰 바로가기",
        KEY_INTROSPECTION_THANKS to "자아성찰 바로가기",
        KEY_GROUP to "그룹 바로가기",
        KEY_COMMUNITY to "커뮤니티"
    )

    fun titleResForKey(key: String): Int = when (key) {
        KEY_ALARM -> R.string.total_menu_alarm
        KEY_SLEEP -> R.string.sleep
        KEY_CHALLENGE -> R.string.challenge
        KEY_INTROSPECTION_DIARY -> R.string.total_menu_diary
        KEY_INTROSPECTION_THANKS -> R.string.total_menu_thanks
        KEY_GROUP -> R.string.group
        KEY_COMMUNITY -> R.string.total_menu_community
        else -> R.string.total_all_services
    }

    fun homeEditTintHexForKey(key: String): String {
        val name = tintNameByKey[key] ?: return "#333333"
        return HomeConfig.HOME_EDIT_ICON_TINT_BY_NAME[name] ?: "#333333"
    }

    fun loadOrder(context: Context): List<String> {
        val raw = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_ORDER, null)?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }
        return normalizeOrder(raw)
    }

    fun persistOrder(context: Context, order: List<String>) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit {
            putString(KEY_ORDER, order.joinToString(","))
        }
    }

    fun normalizeOrder(saved: List<String>?): List<String> {
        val known = ALL_KEYS.toSet()
        val filtered = saved?.filter { it in known } ?: emptyList()
        val missing = ALL_KEYS.filter { it !in filtered }
        return filtered + missing
    }
}
