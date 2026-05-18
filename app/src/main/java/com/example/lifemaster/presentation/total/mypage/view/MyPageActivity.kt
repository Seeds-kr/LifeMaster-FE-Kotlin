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
        setContentView(R.layout.activity_my_page)

        ivProfile = findViewById(R.id.ivProfile)
        ivProfilePlaceholder = findViewById(R.id.ivProfilePlaceholder)
        llPaymentHistory = findViewById(R.id.llPaymentHistory)

        val tvEdit = findViewById<TextView>(R.id.tvEdit)
        val btnSubscribePremium = findViewById<Button>(R.id.btnSubscribePremium)
        val tvLogout = findViewById<TextView>(R.id.tvLogout)
        val tvWithdrawal = findViewById<TextView>(R.id.tvWithdrawal)

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
            showWithdrawalDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        applyLocalAuthToUi()
        bindPaymentHistory()
        refreshMeFromServer()
    }

    private fun applyLocalAuthToUi() {
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        findViewById<TextView>(R.id.tvNickname).text = resolveNickname(prefs)
        findViewById<TextView>(R.id.tvEmail).text = resolveEmail(prefs)
        showProfileImage(prefs.getString("profileImageUrl", null))
        applySubscriptionCard(me = null)
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
            val me = withContext(Dispatchers.IO) {
                runCatching { networkService.getMe(bearer) }.getOrNull()
            }?.takeIf { it.isSuccessful }?.body() ?: return@launch

            persistMe(me)
            withContext(Dispatchers.Main) {
                findViewById<TextView>(R.id.tvNickname).text = displayNick(me)
                findViewById<TextView>(R.id.tvEmail).text = displayEmail(me)
                val url = (me.user?.profileImageUrl ?: me.profileImageUrl)?.trim()?.takeIf { it.isNotEmpty() }
                    ?: getSharedPreferences("auth", Context.MODE_PRIVATE).getString("profileImageUrl", null)
                showProfileImage(url)
                applySubscriptionCard(me)
                bindPaymentHistory()
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

    private fun persistMe(me: MeResponse) {
        getSharedPreferences("auth", Context.MODE_PRIVATE).edit {
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
        }
        SubscriptionHelper.persistFromMe(this, me)
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

    private fun applySubscriptionCard(me: MeResponse?) {
        val typeTv = findViewById<TextView>(R.id.tvSubscriptionType)
        val dateTv = findViewById<TextView>(R.id.tvSubscriptionDate)
        val btnSubscribe = findViewById<Button>(R.id.btnSubscribePremium)

        // 서버 응답(me)에서 프리미엄 여부 확인
        val mePlan = me?.let { SubscriptionHelper.resolvePlan(it) }
        val meIsPremium = mePlan?.let { SubscriptionHelper.isPremiumPlan(it) } ?: false

        if (meIsPremium || SubscriptionHelper.isPremium(this)) {
            // 서버 플랜이 PREMIUM이면 해당 정보를 우선 표시
            if (meIsPremium) {
                val apiDesc = (me.user?.subscriptionDescription ?: me.subscriptionDescription
                    ?: me.user?.expirationDate ?: me.expirationDate)?.trim().orEmpty()
                typeTv.text = getString(R.string.mypage_premium)
                dateTv.text = apiDesc.ifBlank { "프리미엄 혜택 이용 중" }
            } else {
                // 서버 응답이 없거나 PREMIUM이 아니지만, 로컬 캐시/결제 내역이 있는 경우
                val local = MyPageLocalStore.readSubscriptionSummary(this)
                typeTv.text = local?.first?.ifBlank { getString(R.string.mypage_premium) }
                    ?: getString(R.string.mypage_premium)
                dateTv.text = local?.second?.ifBlank { "프리미엄 혜택 이용 중" }
                    ?: "프리미엄 혜택 이용 중"
            }
            btnSubscribe.visibility = View.GONE
            return
        }

        typeTv.text = getString(R.string.mypage_basic_plan_title)
        dateTv.text = getString(R.string.mypage_subscription_basic_detail)
        btnSubscribe.visibility = View.VISIBLE
    }

    private fun bindPaymentHistory() {
        llPaymentHistory.removeAllViews()
        val items = MyPageLocalStore.readPayments(this)
        if (items.isEmpty()) {
            llPaymentHistory.addView(
                TextView(this).apply {
                    text = getString(R.string.mypage_payment_empty)
                    setTextColor(0xFFA0A0A0.toInt())
                    textSize = 14f
                }
            )
            return
        }
        val inflater = LayoutInflater.from(this)
        for (line in items) {
            val row = inflater.inflate(R.layout.item_payment_history_row, llPaymentHistory, false)
            row.findViewById<TextView>(R.id.tvPaymentDate).text =
                MyPageLocalStore.formatPaymentRowDate(line.atMillis)
            row.findViewById<TextView>(R.id.tvPaymentDesc).text = line.description
            row.findViewById<TextView>(R.id.tvPaymentAmount).text = line.amountLabel
            llPaymentHistory.addView(row)
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

    private fun showWithdrawalDialog() {
        val builder = AlertDialog.Builder(this)
        val customLayout = layoutInflater.inflate(R.layout.dialog_confirm_withdrawal, null)
        builder.setView(customLayout)
        builder.setCancelable(false)

        alertDialog = builder.create()
        alertDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnCancelDialog = customLayout.findViewById<Button>(R.id.btnCancelDialog)
        val btnWithdrawalDialog = customLayout.findViewById<Button>(R.id.btnWithdrawalDialog)

        btnCancelDialog.setOnClickListener {
            alertDialog?.dismiss()
            backgroundDimmer.visibility = View.GONE
        }

        btnWithdrawalDialog.setOnClickListener {
            alertDialog?.dismiss()
            backgroundDimmer.visibility = View.GONE
            Toast.makeText(this, "탈퇴 처리가 완료되었습니다.", Toast.LENGTH_SHORT).show()
            logout()
        }

        backgroundDimmer.visibility = View.VISIBLE
        alertDialog?.show()
    }
}
