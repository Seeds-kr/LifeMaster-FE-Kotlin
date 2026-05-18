package com.example.lifemaster.presentation.total.mypage.view

import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.lifemaster.R
import com.example.lifemaster.SubscriptionHelper
import com.example.lifemaster.network.NetworkService
import com.example.lifemaster.network.TokenManager
import com.example.lifemaster.presentation.total.mypage.adapter.CouponAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Response
import javax.inject.Inject

@AndroidEntryPoint
class CouponRegisterActivity : AppCompatActivity() {

    @Inject lateinit var networkService: NetworkService
    @Inject lateinit var tokenManager: TokenManager

    private lateinit var rvMyCoupons: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_coupon_register)

        val btnBack = findViewById<ImageView>(R.id.btn_back)
        val etCouponCode = findViewById<EditText>(R.id.etCouponCode)
        val btnRegisterCoupon = findViewById<AppCompatButton>(R.id.btnRegisterCoupon)
        rvMyCoupons = findViewById(R.id.rvMyCoupons)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)

        rvMyCoupons.layoutManager = LinearLayoutManager(this)

        btnBack.setOnClickListener {
            finish()
        }

        btnRegisterCoupon.setOnClickListener {
            val code = etCouponCode.text.toString().trim()
            if (code.isEmpty()) {
                Toast.makeText(this, "쿠폰 번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            registerCoupon(code)
        }

        swipeRefreshLayout.setOnRefreshListener {
            fetchMyCoupons(isManual = true)
        }

        fetchMyCoupons()
    }

    override fun onResume() {
        super.onResume()
        fetchMyCoupons()
    }

    private fun registerCoupon(code: String) {
        val token = tokenManager.getBearerToken()
        if (token == null) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val response: Response<ResponseBody> = networkService.registerCoupon(
                    token = token,
                    body = mapOf("couponCode" to code)
                )

                if (response.isSuccessful) {
                    Toast.makeText(this@CouponRegisterActivity, getString(R.string.coupon_register_success), Toast.LENGTH_SHORT).show()
                    findViewById<EditText>(R.id.etCouponCode).text.clear()
                    
                    // 결제 내역(MyPageLocalStore)에 등록 내역 기록
                    com.example.lifemaster.presentation.total.mypage.MyPageLocalStore.appendPayment(
                        this@CouponRegisterActivity, 
                        "쿠폰 등록 ($code)", 
                        "확인"
                    )

                    fetchMyCoupons()
                } else if (response.code() == 401) {
                    Toast.makeText(this@CouponRegisterActivity, "세션이 만료되었습니다. 다시 로그인해 주세요.", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this@CouponRegisterActivity, getString(R.string.coupon_register_fail), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@CouponRegisterActivity, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchMyCoupons(isManual: Boolean = false) {
        val token = tokenManager.getBearerToken() ?: run {
            swipeRefreshLayout.isRefreshing = false
            return
        }

        lifecycleScope.launch {
            try {
                val response = networkService.getMyCoupons(token)
                if (response.isSuccessful) {
                    val coupons = response.body() ?: emptyList()
                    rvMyCoupons.adapter = CouponAdapter(coupons)
                    
                    // 쿠폰 정보에 담긴 유저 정보를 통해 로컬 구독 상태 동기화 (서버 응답 보완용)
                    coupons.firstOrNull { it.user != null }?.user?.let { userData ->
                        SubscriptionHelper.persistFromUserData(this@CouponRegisterActivity, userData)
                    }
                    if (isManual) {
                        Toast.makeText(this@CouponRegisterActivity, "쿠폰 목록이 업데이트되었습니다.", Toast.LENGTH_SHORT).show()
                    }
                } else if (response.code() == 401) {
                    Log.e("CouponRegister", "Token expired during fetch")
                }
            } catch (e: Exception) {
                Log.e("CouponRegister", "Fetch failed", e)
            } finally {
                swipeRefreshLayout.isRefreshing = false
            }
        }
    }
}
