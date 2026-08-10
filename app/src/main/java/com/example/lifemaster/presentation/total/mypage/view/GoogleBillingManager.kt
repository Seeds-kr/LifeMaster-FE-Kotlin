package com.example.lifemaster.presentation.total.mypage.view

import android.app.Activity
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.example.lifemaster.network.NetworkService
import com.example.lifemaster.presentation.total.mypage.model.GooglePayVerifyRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GoogleBillingManager(
    private val activity: Activity,
    private val coroutineScope: CoroutineScope,
    private val networkService: NetworkService,
    private val authTokenProvider: () -> String?,
    private val listener: Listener
) {
    private var isConnected = false

    // queryProductDetailsAsync 콜백 이후 결제 완료 시점까지 상품 가격/이름 정보를 들고 있어야
    // 서버 검증 요청(amount, currency, productName)을 만들 수 있음
    private var pendingProductDetails: ProductDetails? = null

    interface Listener {
        fun onConnected()
        fun onPurchaseVerified()
        fun onError(message: String)
    }

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                handlePurchases(purchases)
            }

            BillingClient.BillingResponseCode.USER_CANCELED -> {
                listener.onError("결제가 취소되었습니다.")
            }

            else -> {
                listener.onError(billingResult.debugMessage.ifBlank { "결제 처리 중 오류가 발생했습니다." })
            }
        }
    }

    private val billingClient: BillingClient = BillingClient.newBuilder(activity)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()
        )
        .build()

    fun connect() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    isConnected = true
                    listener.onConnected()
                } else {
                    listener.onError(billingResult.debugMessage.ifBlank { "결제 연결에 실패했습니다." })
                }
            }

            override fun onBillingServiceDisconnected() {
                isConnected = false
                listener.onError("결제 서비스와 연결이 끊어졌습니다.")
            }
        })
    }

    fun launchSubscription(productId: String) {
        if (!isConnected) {
            listener.onError("결제 서비스 연결 중입니다. 잠시 후 다시 시도해 주세요.")
            return
        }

        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )

        val queryParams = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(queryParams) { billingResult, productDetailsList ->
            if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                listener.onError(billingResult.debugMessage.ifBlank { "상품 정보를 불러오지 못했습니다." })
                return@queryProductDetailsAsync
            }

            val targetProduct = productDetailsList.firstOrNull()
            if (targetProduct == null) {
                listener.onError("구매 가능한 상품이 없습니다. 콘솔 상품 설정을 확인해 주세요.")
                return@queryProductDetailsAsync
            }

            val offerToken = targetProduct.subscriptionOfferDetails?.firstOrNull()?.offerToken
            if (offerToken.isNullOrBlank()) {
                listener.onError("구독 상품 오퍼 정보를 찾을 수 없습니다.")
                return@queryProductDetailsAsync
            }

            pendingProductDetails = targetProduct
            launchBillingFlow(targetProduct, offerToken)
        }
    }

    private fun launchBillingFlow(productDetails: ProductDetails, offerToken: String) {
        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)
            .setOfferToken(offerToken)
            .build()

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams))
            .build()

        val billingResult = billingClient.launchBillingFlow(activity, billingFlowParams)
        if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
            listener.onError(billingResult.debugMessage.ifBlank { "결제창을 열지 못했습니다." })
        }
    }

    private fun handlePurchases(purchases: List<Purchase>?) {
        if (purchases.isNullOrEmpty()) {
            listener.onError("결제 내역을 확인할 수 없습니다.")
            return
        }

        purchases.forEach { purchase ->
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                verifyWithServer(purchase)
            }
        }
    }

    // Google은 결제 성공만으로 구매를 완료 처리하지만, 위·변조된 영수증으로 프리미엄이
    // 활성화되지 않도록 서버 검증(/payments/googlePay/verify)을 통과한 뒤에만 acknowledge한다.
    private fun verifyWithServer(purchase: Purchase) {
        val token = authTokenProvider()
        if (token.isNullOrBlank()) {
            listener.onError("로그인이 필요합니다.")
            return
        }

        val productDetails = pendingProductDetails
        val pricingPhase = productDetails?.subscriptionOfferDetails
            ?.firstOrNull()
            ?.pricingPhases
            ?.pricingPhaseList
            ?.firstOrNull()

        val request = GooglePayVerifyRequest(
            subscriptionId = purchase.products.firstOrNull().orEmpty().ifBlank { productDetails?.productId.orEmpty() },
            purchaseToken = purchase.purchaseToken,
            productName = productDetails?.name ?: productDetails?.productId.orEmpty(),
            amount = pricingPhase?.priceAmountMicros?.let { (it / 1_000_000L).toInt() } ?: 0,
            currency = pricingPhase?.priceCurrencyCode.orEmpty()
        )

        coroutineScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    networkService.verifyGooglePayReceipt(token, request)
                }
                if (response.isSuccessful) {
                    acknowledgePurchase(purchase)
                } else {
                    val message = response.errorBody()?.string()?.takeIf { it.isNotBlank() }
                        ?: "구독 검증에 실패했습니다. (${response.code()})"
                    listener.onError(message)
                }
            } catch (e: Exception) {
                listener.onError("구독 검증 중 오류가 발생했습니다: ${e.message}")
            }
        }
    }

    private fun acknowledgePurchase(purchase: Purchase) {
        if (purchase.isAcknowledged) {
            listener.onPurchaseVerified()
            return
        }

        val acknowledgeParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        billingClient.acknowledgePurchase(acknowledgeParams) { ackResult ->
            if (ackResult.responseCode == BillingClient.BillingResponseCode.OK) {
                listener.onPurchaseVerified()
            } else {
                listener.onError(ackResult.debugMessage.ifBlank { "결제 승인 처리에 실패했습니다." })
            }
        }
    }

    fun release() {
        isConnected = false
        billingClient.endConnection()
    }
}
