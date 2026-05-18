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
        // 1. Auth SharedPreferences 체크 (가장 최신 동기화 상태)
        if (isPremiumFromAuth(context)) return true
        
        // 2. MyPageLocalStore 체크 (결제 직후나 로컬 캐시 상태)
        if (hasLocalPremiumSummary(context)) return true

        // 3. (추가) 만약 이메일이나 닉네임에 'test'가 포함된 테스트 계정인 경우 프리미엄 허용 (개발용 편의)
        // val prefs = context.getSharedPreferences(AUTH_PREFS, Context.MODE_PRIVATE)
        // val email = prefs.getString("email", "").orEmpty()
        // if (email.startsWith("testuser", ignoreCase = true)) return true

        return false
    }

    fun checkPremiumAndRun(context: Context, action: () -> Unit) {
        if (isPremium(context)) {
            action()
        } else {
            context.startActivity(Intent(context, PremiumSubscribeActivity::class.java))
        }
    }

    fun isPremiumPlan(plan: String?): Boolean {
        val normalized = plan?.trim().orEmpty().uppercase(Locale.US)
        return normalized.contains("PREMIUM") ||
            normalized.contains("PAID") ||
            normalized.contains("PRO") ||
            normalized == "VIP" ||
            normalized == "GOLD"
    }

    /** 서버에서 받은 MeResponse 정보를 로컬(auth SharedPreferences)에 통합 저장 */
    fun saveAuthUserFromMe(context: Context, me: MeResponse) {
        context.getSharedPreferences(AUTH_PREFS, Context.MODE_PRIVATE).edit {
            val nick = (me.user?.nickName ?: me.nickName)?.trim().orEmpty()
            if (nick.isNotBlank() && nick != "null") {
                putString("nickname", nick)
                putString("nickName", nick)
            }
            val em = (me.user?.email ?: me.email)?.trim().orEmpty()
            if (em.isNotBlank() && em != "null") {
                putString("email", em)
            }
            val memberId = me.user?.id ?: me.id
            if (memberId > 0L) {
                putLong("memberId", memberId)
            }
            val url = (me.user?.profileImageUrl ?: me.profileImageUrl)?.trim().orEmpty()
            if (url.isNotBlank() && url != "null") {
                putString("profileImageUrl", url)
            }
            
            // 결제 상태 별도 저장
            val status = (me.user?.paymentStatus ?: me.paymentStatus)?.trim().orEmpty()
            if (status.isNotBlank()) {
                putString("paymentStatus", status)
            }
        }
        persistFromMe(context, me)
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
        
        // 플랜 이름 자체가 프리미엄인 경우
        if (isPremiumPlan(plan)) return true

        // 만약 플랜이 비어있거나 BASIC인데, 결제 상태(paymentStatus)가 별도로 저장되어 있다면 체크
        val status = prefs.getString("paymentStatus", null)?.trim().orEmpty()
        if (status.equals("PAID", ignoreCase = true)) return true

        return false
    }

    private fun hasLocalPremiumSummary(context: Context): Boolean {
        val summary = MyPageLocalStore.readSubscriptionSummary(context) ?: return false
        return isPremiumPlan(summary.first)
    }
}
