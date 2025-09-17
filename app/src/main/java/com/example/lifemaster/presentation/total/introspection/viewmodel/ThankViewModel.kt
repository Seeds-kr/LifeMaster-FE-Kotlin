import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifemaster.network.RetrofitInstance
import com.example.lifemaster.presentation.total.introspection.model.ThankRequest
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
                    _uiState.value = UiState.Success
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
}


