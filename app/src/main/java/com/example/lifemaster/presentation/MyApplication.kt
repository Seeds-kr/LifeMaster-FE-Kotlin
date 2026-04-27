package com.example.lifemaster.presentation

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication: Application() {
    override fun onCreate() {
        try {
            super.onCreate()
        } catch (t: Throwable) {
            // 앱이 시작 직후 크래시 나는 경우를 위해 최대한 로그를 남깁니다.
            Log.e("APP_STARTUP_CRASH", "MyApplication.onCreate failed", t)
            throw t
        }
//        val keyHash = Utility.getKeyHash(this)
//        Log.d("keyHash", keyHash)
//        KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_KEY)
    }
}