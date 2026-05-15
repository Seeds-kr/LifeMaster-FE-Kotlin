package com.example.lifemaster

import android.content.Context
import android.content.Intent
import com.example.lifemaster.presentation.total.mypage.view.PremiumSubscribeActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object SubscriptionHelper {

    fun isPremium(context: Context): Boolean {
        val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        val plan = prefs.getString("subscriptionPlan", null)?.trim().orEmpty()
        val expDate = prefs.getString("expirationDate", null)?.trim().orEmpty()

        if (!plan.equals("PREMIUM", ignoreCase = true)) return false
        if (expDate == "9999-12-31") return true
        if (expDate.isBlank()) return false

        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
            val expirationDate = sdf.parse(expDate)
            val today = Date()
            // expirationDate가 오늘 이후이면 (오늘 포함하려면 !before 혹은 after)
            // 보통 만료일 2026-05-15 이면 15일까지는 되는게 맞음. 
            // .after(today)는 현재 시각 기준이라 당일에도 false가 될 수 있음.
            // 날짜만 비교하거나 끝 시간을 23:59:59로 잡는게 정확함.
            // 일단 간단하게 parse한 날짜(00:00:00)가 어제 이후이면 통과로 처리.
            val yesterday = Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000)
            expirationDate?.after(yesterday) ?: false
        } catch (_: Exception) {
            false
        }
    }

    fun checkPremiumAndRun(context: Context, action: () -> Unit) {
        if (isPremium(context)) {
            action()
        } else {
            context.startActivity(Intent(context, PremiumSubscribeActivity::class.java))
        }
    }
}
