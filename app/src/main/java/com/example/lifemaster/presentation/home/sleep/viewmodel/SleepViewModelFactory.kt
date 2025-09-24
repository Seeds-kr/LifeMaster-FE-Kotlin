package com.example.lifemaster.presentation.home.sleep.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.lifemaster.network.NetworkService

/**
 * viewmodel에 생성자가 필요한 경우 팩토리를 사용해야한다.
 * 단점) 모델 클래스에 대한 검증 과정이 없어 SleepViewModel이 아닌 다른 ViewModel을 요구해도 무조건 SleepViewModel을 반환하게 된다.
 * 또한, 여러 ViewModel을 팩토리 하나로 관리할 때는 부적합.
 */
class SleepViewModelFactory(
    private val networkService: NetworkService
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SleepViewModel(networkService) as T
    }
}