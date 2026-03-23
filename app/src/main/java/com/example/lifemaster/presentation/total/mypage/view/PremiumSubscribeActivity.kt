package com.example.lifemaster.presentation.total.mypage.view

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.lifemaster.R

/**
 * 프리미엄 구독 결제 화면 (UI만 구현, 실제 결제 기능 없음)
 */
class PremiumSubscribeActivity : AppCompatActivity() {

    private var isAnnualSelected = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_premium_subscribe)

        val cardAnnual = findViewById<ConstraintLayout>(R.id.cardAnnual)
        val cardMonthly = findViewById<ConstraintLayout>(R.id.cardMonthly)
        val iconAnnualCheck = findViewById<ImageView>(R.id.iconAnnualCheck)
        val iconMonthlyCheck = findViewById<ImageView>(R.id.iconMonthlyCheck)

        fun updateSelection() {
            if (isAnnualSelected) {
                cardAnnual.setBackgroundResource(R.drawable.bg_subscription_card_selected)
                cardMonthly.setBackgroundResource(R.drawable.bg_subscription_card_unselected)
                iconAnnualCheck.setImageResource(R.drawable.ic_subscription_check_selected)
                iconMonthlyCheck.setImageResource(R.drawable.ic_subscription_check_unselected)
            } else {
                cardAnnual.setBackgroundResource(R.drawable.bg_subscription_card_unselected)
                cardMonthly.setBackgroundResource(R.drawable.bg_subscription_card_selected)
                iconAnnualCheck.setImageResource(R.drawable.ic_subscription_check_unselected)
                iconMonthlyCheck.setImageResource(R.drawable.ic_subscription_check_selected)
            }
        }

        cardAnnual.setOnClickListener {
            isAnnualSelected = true
            updateSelection()
        }
        cardMonthly.setOnClickListener {
            isAnnualSelected = false
            updateSelection()
        }

        updateSelection()

        findViewById<TextView>(R.id.tvCouponEnter).setOnClickListener {
            // 쿠폰 입력 기능 추후 구현
        }

        findViewById<Button>(R.id.btnProceedPayment).setOnClickListener {
            val period = if (isAnnualSelected) getString(R.string.payment_period_annual) else getString(R.string.payment_period_monthly)
            val amount = if (isAnnualSelected) getString(R.string.subscription_price_annual_main) else getString(R.string.subscription_price_monthly_main)

            startActivity(
                PaymentMethodSelectionActivity.newIntent(
                    context = this,
                    amount = amount,
                    period = period
                )
            )
        }
    }
}
