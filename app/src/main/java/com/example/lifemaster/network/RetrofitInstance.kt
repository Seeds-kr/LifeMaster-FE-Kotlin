package com.example.lifemaster.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

object RetrofitInstance {

    private const val BASE_URL =
        "http://ec2-54-180-100-209.ap-northeast-2.compute.amazonaws.com:8080" // 임시 서버

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val networkService: NetworkService by lazy {
        retrofit.create(NetworkService::class.java)
    }
}