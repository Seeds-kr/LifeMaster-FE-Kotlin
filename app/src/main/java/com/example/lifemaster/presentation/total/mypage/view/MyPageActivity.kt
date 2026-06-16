package com.example.lifemaster.presentation.total.mypage.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.lifemaster.R
import com.example.lifemaster.SubscriptionHelper
import com.example.lifemaster.network.NetworkService
import com.example.lifemaster.network.TokenManager
import com.example.lifemaster.presentation.login.view.LoginActivity
import com.example.lifemaster.presentation.total.mypage.MyPageLocalStore
import com.example.lifemaster.presentation.total.mypage.model.MeResponse
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class MyPageActivity : AppCompatActivity() {

    @Inject lateinit var tokenManager: TokenManager
    @Inject lateinit var networkService: NetworkService

    private var alertDialog: AlertDialog? = null
    private lateinit var ivProfile: ImageView
    private lateinit var ivProfilePlaceholder: ImageView
    private lateinit var llPaymentHistory: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_my_page)

        ivProfile = findViewById(R.id.ivProfile)
        ivProfilePlaceholder = findViewById(R.id.ivProfilePlaceholder)
        llPaymentHistory = findViewById(R.id.llPaymentHistory)

        val tvEdit = findViewById<TextView>(R.id.tvEdit)
        val btnSubscribePremium = findViewById<Button>(R.id.btnSubscribePremium)
        val tvLogout = findViewById<TextView>(R.id.tvLogout)
        val tvWithdrawal = findViewById<TextView>(R.id.tvWithdrawal)
        val tvCouponLabel = findViewById<TextView>(R.id.tvCouponLabel)
        val ivCouponArrow = findViewById<ImageView>(R.id.ivCouponArrow)

        tvEdit.setOnClickListener {
            Toast.makeText(this, "프로필 수정 클릭", Toast.LENGTH_SHORT).show()
        }

        btnSubscribePremium.setOnClickListener {
            startActivity(Intent(this, PremiumSubscribeActivity::class.java))
        }

        tvLogout.setOnClickListener {
            logout()
        }

        tvWithdrawal.setOnClickListener {
            // 서버 연동 전 임시 토스트 (임의의 탈퇴 기능 연동 원복)
            Toast.makeText(this, "회원 탈퇴는 고객센터에 문의해주세요.", Toast.LENGTH_SHORT).show()
        }

        val openCoupons = View.OnClickListener {
            startActivity(Intent(this, CouponRegisterActivity::class.java))
        }
        tvCouponLabel.setOnClickListener(openCoupons)
        ivCouponArrow.setOnClickListener(openCoupons)
        
        // 초기 실행 시 딥링크 처리
        intent?.let { handleDeepLink(it) }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent) // 중요한 정보를 담고 있을 수 있으므로 업데이트
        intent?.let { handleDeepLink(it) }
    }

    private fun handleDeepLink(intent: Intent) {
        val data = intent.data ?: return
        android.util.Log.d("PAYMENT_DEEP_LINK", "Received: $data")
        
        if (data.scheme == "lifemaster" && data.host == "payment") {
            val orderId = data.getQueryParameter("token") ?: data.getQueryParameter("orderId")
            
            when (data.path) {
                "/success" -> {
                    if (!orderId.isNullOrBlank()) {
                        lifecycleScope.launch {
                            // 로딩 인디케이터나 토스트로 사용자에게 알림
                            Toast.makeText(this@MyPageActivity, "결제 승인을 확인하고 있습니다. 잠시만 기다려주세요...", Toast.LENGTH_LONG).show()
                            delay(2000)
                            checkAndCaptureOrder(orderId)
                        }
                    }
                }
                "/fail", "/cancel" -> {
                    Toast.makeText(this, "결제가 취소되었습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private suspend fun checkAndCaptureOrder(orderId: String) {
        val bearer = tokenManager.getBearerToken() ?: return
        
        try {
            // 1. 이미 프리미엄인지 먼저 확인
            val meRes = withContext(Dispatchers.IO) { runCatching { networkService.getMe(bearer) }.getOrNull() }
            if (meRes?.isSuccessful == true) {
                val me = meRes.body()
                if (me != null && SubscriptionHelper.isPremiumPlan(SubscriptionHelper.resolvePlan(me))) {
                    handleCaptureSuccess()
                    return
                }
            }

            // 2. 서버에 결제 승인 요청 (Capture)
            val response = withContext(Dispatchers.IO) {
                runCatching { networkService.capturePaypalOrder(bearer, orderId) }.getOrNull()
            }

            if (response?.isSuccessful == true) {
                handleCaptureSuccess()
            } else {
                showCaptureError(response?.code() ?: -1, response?.errorBody()?.string())
            }
        } catch (e: Exception) {
            showCaptureError(-1, e.message)
        }
    }

    private fun handleCaptureSuccess() {
        Toast.makeText(this, R.string.payment_purchase_success, Toast.LENGTH_SHORT).show()
        
        // 결제 시 선택했던 플랜 정보 확인
        val payPrefs = getSharedPreferences("payment_prefs", MODE_PRIVATE)
        val lastPlanType = payPrefs.getString("last_selected_plan_type", "plan_monthly")
        val isAnnual = lastPlanType == "plan_annual"

        // 1. 로컬 상태 즉시 반영 (연간 365일, 월간 30일 계산)
        val cal = java.util.Calendar.getInstance(java.util.Locale.KOREA)
        cal.add(java.util.Calendar.DAY_OF_YEAR, if (isAnnual) 365 else 30)
        val expDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.KOREA).format(cal.time)
        SubscriptionHelper.markPremiumActive(this, expDate)
        
        // 2. 결제 내역 로컬 기록 (서버 반영 전 임시)
        val typeLabel = if (isAnnual) "연간권" else "30일권"
        val desc = "프리미엄 결제 (PayPal) · $typeLabel"
        val price = if (isAnnual) "₩41,200" else "₩4,900"
        MyPageLocalStore.appendPayment(this, desc, price)
        
        // 3. 서버 데이터 동기화
        refreshMeFromServer()
    }

    private fun showCaptureError(code: Int, errorBody: String?) {
        val detail = if (errorBody?.contains("ORDER_NOT_APPROVED", ignoreCase = true) == true) {
            "\n(페이팔 미승인 - 페이팔 페이지에서 '지금 결제' 버튼을 끝까지 눌렀는지 확인해 주세요)"
        } else if (errorBody?.contains("422", ignoreCase = true) == true || errorBody?.contains("UNPROCESSABLE_ENTITY", ignoreCase = true) == true) {
            "\n(결제 처리 대기 중이거나 승인이 완료되지 않았습니다. 잠시 후 다시 시도해 주세요.)"
        } else if (!errorBody.isNullOrBlank()) {
            val msgMatch = Regex("\"message\"\\s*:\\s*\"([^\"]+)\"").find(errorBody)
            if (msgMatch != null) "\n(${msgMatch.groupValues[1]})" else "\n($errorBody)"
        } else ""
        
        Toast.makeText(this, "결제 승인 확인 중 오류가 발생했습니다. ($code)$detail", Toast.LENGTH_LONG).show()
    }

    override fun onResume() {
        super.onResume()
        applyLocalAuthToUi()
        refreshMeFromServer()
    }

    private fun applyLocalAuthToUi() {
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        findViewById<TextView>(R.id.tvNickname).text = resolveNickname(prefs)
        findViewById<TextView>(R.id.tvEmail).text = resolveEmail(prefs)
        showProfileImage(prefs.getString("profileImageUrl", null))
        // 로컬 상태 UI 반영 시에도 쿠폰/결제 목록 정보가 없으면 기본으로 설정
        applySubscriptionCard(null, null, null)
        bindPaymentHistory(null, null)
    }

    private fun resolveNickname(prefs: android.content.SharedPreferences): String {
        val n = prefs.getString("nickname", null)?.trim()?.takeIf { it.isNotBlank() && it != "null" }
            ?: prefs.getString("nickName", null)?.trim()?.takeIf { it.isNotBlank() && it != "null" }
        
        if (n != null) return n
        
        val email = resolveEmail(prefs)
        if (email.contains("@") && email != getString(R.string.mypage_email_sample)) {
            return email.substringBefore("@")
        }
        return getString(R.string.mypage_nickname_sample)
    }

    private fun resolveEmail(prefs: android.content.SharedPreferences): String {
        val e = prefs.getString("email", null)?.trim().orEmpty()
            .ifBlank { prefs.getString("loginEmail", null)?.trim().orEmpty() }
        if (e.isNotBlank()) return e
        return getString(R.string.mypage_email_sample)
    }

    private fun refreshMeFromServer() {
        val bearer = tokenManager.getBearerToken() ?: return
        lifecycleScope.launch {
            // 1. 서버에서 데이터 3종 실시간 조회 (진실의 원천)
            val meRes = withContext(Dispatchers.IO) { runCatching { networkService.getMe(bearer) }.getOrNull() }
            val paymentRes = withContext(Dispatchers.IO) {
                val memberId = meRes?.body()?.let { it.user?.id ?: it.id } ?: tokenManager.getMemberId() ?: 0L
                runCatching { networkService.getPaymentHistory(bearer, memberId) }.getOrNull()
            }
            val couponRes = withContext(Dispatchers.IO) { runCatching { networkService.getMyCoupons(bearer) }.getOrNull() }

            val me = meRes?.body()?.takeIf { meRes.isSuccessful }
            val serverPayments = if (paymentRes?.isSuccessful == true) paymentRes.body() else null
            val serverCoupons = if (couponRes?.isSuccessful == true) couponRes.body() else null

            withContext(Dispatchers.Main) {
                if (me != null) {
                    SubscriptionHelper.saveAuthUserFromMe(this@MyPageActivity, me)
                    findViewById<TextView>(R.id.tvNickname).text = displayNick(me)
                    findViewById<TextView>(R.id.tvEmail).text = displayEmail(me)
                    showProfileImage((me.user?.profileImageUrl ?: me.profileImageUrl))
                } else {
                    applyLocalAuthToUi()
                }
                
                // 오직 이번에 서버에서 받아온 데이터로만 구독 상태를 결정함
                applySubscriptionCard(me, serverCoupons, serverPayments)
                
                // 오직 서버 데이터(결제 내역 + 쿠폰 목록)로만 리스트 구성
                bindPaymentHistory(serverPayments, serverCoupons)
            }
        }
    }

    private fun displayNick(me: MeResponse): String {
        val n = (me.user?.nickName ?: me.nickName)?.trim()
            ?.takeIf { it.isNotBlank() && it != "null" }
        if (n != null) return n

        val email = (me.user?.email ?: me.email)?.trim()
        if (!email.isNullOrBlank() && email.contains("@") && email != getString(R.string.mypage_email_sample)) {
            return email.substringBefore("@")
        }

        return resolveNickname(getSharedPreferences("auth", Context.MODE_PRIVATE))
    }

    private fun displayEmail(me: MeResponse): String {
        val e = (me.user?.email ?: me.email)?.trim().orEmpty()
        if (e.isNotBlank()) return e
        return resolveEmail(getSharedPreferences("auth", Context.MODE_PRIVATE))
    }

    private fun showProfileImage(url: String?) {
        val cleanUrl = url?.trim()?.takeIf { it.isNotBlank() && it != "null" }
        if (cleanUrl == null) {
            ivProfilePlaceholder.visibility = View.VISIBLE
            ivProfile.setImageDrawable(null)
        } else {
            ivProfilePlaceholder.visibility = View.GONE
            Glide.with(this)
                .load(cleanUrl)
                .circleCrop()
                .into(ivProfile)
        }
    }

    private fun applySubscriptionCard(
        me: MeResponse?,
        serverCoupons: List<com.example.lifemaster.presentation.total.mypage.model.CouponResponse>?,
        serverPayments: List<com.example.lifemaster.presentation.total.mypage.model.PaymentHistoryResponse>?
    ) {
        val typeTv = findViewById<TextView>(R.id.tvSubscriptionType)
        val dateTv = findViewById<TextView>(R.id.tvSubscriptionDate)
        val btnSubscribe = findViewById<Button>(R.id.btnSubscribePremium)
        val planTitleTv = findViewById<TextView>(R.id.tvPlanTitle)
        val cvBasic = findViewById<View>(R.id.cvBasicPlan)
        val cvPremium = findViewById<View>(R.id.cvPremiumPlan)

        // 1. 프리미엄 여부 판단 (서버 데이터 최우선)
        val planFromMe = me?.let { SubscriptionHelper.resolvePlan(it) }.orEmpty()
        var isPremium = SubscriptionHelper.isPremiumPlan(planFromMe)

        // 2. 서버 데이터에서 추가 확인
        if (!isPremium) {
            val planFromCoupons = serverCoupons?.firstOrNull { it.user != null }?.user?.subscriptionPlan.orEmpty()
            val statusFromCoupons = serverCoupons?.firstOrNull { it.user != null }?.user?.paymentStatus.orEmpty()
            
            val hasPaidRecord = serverPayments?.any { 
                val stat = it.status.orEmpty().uppercase()
                (stat == "COMPLETED" || stat == "SUCCESS" || stat == "PAID")
            } ?: false
            
            isPremium = SubscriptionHelper.isPremiumPlan(planFromMe) || 
                         SubscriptionHelper.isPremiumPlan(planFromCoupons) ||
                         statusFromCoupons.equals("PAID", ignoreCase = true) ||
                         hasPaidRecord ||
                         SubscriptionHelper.isPremium(this)
        }

        if (isPremium) {
            val prefs = getSharedPreferences("auth", MODE_PRIVATE)
            val finalPlan = if (SubscriptionHelper.isPremiumPlan(planFromMe)) planFromMe else "PREMIUM"
            
            // 만료일 결정 우선순위
            var expDate = me?.let { SubscriptionHelper.resolveExpirationDate(it) }?.takeIf { it.isNotBlank() }
                           ?: serverCoupons?.firstOrNull { it.user != null }?.user?.expirationDate?.takeIf { it.isNotBlank() }
                           ?: ""

            // 서버 응답에 날짜가 없는 경우, 결제 내역(서버+로컬)에서 추정
            if (expDate.isBlank()) {
                val allPayments = (serverPayments?.map { 
                    MyPageLocalStore.PaymentLine(parseDateToMillis(it.paymentDate), it.description.orEmpty(), it.amount.orEmpty())
                }.orEmpty() + MyPageLocalStore.readPayments(this)).filter { 
                    val d = it.description.uppercase()
                    d.contains("PREMIUM") || d.contains("프리미엄") || d.contains("구독") || d.contains("PAYPAL")
                }
                
                val latestPaid = allPayments.maxByOrNull { it.atMillis }
                if (latestPaid != null) {
                    val cal = java.util.Calendar.getInstance()
                    cal.timeInMillis = latestPaid.atMillis
                    val isAnnual = latestPaid.description.contains("연간") || latestPaid.amountLabel.contains("41,200")
                    cal.add(java.util.Calendar.DAY_OF_YEAR, if (isAnnual) 365 else 30)
                    expDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.KOREA).format(cal.time)
                }
            }

            if (expDate.isBlank()) {
                expDate = prefs.getString("expirationDate", null)?.takeIf { it.isNotBlank() } ?: ""
            }
            
            // 프리미엄인데 만료일이 도저히 안 나오면 오늘부터 30일 뒤로라도 표시
            if (expDate.isBlank()) {
                val cal = java.util.Calendar.getInstance()
                cal.add(java.util.Calendar.DAY_OF_YEAR, 30)
                expDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.KOREA).format(cal.time)
            }

            val displayExp = if (expDate != "9999-12-31") {
                val cleanDate = expDate.take(10).replace("-", ".")
                "현재 이용 중 (만료일: $cleanDate)"
            } else {
                "무제한 이용권"
            }

            typeTv.text = finalPlan.takeIf { it != "PAID" } ?: getString(R.string.mypage_premium)
            dateTv.text = displayExp
            
            // 이미 구독 중이면 구독 유도 버튼만 숨김
            btnSubscribe.visibility = View.GONE
            planTitleTv?.visibility = View.VISIBLE
            cvBasic?.visibility = View.VISIBLE
            cvPremium?.visibility = View.VISIBLE
        } else {
            typeTv.text = getString(R.string.mypage_basic_plan_title)
            dateTv.text = getString(R.string.mypage_subscription_basic_detail)
            btnSubscribe.visibility = View.VISIBLE
            planTitleTv?.visibility = View.VISIBLE
            cvBasic?.visibility = View.VISIBLE
            cvPremium?.visibility = View.VISIBLE
        }
    }

    private fun bindPaymentHistory(
        serverPayments: List<com.example.lifemaster.presentation.total.mypage.model.PaymentHistoryResponse>?,
        serverCoupons: List<com.example.lifemaster.presentation.total.mypage.model.CouponResponse>?
    ) {
        llPaymentHistory.removeAllViews()
        
        // 로컬 + 서버 데이터 통합
        val combinedList = MyPageLocalStore.readPayments(this).toMutableList()

        serverPayments?.forEach { s ->
            val at = parseDateToMillis(s.paymentDate)
            val rawDesc = s.description ?: "프리미엄 결제"
            val amt = s.amount ?: ""

            // 상품 타입 추론 (연간/30일)
            val isAnnual = rawDesc.contains("ANNUAL", ignoreCase = true) || 
                           rawDesc.contains("YEAR", ignoreCase = true) || 
                           rawDesc.contains("연간", ignoreCase = true) || 
                           amt.contains("41200") || amt.contains("41,200")

            val isMonthly = rawDesc.contains("MONTH", ignoreCase = true) || 
                            rawDesc.contains("월간", ignoreCase = true) ||
                            rawDesc.contains("30일", ignoreCase = true) ||
                            amt.contains("4900") || amt.contains("4,900") ||
                            rawDesc.contains("PREMIUM", ignoreCase = true)

            val typeSuffix = when {
                isAnnual -> " [연간권]"
                isMonthly -> " [30일권]"
                else -> ""
            }

            var finalDesc = rawDesc
            if (typeSuffix.isNotEmpty() && !finalDesc.contains("권]")) {
                finalDesc = "$finalDesc$typeSuffix"
            }

            combinedList.add(MyPageLocalStore.PaymentLine(at, finalDesc, s.amount ?: "₩4,900"))
        }
        
        serverCoupons?.forEach { c ->
            val at = parseDateToMillis(c.createdAt ?: c.updatedAt)
            val st = c.couponStatus.trim().uppercase()
            val statusLabel = if (st == "USE" || st == "USED") "사용 완료" else "등록 완료"
            combinedList.add(MyPageLocalStore.PaymentLine(at, "쿠폰 $statusLabel (${c.couponCode}) [무제한]", "확인"))
        }

        val sorted = combinedList
            .filter { it.atMillis > 0 }
            .distinctBy { (it.atMillis / 1000).toString() + it.description }
            .sortedByDescending { it.atMillis }

        if (sorted.isEmpty()) {
            val emptyTv = TextView(this).apply {
                text = getString(R.string.mypage_payment_empty)
                setTextColor(0xFFA0A0A0.toInt())
                textSize = 14f
                gravity = android.view.Gravity.CENTER
                setPadding(0, 40, 0, 40)
            }
            llPaymentHistory.addView(emptyTv)
            return
        }
        
        val inflater = LayoutInflater.from(this)
        for (line in sorted) {
            val row = inflater.inflate(R.layout.item_payment_history_row, llPaymentHistory, false)
            row.findViewById<TextView>(R.id.tvPaymentDate).text = MyPageLocalStore.formatPaymentRowDate(line.atMillis)
            row.findViewById<TextView>(R.id.tvPaymentDesc).text = line.description
            row.findViewById<TextView>(R.id.tvPaymentAmount).text = line.amountLabel
            llPaymentHistory.addView(row)
        }
    }

    private fun parseDateToMillis(dateStr: String?): Long {
        if (dateStr.isNullOrBlank()) return 0L
        return runCatching {
            java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.KOREA).parse(dateStr)?.time ?: 0L
        }.getOrElse {
            runCatching {
                java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.KOREA).parse(dateStr.take(10))?.time ?: 0L
            }.getOrDefault(0L)
        }
    }

    private fun logout() {
        tokenManager.clear()
        Toast.makeText(this, "로그아웃되었습니다.", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}
