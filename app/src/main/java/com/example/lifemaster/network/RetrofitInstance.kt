package com.example.lifemaster.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

object RetrofitInstance {
    private const val BASE_URL = "http://ec2-52-79-242-42.ap-northeast-2.compute.amazonaws.com:8080"
    val networkService: NetworkService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NetworkService::class.java)
    }
}