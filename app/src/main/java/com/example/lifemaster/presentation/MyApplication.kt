package com.example.lifemaster.presentation

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication: Application() {
    override fun onCreate() {
        super.onCreate()
//        val keyHash = Utility.getKeyHash(this)
//        Log.d("keyHash", keyHash)
//        KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_KEY)
    }
}