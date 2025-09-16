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
}