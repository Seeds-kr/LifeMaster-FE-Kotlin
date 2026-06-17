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

    private lateinit var selectedPlanType: String
    private lateinit var btnPayNow: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_method_selection)

        val amount = intent.getStringExtra(EXTRA_AMOUNT).orEmpty()
        val period = intent.getStringExtra(EXTRA_PERIOD).orEmpty()
        selectedPlanType = intent.getStringExtra(EXTRA_PLAN_TYPE) ?: PLAN_MONTHLY

        findViewById<TextView>(R.id.tvPaymentAmountValue).text = amount
        findViewById<TextView>(R.id.tvPaymentPeriodValue).text = period

        val btnPaymentPaypal = findViewById<LinearLayout>(R.id.btnPaymentPaypal)
        btnPayNow = findViewById<Button>(R.id.btnPayNow)
        findViewById<TextView>(R.id.tvRefundPolicy).setOnClickListener {
            startActivity(Intent(this, RefundPolicyActivity::class.java))
        }

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
        }

        btnPaymentPaypal.setOnClickListener { updatePaymentMethodSelection(R.id.btnPaymentPaypal) }

        btnPayNow.setOnClickListener {
            if (selectedPaymentOptionId != R.id.btnPaymentPaypal) {
                Toast.makeText(this, R.string.payment_select_method_required, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val authHeader = getAuthHeaderOrNull()
            if (authHeader.isNullOrBlank()) {
                Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnPayNow.isEnabled = false
            
            // 결제 시작 전 선택한 플랜 정보를 저장 (나중에 MyPageActivity에서 결과 처리 시 사용)
            getSharedPreferences("payment_prefs", Context.MODE_PRIVATE).edit()
                .putString("last_selected_plan_type", selectedPlanType)
                .apply()

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

        fun newIntent(context: Context, amount: String, period: String, planType: String): Intent {
            return Intent(context, PaymentMethodSelectionActivity::class.java).apply {
                putExtra(EXTRA_AMOUNT, amount)
                putExtra(EXTRA_PERIOD, period)
                putExtra(EXTRA_PLAN_TYPE, planType)
            }
        }
    }
}
