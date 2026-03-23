package com.example.lifemaster.presentation.total.mypage.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.lifemaster.R

class PaymentMethodSelectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_method_selection)

        val amount = intent.getStringExtra(EXTRA_AMOUNT).orEmpty()
        val period = intent.getStringExtra(EXTRA_PERIOD).orEmpty()

        findViewById<TextView>(R.id.tvPaymentAmountValue).text = amount
        findViewById<TextView>(R.id.tvPaymentPeriodValue).text = period

        val paymentMethodGroup = findViewById<RadioGroup>(R.id.rgPaymentMethod)
        val btnPayNow = findViewById<Button>(R.id.btnPayNow)

        btnPayNow.setOnClickListener {
            val selectedMethodText = when (paymentMethodGroup.checkedRadioButtonId) {
                R.id.rbPaypal -> getString(R.string.payment_method_paypal)
                R.id.rbGooglePlay -> getString(R.string.payment_method_google_play)
                R.id.rbNaverPay -> getString(R.string.payment_method_naver_pay)
                else -> null
            }

            if (selectedMethodText == null) {
                Toast.makeText(this, R.string.payment_select_method_required, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(
                this,
                getString(R.string.payment_preparing_message, selectedMethodText),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    companion object {
        private const val EXTRA_AMOUNT = "extra_amount"
        private const val EXTRA_PERIOD = "extra_period"

        fun newIntent(context: Context, amount: String, period: String): Intent {
            return Intent(context, PaymentMethodSelectionActivity::class.java).apply {
                putExtra(EXTRA_AMOUNT, amount)
                putExtra(EXTRA_PERIOD, period)
            }
        }
    }
}
