package com.example.lifemaster.presentation.total.mypage.view

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.lifecycle.lifecycleScope
import com.example.lifemaster.R
import com.example.lifemaster.network.NetworkService
import com.example.lifemaster.network.TokenManager
import com.example.lifemaster.presentation.total.mypage.MyPageLocalStore
import com.example.lifemaster.presentation.total.mypage.model.CouponResponse
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class CouponUseActivity : AppCompatActivity() {

    @Inject lateinit var networkService: NetworkService
    @Inject lateinit var tokenManager: TokenManager

    private var coupon: CouponResponse? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coupon_use)

        coupon = intent.getSerializableExtra("coupon") as? CouponResponse

        if (coupon == null) {
            Toast.makeText(this, getString(R.string.coupon_error_load), Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupUI()
    }

    private fun setupUI() {
        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            finish()
        }

        findViewById<TextView>(R.id.tvCouponCode).text = coupon?.couponCode
        findViewById<TextView>(R.id.tvCouponType).text = coupon?.couponType
        
        // 사용 기한 표시
        findViewById<TextView>(R.id.tvExpirationDate).text = getString(R.string.coupon_expiration_format, "9999-12-31")

        val btnUse = findViewById<AppCompatButton>(R.id.btnUseCoupon)
        if (coupon?.couponStatus == "USE") {
            btnUse.isEnabled = false
            btnUse.text = "사용 중인 쿠폰입니다"
            btnUse.setBackgroundColor(android.graphics.Color.LTGRAY)
        } else {
            btnUse.setOnClickListener {
                useCoupon()
            }
        }
    }

    private fun useCoupon() {
        val token = tokenManager.getBearerToken() ?: return
        val couponCode = coupon?.couponCode ?: return

        lifecycleScope.launch {
            try {
                val response = networkService.useCoupon(
                    token = token,
                    body = mapOf("couponCode" to couponCode)
                )
                if (response.isSuccessful) {
                    Toast.makeText(this@CouponUseActivity, getString(R.string.coupon_use_success), Toast.LENGTH_SHORT).show()
                    
                    // 로컬 스토어에 구독 정보 및 결제 내역 업데이트
                    saveCouponUsageLocally()

                    setResult(RESULT_OK)
                    finish()
                } else {
                    Toast.makeText(this@CouponUseActivity, getString(R.string.coupon_use_fail), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@CouponUseActivity, getString(R.string.server_error_message), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveCouponUsageLocally() {
        val today = SimpleDateFormat("yyyy.MM.dd", Locale.KOREA).format(Date())
        // 1. 구독 정보를 Premium으로 변경
        MyPageLocalStore.setSubscriptionSummary(
            context = this,
            title = "Premium",
            detail = "$today 쿠폰으로 활성화됨"
        )
        // 2. 결제 내역에 추가
        MyPageLocalStore.appendPayment(
            context = this,
            description = "Premium 쿠폰 사용 (${coupon?.couponCode})",
            amountLabel = "0원"
        )
    }
}
