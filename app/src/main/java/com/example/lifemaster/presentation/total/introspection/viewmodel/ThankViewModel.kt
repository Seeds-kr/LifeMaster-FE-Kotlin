package com.example.lifemaster.presentation.total.introspection.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifemaster.network.RetrofitInstance
import com.example.lifemaster.presentation.total.introspection.model.ThankRequest
import com.example.lifemaster.presentation.total.introspection.model.ThankResponse
import com.example.lifemaster.presentation.total.introspection.model.ThankCreateResponse
import com.example.lifemaster.presentation.total.introspection.model.DiaryRequest
import com.example.lifemaster.presentation.total.introspection.model.DiaryResponse
import kotlinx.coroutines.launch

sealed class UiState {
    object Loading : UiState()
    object Success : UiState()
    data class Error(val message: String) : UiState()
    object Idle : UiState()
}

class ThankViewModel : ViewModel() {

    private val networkService = RetrofitInstance.networkService

    private val _uiState = MutableLiveData<UiState>()
    val uiState: LiveData<UiState> get() = _uiState

    private val _thankData = MutableLiveData<ThankResponse?>()
    val thankData: LiveData<ThankResponse?> get() = _thankData

    //감사일기 작성 기능
    fun createThankEntry(
        token: String,
        thankOne: String,
        thankTwo: String,
        thankThree: String,
        thankFour: String,
        thankFive: String,
        thankDate: String
    ) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading

            val request = ThankRequest(
                thankOne = thankOne,
                thankTwo = thankTwo,
                thankThree = thankThree,
                thankFour = thankFour,
                thankFive = thankFive,
                thankDate = thankDate
            )

            try {
                val response = networkService.createThank("Bearer $token", request)

                if (response.isSuccessful) {
                    response.body()?.let {
                        // thankId를 받을 수 있음 (필요시 사용)
                    _uiState.value = UiState.Success
                    } ?: run {
                        _uiState.value = UiState.Error("응답 데이터가 없습니다.")
                    }
                } else {
                    _uiState.value = UiState.Error("오류: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "알 수 없는 오류가 발생했습니다.")
            }
        }
    }

    //감사일기 수정 기능
    fun updateThankEntry(
        token: String,
        thankId: Long, // 수정할 감사일기의 ID
        thankOne: String,
        thankTwo: String,
        thankThree: String,
        thankFour: String,
        thankFive: String,
        thankDate: String
    ) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading

            val request = ThankRequest(
                thankOne = thankOne,
                thankTwo = thankTwo,
                thankThree = thankThree,
                thankFour = thankFour,
                thankFive = thankFive,
                thankDate = thankDate
            )

            try {
                // 수정 API 호출
                val response = networkService.updateThank("Bearer $token", thankId, request)

                if (response.isSuccessful) {
                    _uiState.value = UiState.Success
                } else {
                    _uiState.value = UiState.Error("오류: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "알 수 없는 오류가 발생했습니다.")
            }
        }
    }

    // 감사일기 조회 기능
    fun loadThankEntry(token: String, thankId: Long) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val response = networkService.getThank("Bearer $token", thankId)
                if (response.isSuccessful) {
                    response.body()?.let {
                        _thankData.value = it
                        _uiState.value = UiState.Success
                    } ?: run {
                        _uiState.value = UiState.Error("데이터를 불러올 수 없습니다.")
                    }
                } else {
                    _uiState.value = UiState.Error("오류: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "알 수 없는 오류가 발생했습니다.")
            }
        }
    }

    // 감사일기 삭제 기능
    fun deleteThankEntry(token: String, thankId: Long) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val response = networkService.deleteThank("Bearer $token", thankId)
                if (response.isSuccessful) {
                    _uiState.value = UiState.Success
                } else {
                    _uiState.value = UiState.Error("오류: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "알 수 없는 오류가 발생했습니다.")
            }
        }
    }

    // 다이어리 생성 기능
    fun createDiaryEntry(
        token: String,
        diaryContent: String,
        diaryDate: String,
        date: String
    ) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading

            val request = DiaryRequest(
                diaryContent = diaryContent,
                diaryDate = diaryDate,
                date = date
            )

            try {
                val response = networkService.createDiary("Bearer $token", request)

                if (response.isSuccessful) {
                    response.body()?.let {
                        // diaryId를 받을 수 있음 (필요시 사용)
                        _uiState.value = UiState.Success
                    } ?: run {
                        _uiState.value = UiState.Error("응답 데이터가 없습니다.")
                    }
                } else {
                    _uiState.value = UiState.Error("오류: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "알 수 없는 오류가 발생했습니다.")
            }
        }
    }

    // 다이어리 수정 기능
    fun updateDiaryEntry(
        token: String,
        diaryId: Long,
        diaryContent: String,
        diaryDate: String,
        date: String
    ) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading

            val request = DiaryRequest(
                diaryContent = diaryContent,
                diaryDate = diaryDate,
                date = date
            )

            try {
                val response = networkService.updateDiary("Bearer $token", diaryId, request)

                if (response.isSuccessful) {
                    _uiState.value = UiState.Success
                } else {
                    _uiState.value = UiState.Error("오류: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "알 수 없는 오류가 발생했습니다.")
            }
        }
    }

    // 다이어리 삭제 기능
    fun deleteDiaryEntry(token: String, diaryId: Long) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val response = networkService.deleteDiary("Bearer $token", diaryId)
                if (response.isSuccessful) {
                    _uiState.value = UiState.Success
                } else {
                    _uiState.value = UiState.Error("오류: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "알 수 없는 오류가 발생했습니다.")
            }
        }
    }
}


