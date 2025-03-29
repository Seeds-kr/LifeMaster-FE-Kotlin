package com.example.lifemaster.presentation

import android.app.Application
//import com.example.lifemaster.BuildConfig
import com.kakao.sdk.common.KakaoSdk

class MyApplication: Application() {
    override fun onCreate() {
        super.onCreate()
//        KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_KEY)
        System.setProperty("java.net.preferIPv4Stack", "true");
    }
}