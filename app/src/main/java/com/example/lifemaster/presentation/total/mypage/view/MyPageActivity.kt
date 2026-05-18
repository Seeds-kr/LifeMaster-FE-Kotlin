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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class MyPageActivity : AppCompatActivity() {

    @Inject lateinit var tokenManager: TokenManager
    @Inject lateinit var networkService: NetworkService

    private var alertDialog: AlertDialog? = null
    private lateinit var backgroundDimmer: View
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

        val rootView = findViewById<ViewGroup>(android.R.id.content)
        backgroundDimmer = View(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(resources.getColor(R.color.black_dim, theme))
            visibility = View.GONE
        }
        rootView.addView(backgroundDimmer)

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
        // 로컬 상태 UI 반영 시에도 쿠폰 목록 정보가 없으면 기본으로 설정 (refreshMeFromServer에서 서버 체크 수행)
        applySubscriptionCard(null, null)
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
                
                // 오직 이번에 서버에서 받아온 데이터(me, serverCoupons)로만 구독 상태를 결정함 (로컬 캐시 무시)
                applySubscriptionCard(me, serverCoupons)
                
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
        val u = url?.trim().orEmpty()
        if (u.isBlank() || u == "null") {
            Glide.with(this).clear(ivProfile)
            ivProfile.setImageDrawable(null)
            ivProfilePlaceholder.visibility = View.VISIBLE
            return
        }
        ivProfilePlaceholder.visibility = View.GONE
        Glide.with(this)
            .load(u)
            .circleCrop()
            .into(ivProfile)
    }

    private fun applySubscriptionCard(me: MeResponse?, serverCoupons: List<com.example.lifemaster.presentation.total.mypage.model.CouponResponse>?) {
        val typeTv = findViewById<TextView>(R.id.tvSubscriptionType)
        val dateTv = findViewById<TextView>(R.id.tvSubscriptionDate)
        val btnSubscribe = findViewById<Button>(R.id.btnSubscribePremium)

        // 로컬 데이터는 무시하고, 이번에 서버에서 받아온 데이터들 중 가장 신뢰할 수 있는 정보를 찾음
        val planFromMe = me?.let { SubscriptionHelper.resolvePlan(it) }.orEmpty()
        val planFromCoupons = serverCoupons?.firstOrNull { it.user != null }?.user?.subscriptionPlan.orEmpty()
        val statusFromCoupons = serverCoupons?.firstOrNull { it.user != null }?.user?.paymentStatus.orEmpty()
        
        val isPremium = SubscriptionHelper.isPremiumPlan(planFromMe) || 
                         SubscriptionHelper.isPremiumPlan(planFromCoupons) ||
                         statusFromCoupons.equals("PAID", ignoreCase = true)

        if (isPremium) {
            // 프리미엄 상태를 로컬에도 동기화하여 다른 화면에서도 즉시 반영되도록 함
            val finalPlan = if (SubscriptionHelper.isPremiumPlan(planFromMe)) planFromMe else "PREMIUM"
            val expDate = me?.let { SubscriptionHelper.resolveExpirationDate(it) } ?: 
                           serverCoupons?.firstOrNull { it.user != null }?.user?.expirationDate ?: ""
            
            SubscriptionHelper.markPremiumActive(this, expDate.ifBlank { "9999-12-31" })

            typeTv.text = finalPlan.takeIf { it != "PAID" } ?: getString(R.string.mypage_premium)
            dateTv.text = expDate.ifBlank { "프리미엄 혜택 이용 중" }
            btnSubscribe.visibility = View.GONE
        } else {
            typeTv.text = getString(R.string.mypage_basic_plan_title)
            dateTv.text = getString(R.string.mypage_subscription_basic_detail)
            btnSubscribe.visibility = View.VISIBLE
        }
    }

    private fun bindPaymentHistory(
        serverPayments: List<com.example.lifemaster.presentation.total.mypage.model.PaymentHistoryResponse>?,
        serverCoupons: List<com.example.lifemaster.presentation.total.mypage.model.CouponResponse>?
    ) {
        llPaymentHistory.removeAllViews()
        
        val combinedList = mutableListOf<MyPageLocalStore.PaymentLine>()

        // 1. 서버 결제 내역 추가
        serverPayments?.forEach { s ->
            val at = parseDateToMillis(s.paymentDate)
            combinedList.add(MyPageLocalStore.PaymentLine(at, s.description ?: "프리미엄 구독", s.amount ?: ""))
        }
        
        // 2. 서버 쿠폰 목록을 "등록 내역"으로 변환하여 추가 (로컬 데이터 대체)
        serverCoupons?.forEach { c ->
            val at = parseDateToMillis(c.createdAt ?: c.updatedAt)
            val st = c.couponStatus.trim().uppercase()
            val statusLabel = if (st == "USE" || st == "USED") "사용 완료" else "등록 완료"
            combinedList.add(MyPageLocalStore.PaymentLine(at, "쿠폰 $statusLabel (${c.couponCode})", "확인"))
        }

        val sorted = combinedList
            .distinctBy { it.atMillis.toString() + it.description }
            .sortedByDescending { it.atMillis }

        if (sorted.isEmpty()) {
            llPaymentHistory.addView(TextView(this).apply {
                text = getString(R.string.mypage_payment_empty)
                setTextColor(0xFFA0A0A0.toInt())
                textSize = 14f
            })
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
