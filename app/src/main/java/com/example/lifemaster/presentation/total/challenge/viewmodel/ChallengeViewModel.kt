package com.example.lifemaster.presentation.challenge.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.lifemaster.presentation.total.challenge.model.ChallengeItem
import com.example.lifemaster.presentation.total.challenge.model.ChallengeResponse
import com.example.lifemaster.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime

class ChallengeViewModel : ViewModel() {

    private val _originalChallengeData = MutableLiveData<List<ChallengeItem>>()
    private val _sortedChallengeList = MutableLiveData<List<ChallengeItem>>()
    val sortedChallengeList: LiveData<List<ChallengeItem>> = _sortedChallengeList

    fun loadChallenges() {
        Log.d("API_CALL", "챌린지 API 호출 시작")

        RetrofitInstance.networkService.getChallenges(page = 0).enqueue(object : Callback<ChallengeResponse> {
            override fun onResponse(call: Call<ChallengeResponse>, response: Response<ChallengeResponse>) {
                if (response.isSuccessful) {
                    response.body()?.content?.let { list ->
                        _originalChallengeData.value = list
                        sortChallenges("latest")
                        Log.d("ViewModel", "챌린지 로딩 성공: ${list.size}개")
                    }
                } else {
                    Log.e("ViewModel", "서버 응답 에러: ${response.code()} - ${response.message()}")
                }
            }

            override fun onFailure(call: Call<ChallengeResponse>, t: Throwable) {
                Log.e("ViewModel", "통신 실패: ${t.message}")
            }
        })
    }

    fun sortChallenges(criteria: String) {
        val currentList = _originalChallengeData.value ?: return

        val sortedList = when (criteria) {
            "latest" -> currentList.sortedWith(compareByDescending { it.createdAt })
            "oldest" -> currentList.sortedWith(compareBy { it.createdAt })
            "popularity" -> currentList.sortedWith(compareByDescending { it.challCnt })
            "name" -> currentList.sortedWith(compareBy { it.challName })
            else -> currentList
        }

        _sortedChallengeList.value = sortedList
        Log.d("ViewModel", "챌린지 정렬 완료: 기준=$criteria")
    }
}