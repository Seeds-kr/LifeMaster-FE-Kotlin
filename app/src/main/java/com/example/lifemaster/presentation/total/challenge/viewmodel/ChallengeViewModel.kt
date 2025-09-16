package com.example.lifemaster.presentation.challenge.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.lifemaster.presentation.total.challenge.model.ChallengeResponse
import com.example.lifemaster.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChallengeViewModel : ViewModel() {

    private val _challengeData = MutableLiveData<ChallengeResponse>()
    val challengeData: LiveData<ChallengeResponse> = _challengeData

    fun loadChallenges() {
        RetrofitInstance.networkService.getChallenges(page = 0).enqueue(object : Callback<ChallengeResponse> {
            override fun onResponse(call: Call<ChallengeResponse>, response: Response<ChallengeResponse>) {
                if (response.isSuccessful) {
                    _challengeData.postValue(response.body())
                    Log.d("ViewModel", "챌린지 로딩 성공")
                } else {
                    Log.e("ViewModel", "서버 응답 에러: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<ChallengeResponse>, t: Throwable) {
                Log.e("ViewModel", "통신 실패: ${t.message}")
            }
        })
    }
}