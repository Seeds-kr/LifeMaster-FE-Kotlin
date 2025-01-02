package com.example.lifemaster

import android.app.Application
import android.util.Log
import com.kakao.sdk.common.util.Utility

class MyApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        val keyHash = Utility.getKeyHash(this)
        Log.d("keyHash", keyHash)
    }
}