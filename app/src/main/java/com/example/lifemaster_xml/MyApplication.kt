package com.example.lifemaster_xml

import android.app.Application
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner

class MyApplication: Application(), ViewModelStoreOwner {
    private val appViewModelStore: ViewModelStore by lazy { ViewModelStore() }
    override val viewModelStore: ViewModelStore
        get() = appViewModelStore
}