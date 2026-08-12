package com.example.lifemaster.presentation.total.mypage.view

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.lifemaster.R
import com.example.lifemaster.SubscriptionHelper
import com.example.lifemaster.network.RetrofitInstance
import com.example.lifemaster.presentation.total.mypage.MyPageLocalStore
import com.example.lifemaster.presentation.total.mypage.model.PayPalCreateOrderResponse
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class PaymentMethodSelectionActivity : AppCompatActivity() {

    private lateinit var selectedPlanType: String
    private lateinit var btnPayNow: Button
    private var googleBillingManager: GoogleBillingManager? = null
    private var isGoogleBillingConnected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_method_selection)

        val amount = intent.getStringExtra(EXTRA_AMOUNT).orEmpty()
        val period = intent.getStringExtra(EXTRA_PERIOD).orEmpty()
        selectedPlanType = intent.getStringExtra(EXTRA_PLAN_TYPE) ?: PLAN_MONTHLY

        findViewById<TextView>(R.id.tvPaymentAmountValue).text = amount
        findViewById<TextView>(R.id.tvPaymentPeriodValue).text = period

        val btnPaymentPaypal = findViewById<LinearLayout>(R.id.btnPaymentPaypal)
        val btnPaymentGooglePlay = findViewById<LinearLayout>(R.id.btnPaymentGooglePlay)
        btnPayNow = findViewById<Button>(R.id.btnPayNow)
        findViewById<TextView>(R.id.tvRefundPolicy).setOnClickListener {
            startActivity(Intent(this, RefundPolicyActivity::class.java))
        }

        googleBillingManager = GoogleBillingManager(
            activity = this,
            coroutineScope = lifecycleScope,
            networkService = RetrofitInstance.networkService,
            authTokenProvider = ::getAuthHeaderOrNull,
            listener = object : GoogleBillingManager.Listener {
                override fun onConnected() {
                    isGoogleBillingConnected = true
                }

                override fun onPurchaseVerified() {
                    handleGooglePlayPurchaseSuccess()
                }

                override fun onError(message: String) {
                    btnPayNow.isEnabled = true
                    Toast.makeText(this@PaymentMethodSelectionActivity, message, Toast.LENGTH_SHORT).show()
                }
            }
        )
        googleBillingManager?.connect()

        var selectedPaymentOptionId: Int = View.NO_ID

        fun updatePaymentMethodSelection(selectedId: Int) {
            selectedPaymentOptionId = selectedId
            btnPaymentPaypal.setBackgroundResource(
                if (btnPaymentPaypal.id == selectedId) {
                    R.drawable.bg_payment_method_selected
                } else {
                    R.drawable.bg_payment_method_unselected
                }
            )
            btnPaymentGooglePlay.setBackgroundResource(
                if (btnPaymentGooglePlay.id == selectedId) {
                    R.drawable.bg_payment_method_selected
                } else {
                    R.drawable.bg_payment_method_unselected
                }
            )
        }

        btnPaymentPaypal.setOnClickListener { updatePaymentMethodSelection(R.id.btnPaymentPaypal) }
        btnPaymentGooglePlay.setOnClickListener { updatePaymentMethodSelection(R.id.btnPaymentGooglePlay) }

        btnPayNow.setOnClickListener {
            when (selectedPaymentOptionId) {
                R.id.btnPaymentPaypal -> startPaypalPayment()
                R.id.btnPaymentGooglePlay -> startGooglePlayPayment()
                else -> Toast.makeText(this, R.string.payment_select_method_required, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startPaypalPayment() {
        val authHeader = getAuthHeaderOrNull()
        if (authHeader.isNullOrBlank()) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        btnPayNow.isEnabled = false
        rememberSelectedPlan()

        lifecycleScope.launch {
            try {
                val res = RetrofitInstance.networkService.createPaypalOrder(authHeader)
                if (!res.isSuccessful) {
                    Toast.makeText(this@PaymentMethodSelectionActivity, "주문 생성 실패: ${res.code()}", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val body: PayPalCreateOrderResponse? = res.body()
                val approveUrl = body?.approveUrl
                if (approveUrl.isNullOrBlank()) {
                    Toast.makeText(this@PaymentMethodSelectionActivity, "PayPal 승인 URL을 가져올 수 없습니다.", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // 브라우저로 이동. 결제 완료 후 딥링크를 통해 MyPageActivity로 바로 이동하게 됨.
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(approveUrl)))
                finish() // 결제창을 띄우고 나면 이 화면은 종료 (MyPage로 바로 돌아오기 위함)
            } catch (e: Exception) {
                Toast.makeText(this@PaymentMethodSelectionActivity, "오류: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                btnPayNow.isEnabled = true
            }
        }
    }

    private fun startGooglePlayPayment() {
        val authHeader = getAuthHeaderOrNull()
        if (authHeader.isNullOrBlank()) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }
        if (!isGoogleBillingConnected) {
            Toast.makeText(this, "결제 서비스 연결 중입니다. 잠시 후 다시 시도해 주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        btnPayNow.isEnabled = false
        rememberSelectedPlan()
        googleBillingManager?.launchSubscription(googlePlayProductId(selectedPlanType))
    }

    private fun handleGooglePlayPurchaseSuccess() {
        btnPayNow.isEnabled = true

        val isAnnual = selectedPlanType == PLAN_ANNUAL

        // 1. 로컬 상태 즉시 반영 (연간 365일, 월간 30일 계산) - PayPal 흐름과 동일한 방식
        val cal = Calendar.getInstance(Locale.KOREA)
        cal.add(Calendar.DAY_OF_YEAR, if (isAnnual) 365 else 30)
        val expDate = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(cal.time)
        SubscriptionHelper.markPremiumActive(this, expDate)

        // 2. 결제 내역 로컬 기록 (서버 반영 전 임시)
        val typeLabel = if (isAnnual) "연간권" else "30일권"
        val price = if (isAnnual) {
            getString(R.string.subscription_price_annual_main)
        } else {
            getString(R.string.subscription_price_monthly_main)
        }
        MyPageLocalStore.appendPayment(this, "프리미엄 결제 (Google Play) · $typeLabel", price)

        Toast.makeText(this, R.string.payment_purchase_success, Toast.LENGTH_SHORT).show()

        // MyPageActivity.onResume()에서 서버 데이터로 다시 동기화됨
        setResult(RESULT_OK)
        finish()
    }

    private fun rememberSelectedPlan() {
        // 결제 시작 전 선택한 플랜 정보를 저장 (나중에 결과 처리 시 사용)
        getSharedPreferences("payment_prefs", Context.MODE_PRIVATE).edit()
            .putString("last_selected_plan_type", selectedPlanType)
            .apply()
    }

    private fun googlePlayProductId(planType: String): String =
        if (planType == PLAN_ANNUAL) GOOGLE_PRODUCT_ID_ANNUAL else GOOGLE_PRODUCT_ID_MONTHLY

    override fun onDestroy() {
        googleBillingManager?.release()
        super.onDestroy()
    }

    private fun getAuthHeaderOrNull(): String? {
        val rawToken = getSharedPreferences("auth", Context.MODE_PRIVATE).getString("token", null).orEmpty()
        if (rawToken.isBlank()) return null
        return if (rawToken.startsWith("Bearer ")) rawToken else "Bearer $rawToken"
    }

    companion object {
        private const val EXTRA_AMOUNT = "extra_amount"
        private const val EXTRA_PERIOD = "extra_period"
        private const val EXTRA_PLAN_TYPE = "extra_plan_type"
        const val PLAN_ANNUAL = "plan_annual"
        const val PLAN_MONTHLY = "plan_monthly"

        // Google Play Console에 등록한 구독 상품 ID와 반드시 일치해야 함
        private const val GOOGLE_PRODUCT_ID_ANNUAL = "premium_annual"
        private const val GOOGLE_PRODUCT_ID_MONTHLY = "premium_monthly"

        fun newIntent(context: Context, amount: String, period: String, planType: String): Intent {
            return Intent(context, PaymentMethodSelectionActivity::class.java).apply {
                putExtra(EXTRA_AMOUNT, amount)
                putExtra(EXTRA_PERIOD, period)
                putExtra(EXTRA_PLAN_TYPE, planType)
            }
        }
    }
}
