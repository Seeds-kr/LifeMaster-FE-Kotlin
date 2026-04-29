package com.example.lifemaster.network

import com.example.lifemaster.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

object RetrofitInstance {

    private const val DEFAULT_BASE_URL = "https://lifemaster.harvester.kr/"

    private val baseUrl: String
        get() = (BuildConfig.BASE_URL.takeIf { it.isNotBlank() } ?: DEFAULT_BASE_URL)
            .let { if (it.endsWith("/")) it else "$it/" }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .baseUrl(BuildConfig.BASE_URL)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val networkService: NetworkService by lazy {
        retrofit.create(NetworkService::class.java)
    }
}

