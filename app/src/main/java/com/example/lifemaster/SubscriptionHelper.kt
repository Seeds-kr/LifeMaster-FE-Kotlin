package com.example.lifemaster

import android.content.Context
import android.content.Intent
import androidx.core.content.edit
import com.example.lifemaster.presentation.total.mypage.MyPageLocalStore
import com.example.lifemaster.presentation.total.mypage.model.MeResponse
import com.example.lifemaster.presentation.total.mypage.view.PremiumSubscribeActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object SubscriptionHelper {

    private const val AUTH_PREFS = "auth"
    private const val KEY_PLAN = "subscriptionPlan"
    private const val KEY_EXPIRATION = "expirationDate"
    private const val UNLIMITED_DATE = "9999-12-31"

    fun resolvePlan(me: MeResponse): String {
        // paymentStatus가 PAID면 무조건 PREMIUM으로 간주 (최우선순위)
        val status = (me.user?.paymentStatus ?: me.paymentStatus)?.trim().orEmpty()
        if (status.equals("PAID", ignoreCase = true)) return "PREMIUM"

        val plan = (me.user?.subscriptionPlan ?: me.subscriptionPlan)?.trim().orEmpty()
        if (plan.isNotBlank()) return plan
        
        return ""
    }

    fun resolveExpirationDate(me: MeResponse): String =
        (me.user?.expirationDate ?: me.expirationDate
            ?: me.user?.subscriptionDescription ?: me.subscriptionDescription)
            ?.trim()
            .orEmpty()

    fun persistFromMe(context: Context, me: MeResponse) {
        val plan = resolvePlan(me)
        val exp = resolveExpirationDate(me)
        persistLocal(context, plan, exp)
    }

    fun persistFromUserData(context: Context, userData: com.example.lifemaster.presentation.total.mypage.model.UserData) {
        val plan = userData.subscriptionPlan?.trim().orEmpty().ifBlank { 
            if (userData.paymentStatus?.trim().equals("PAID", ignoreCase = true)) "PREMIUM" else ""
        }
        val exp = (userData.expirationDate ?: userData.subscriptionDescription)?.trim().orEmpty()
        persistLocal(context, plan, exp)
    }

    private fun persistLocal(context: Context, plan: String, exp: String) {
        context.getSharedPreferences(AUTH_PREFS, Context.MODE_PRIVATE).edit {
            if (plan.isNotBlank()) {
                putString(KEY_PLAN, plan)
                if (isPremiumPlan(plan) && exp.isBlank()) {
                    remove(KEY_EXPIRATION)
                }
            }
            if (exp.isNotBlank()) putString(KEY_EXPIRATION, exp)
        }
    }

    /** 결제·쿠폰 등으로 프리미엄이 활성화된 직후 auth 캐시에도 반영 */
    fun markPremiumActive(context: Context, expirationDate: String = UNLIMITED_DATE) {
        context.getSharedPreferences(AUTH_PREFS, Context.MODE_PRIVATE).edit {
            putString(KEY_PLAN, "PREMIUM")
            putString(KEY_EXPIRATION, expirationDate)
        }
    }

    fun isPremium(context: Context): Boolean {
        if (isPremiumFromAuth(context)) return true
        return hasLocalPremiumSummary(context)
    }

    fun checkPremiumAndRun(context: Context, action: () -> Unit) {
        if (isPremium(context)) {
            action()
        } else {
            context.startActivity(Intent(context, PremiumSubscribeActivity::class.java))
        }
    }

    fun isPremiumPlan(plan: String?): Boolean {
        val normalized = plan?.trim().orEmpty()
        return normalized.equals("PREMIUM", ignoreCase = true) ||
            normalized.equals("Premium", ignoreCase = true) ||
            normalized.equals("PAID", ignoreCase = true)
    }

    fun isExpired(dateStr: String?): Boolean {
        val raw = dateStr?.trim().orEmpty()
        if (raw.isBlank()) return false
        val dateOnly = raw.take(10)
        if (dateOnly == UNLIMITED_DATE) return false

        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).apply { isLenient = false }
            val expiration = sdf.parse(dateOnly) ?: return false
            val today = Calendar.getInstance(Locale.KOREA).apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time
            expiration.before(today)
        } catch (_: Exception) {
            false
        }
    }

    private fun isPremiumFromAuth(context: Context): Boolean {
        val prefs = context.getSharedPreferences(AUTH_PREFS, Context.MODE_PRIVATE)
        val plan = prefs.getString(KEY_PLAN, null)?.trim().orEmpty()
        if (!isPremiumPlan(plan)) return false

        // 플랜이 PREMIUM인 경우, 서버가 명시적으로 BASIC으로 내리기 전까지는 프리미엄으로 간주합니다.
        // 만료일 체크는 UI에서 안내용으로만 사용하도록 정책 변경 (유저 보고 내용 반영)
        return true
    }

    private fun hasLocalPremiumSummary(context: Context): Boolean {
        val summary = MyPageLocalStore.readSubscriptionSummary(context) ?: return false
        return isPremiumPlan(summary.first)
    }
}
