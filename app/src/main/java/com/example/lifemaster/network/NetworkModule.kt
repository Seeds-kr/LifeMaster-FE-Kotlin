package com.example.lifemaster.network

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import javax.inject.Singleton

/**
 * SingletonComponent에 NetworkModule이 가지고 있는 의존성이 바인딩됨
 * SingletonComponent → 애플리케이션이 종료되기 전까지 메모리에 남아있음
 * @Singleton : Hilt에게 이 객체를 앱 전체 수명 주기 동안 오직 단 하나만 생성하여 재사용하도록 지시하는 스코프 어노테이션
 * NetworkService 는 자원을 매우 많이 소모하는 객체이기에 단 하나만 만드는 것이 권장된다.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = ""

    @Singleton
    @Provides
    fun provideNetworkService(retrofit: Retrofit): NetworkService {
        return retrofit.create(NetworkService::class.java)
    }

    @Singleton
    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create()) // json 구조의 응답이 아닌 경우 처리
            .addConverterFactory(GsonConverterFactory.create()) // json 구조 응답 처리
            .build()
    }

    @Singleton
    @Provides
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder().addInterceptor(AuthInterceptor("YOUR_TOKEN")).build()
    }
}