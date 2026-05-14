package com.example.lifemaster.presentation.total.mypage

import android.content.Context
import com.example.lifemaster.network.TokenProvider
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 서버에 결제 내역 API가 없을 때, 앱 내 결제 완료 시점에만 로컬에 쌓는 결제 기록.
 * 회원별로 SharedPreferences 키를 나눕니다.
 */
object MyPageLocalStore {

    private const val PREF = "mypage_state"
    private const val PAYMENTS_PREFIX = "payments_v1_"
    private const val SUB_TITLE_PREFIX = "sub_title_v1_"
    private const val SUB_DETAIL_PREFIX = "sub_detail_v1_"
    private const val MAX_ENTRIES = 40

    data class PaymentLine(
        val atMillis: Long,
        val description: String,
        val amountLabel: String,
    )

    private fun sp(context: Context) =
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)

    private fun memberSuffix(context: Context): String {
        val id = TokenProvider.getMemberId(context)
        return if (id != null && id > 0L) id.toString() else "0"
    }

    fun appendPayment(context: Context, description: String, amountLabel: String) {
        val key = PAYMENTS_PREFIX + memberSuffix(context)
        val arr = JSONArray(sp(context).getString(key, "[]"))
        val next = JSONArray()
        next.put(
            JSONObject().apply {
                put("at", System.currentTimeMillis())
                put("d", description)
                put("a", amountLabel)
            },
        )
        val keep = minOf(MAX_ENTRIES - 1, arr.length())
        for (i in 0 until keep) {
            next.put(arr.getJSONObject(i))
        }
        sp(context).edit().putString(key, next.toString()).apply()
    }

    fun readPayments(context: Context): List<PaymentLine> {
        val key = PAYMENTS_PREFIX + memberSuffix(context)
        val raw = sp(context).getString(key, "[]") ?: "[]"
        return runCatching {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val o = arr.optJSONObject(i) ?: continue
                    add(
                        PaymentLine(
                            atMillis = o.optLong("at"),
                            description = o.optString("d"),
                            amountLabel = o.optString("a"),
                        ),
                    )
                }
            }.sortedByDescending { it.atMillis }
        }.getOrElse { emptyList() }
    }

    fun setSubscriptionSummary(context: Context, title: String, detail: String) {
        val s = memberSuffix(context)
        sp(context).edit()
            .putString(SUB_TITLE_PREFIX + s, title)
            .putString(SUB_DETAIL_PREFIX + s, detail)
            .apply()
    }

    fun readSubscriptionSummary(context: Context): Pair<String, String>? {
        val s = memberSuffix(context)
        val title = sp(context).getString(SUB_TITLE_PREFIX + s, null)?.trim().orEmpty()
        val detail = sp(context).getString(SUB_DETAIL_PREFIX + s, null)?.trim().orEmpty()
        if (title.isBlank() && detail.isBlank()) return null
        return title to detail
    }

    fun formatPaymentRowDate(atMillis: Long): String {
        if (atMillis <= 0L) return ""
        return SimpleDateFormat("M월 d일", Locale.KOREA).format(Date(atMillis))
    }
}
