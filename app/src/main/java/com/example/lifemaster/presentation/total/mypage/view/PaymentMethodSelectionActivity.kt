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
import com.example.lifemaster.network.RetrofitInstance
import com.example.lifemaster.presentation.total.mypage.model.PayPalCreateOrderResponse
import kotlinx.coroutines.launch

class PaymentMethodSelectionActivity : AppCompatActivity() {

    private lateinit var billingManager: GoogleBillingManager
    private lateinit var selectedPlanType: String
    private lateinit var btnPayNow: Button

    // PayPal 흐름용
    private var pendingPaypalOrderId: String? = null
    private var shouldCapturePaypalOnResume: Boolean = false
    private var isCapturingPaypal: Boolean = false

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
        val btnPaymentNaverPay = findViewById<LinearLayout>(R.id.btnPaymentNaverPay)
        btnPayNow = findViewById<Button>(R.id.btnPayNow)
        findViewById<TextView>(R.id.tvRefundPolicy).setOnClickListener {
            startActivity(Intent(this, RefundPolicyActivity::class.java))
        }

        var selectedPaymentOptionId: Int = View.NO_ID

        fun updatePaymentMethodSelection(selectedId: Int) {
            selectedPaymentOptionId = selectedId
            listOf(btnPaymentPaypal, btnPaymentGooglePlay, btnPaymentNaverPay).forEach { row ->
                row.setBackgroundResource(
                    if (row.id == selectedId) {
                        R.drawable.bg_payment_method_selected
                    } else {
                        R.drawable.bg_payment_method_unselected
                    }
                )
            }
        }

        btnPaymentPaypal.setOnClickListener { updatePaymentMethodSelection(R.id.btnPaymentPaypal) }
        btnPaymentGooglePlay.setOnClickListener { updatePaymentMethodSelection(R.id.btnPaymentGooglePlay) }
        btnPaymentNaverPay.setOnClickListener { updatePaymentMethodSelection(R.id.btnPaymentNaverPay) }
        billingManager = GoogleBillingManager(
            activity = this,
            listener = object : GoogleBillingManager.Listener {
                override fun onConnected() {
                    // 연결 성공 시 사용자 액션에서 결제창 호출
                }

                override fun onPurchaseSuccess() {
                    Toast.makeText(
                        this@PaymentMethodSelectionActivity,
                        R.string.payment_purchase_success,
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onError(message: String) {
                    Toast.makeText(this@PaymentMethodSelectionActivity, message, Toast.LENGTH_SHORT).show()
                }
            }
        )
        billingManager.connect()

        btnPayNow.setOnClickListener {
            val selectedMethodText = when (selectedPaymentOptionId) {
                R.id.btnPaymentPaypal -> getString(R.string.payment_method_paypal)
                R.id.btnPaymentGooglePlay -> getString(R.string.payment_method_google_play)
                R.id.btnPaymentNaverPay -> getString(R.string.payment_method_naver_pay)
                else -> {
                    Toast.makeText(this, R.string.payment_select_method_required, Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }
            when (selectedMethodText) {
                getString(R.string.payment_method_google_play) -> {
                    val productId = if (selectedPlanType == PLAN_ANNUAL) {
                        GOOGLE_SUBS_ANNUAL_PRODUCT_ID
                    } else {
                        GOOGLE_SUBS_MONTHLY_PRODUCT_ID
                    }
                    billingManager.launchSubscription(productId)
                }

                getString(R.string.payment_method_paypal),
                // TODO: 네이버페이는 현재 "준비 중" 상태
                getString(R.string.payment_method_naver_pay) -> {
                    if (selectedMethodText == getString(R.string.payment_method_paypal)) {
                        val authHeader = getAuthHeaderOrNull()
                        if (authHeader.isNullOrBlank()) {
                            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }

                        val existingOrderId = pendingPaypalOrderId
                        if (!existingOrderId.isNullOrBlank()) {
                            // 이미 orderId가 있으면, create-order를 다시 호출하지 않고 capture만 수행
                            capturePaypalOrder(
                                authHeader = authHeader,
                                orderId = existingOrderId,
                            )
                            return@setOnClickListener
                        }

                        btnPayNow.isEnabled = false
                        lifecycleScope.launch {
                            try {
                                val res = RetrofitInstance.networkService.createPaypalOrder(authHeader)
                                if (!res.isSuccessful) {
                                    Toast.makeText(
                                        this@PaymentMethodSelectionActivity,
                                        "PayPal create-order 실패: ${res.code()}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    return@launch
                                }

                                val body: PayPalCreateOrderResponse? = res.body()
                                val orderId = body?.orderId
                                val approveUrl = body?.approveUrl
                                if (orderId.isNullOrBlank() || approveUrl.isNullOrBlank()) {
                                    Toast.makeText(
                                        this@PaymentMethodSelectionActivity,
                                        "PayPal 응답이 올바르지 않습니다.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    return@launch
                                }

                                pendingPaypalOrderId = orderId
                                shouldCapturePaypalOnResume = true
                                // 승인/캡처 시도 전이므로 새로 capture 중복 방지 상태를 초기화
                                isCapturingPaypal = false

                                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(approveUrl)))

                                Toast.makeText(
                                    this@PaymentMethodSelectionActivity,
                                    "브라우저에서 승인 후 앱으로 돌아오면 결제를 확인합니다.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } catch (e: Exception) {
                                Toast.makeText(
                                    this@PaymentMethodSelectionActivity,
                                    "PayPal create-order 중 오류: ${e.message ?: "unknown"}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } finally {
                                // 브라우저 승인 페이지로 이동했더라도 Activity가 pause 되므로 크게 문제는 없지만,
                                // resumed 이후에도 안전하게 버튼 상태를 복구합니다.
                                btnPayNow.isEnabled = true
                            }
                        }
                    } else {
                        Toast.makeText(
                            this,
                            getString(R.string.payment_preparing_message, selectedMethodText),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        val orderId = pendingPaypalOrderId
        if (!shouldCapturePaypalOnResume || orderId.isNullOrBlank()) return
        if (isCapturingPaypal) return

        val authHeader = getAuthHeaderOrNull()
        if (authHeader.isNullOrBlank()) return

        shouldCapturePaypalOnResume = false
        capturePaypalOrder(
            authHeader = authHeader,
            orderId = orderId,
        )
    }

    private fun getAuthHeaderOrNull(): String? {
        val rawToken = getSharedPreferences("auth", Context.MODE_PRIVATE).getString("token", null).orEmpty()
        if (rawToken.isBlank()) return null
        return if (rawToken.startsWith("Bearer ")) rawToken else "Bearer $rawToken"
    }

    private fun capturePaypalOrder(
        authHeader: String,
        orderId: String,
    ) {
        if (isCapturingPaypal) return
        isCapturingPaypal = true
        btnPayNow.isEnabled = false

        lifecycleScope.launch {
            try {
                val res = RetrofitInstance.networkService.capturePaypalOrder(authHeader, orderId)
                if (res.isSuccessful) {
                    Toast.makeText(
                        this@PaymentMethodSelectionActivity,
                        R.string.payment_purchase_success,
                        Toast.LENGTH_SHORT
                    ).show()
                    pendingPaypalOrderId = null
                } else {
                    Toast.makeText(
                        this@PaymentMethodSelectionActivity,
                        "PayPal capture 실패: ${res.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                    // capture는 실패할 수 있음(승인 타이밍 등). 사용자가 다시 눌러 재시도할 수 있게 pending 값은 유지합니다.
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@PaymentMethodSelectionActivity,
                    "PayPal capture 중 오류: ${e.message ?: "unknown"}",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                isCapturingPaypal = false
                btnPayNow.isEnabled = true
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        billingManager.release()
    }

    companion object {
        private const val EXTRA_AMOUNT = "extra_amount"
        private const val EXTRA_PERIOD = "extra_period"
        private const val EXTRA_PLAN_TYPE = "extra_plan_type"
        const val PLAN_ANNUAL = "plan_annual"
        const val PLAN_MONTHLY = "plan_monthly"
        // TODO: Play Console에 등록한 실제 상품 ID로 변경
        private const val GOOGLE_SUBS_ANNUAL_PRODUCT_ID = "premium_annual"
        private const val GOOGLE_SUBS_MONTHLY_PRODUCT_ID = "premium_monthly"

        fun newIntent(context: Context, amount: String, period: String, planType: String): Intent {
            return Intent(context, PaymentMethodSelectionActivity::class.java).apply {
                putExtra(EXTRA_AMOUNT, amount)
                putExtra(EXTRA_PERIOD, period)
                putExtra(EXTRA_PLAN_TYPE, planType)
            }
        }
    }
}
