package com.example.lifemaster.presentation.login.view

import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.edit
import androidx.core.graphics.toColorInt
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.example.lifemaster.R
import com.example.lifemaster.databinding.FragmentRegisterProfileBinding
import com.example.lifemaster.network.NetworkService
import com.example.lifemaster.presentation.MainActivity
import com.example.lifemaster.presentation.login.model.LoginInfo
import com.example.lifemaster.presentation.login.model.NicknameCheckResponse
import com.example.lifemaster.presentation.login.model.RegNickResponse
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class RegisterProfileFragment : Fragment(R.layout.fragment_register_profile) {

    private var _binding: FragmentRegisterProfileBinding? = null
    private val binding get() = _binding!!

    @Inject lateinit var networkService: NetworkService

    private var imageUri: Uri? = null
    private var regId: String = ""
    private var email: String = ""
    private var password: String = ""

    private var isNickAvailable = false
    private val uiHandler = Handler(Looper.getMainLooper())
    private var nickCheckRunnable: Runnable? = null
    private var lastCall: Call<NicknameCheckResponse>? = null
    private var lastQueryText: String = ""

    private val pickImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            imageUri = uri
            if (uri != null) {
                binding.ivProfile.setImageURI(uri)
                binding.ivDefaultIcon.visibility = View.GONE
            } else {
                binding.ivDefaultIcon.visibility = View.VISIBLE
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        regId = arguments?.getString("regId").orEmpty()
        email = arguments?.getString("email").orEmpty()
        password = arguments?.getString("password").orEmpty()

        binding.tvEmail.text = email

        binding.includeBackButton.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.frameProfile.clipToOutline = true
        binding.frameProfile.setOnClickListener { pickImage.launch("image/*") }

        applyStartButtonStyle(false)
        hideNickStatus()

        binding.editNickname.addTextChangedListener { s ->
            val nick = s?.toString()?.trim().orEmpty()
            if (nick.isBlank()) {
                isNickAvailable = false
                applyStartButtonStyle(false)
                hideNickStatus()
                return@addTextChangedListener
            }

            lastQueryText = nick
            nickCheckRunnable?.let { uiHandler.removeCallbacks(it) }
            nickCheckRunnable = Runnable { requestNicknameCheck(nick) }
            uiHandler.postDelayed(nickCheckRunnable!!, 300)
        }

        binding.btnStart.setOnClickListener {
            val nick = binding.editNickname.text.toString().trim()
            if (!isNickAvailable) {
                toast("닉네임 확인 후 진행해 주세요")
                return@setOnClickListener
            }
            postRegisterNicknameMultipart(nick)
        }
    }

    private fun requestNicknameCheck(nick: String) {
        showNickStatusChecking()
        lastCall?.cancel()
        lastCall = networkService.checkNickname(nick)
        lastCall!!.enqueue(object : Callback<NicknameCheckResponse> {
            override fun onResponse(
                call: Call<NicknameCheckResponse>,
                res: Response<NicknameCheckResponse>
            ) {
                if (call.isCanceled) return
                if (binding.editNickname.text.toString().trim() != lastQueryText) return

                val available = res.isSuccessful && (res.body()?.available == true)
                isNickAvailable = available
                applyStartButtonStyle(available)
                if (available) showNickStatusAvailable() else hideNickStatus()
            }

            override fun onFailure(call: Call<NicknameCheckResponse>, t: Throwable) {
                if (call.isCanceled) return
                isNickAvailable = false
                applyStartButtonStyle(false)
                hideNickStatus()
            }
        })
    }

    private fun applyStartButtonStyle(available: Boolean) {
        binding.btnStart.isEnabled = available
        if (available) {
            binding.btnStart.setBackgroundResource(R.drawable.bg_round_and_mint)
            binding.btnStart.text = "라이프마스터 시작하기"
        } else {
            binding.btnStart.setBackgroundResource(R.drawable.bg_round_and_mint_60)
            binding.btnStart.text = "닉네임을 입력해주세요"
        }
    }

    private fun showNickStatusAvailable() {
        val mint = "#7CD7BD".toColorInt()
        binding.layoutNicknameStatus.visibility = View.VISIBLE
        binding.tvNicknameStatus.text = "사용 가능한 닉네임이에요"
        binding.tvNicknameStatus.setTextColor(mint)
        binding.ivNicknameStatus.setImageResource(R.drawable.ic_check_circle)
        binding.ivNicknameStatus.imageTintList = ColorStateList.valueOf(mint)
        binding.ivNicknameStatus.imageAlpha = 255
    }

    private fun showNickStatusChecking() {
        val gray = "#9E9E9E".toColorInt()
        binding.layoutNicknameStatus.visibility = View.VISIBLE
        binding.tvNicknameStatus.text = "확인 중..."
        binding.tvNicknameStatus.setTextColor(gray)
        binding.ivNicknameStatus.setImageResource(R.drawable.ic_check_circle)
        binding.ivNicknameStatus.imageTintList = ColorStateList.valueOf(gray)
        binding.ivNicknameStatus.imageAlpha = 160
    }

    private fun hideNickStatus() {
        binding.layoutNicknameStatus.visibility = View.INVISIBLE
    }

    private fun postRegisterNicknameMultipart(nickname: String) {
        val text = "text/plain".toMediaType()
        val regIdPart: RequestBody = regId.toRequestBody(text)
        val nickPart: RequestBody = nickname.toRequestBody(text)

        val imagePart: MultipartBody.Part? = imageUri?.let { uri ->
            val temp = File(requireContext().cacheDir, "profile_${System.currentTimeMillis()}.jpg")
            requireContext().contentResolver.openInputStream(uri)?.use { input ->
                temp.outputStream().use { output -> input.copyTo(output) }
            }
            MultipartBody.Part.createFormData(
                "image",
                temp.name,
                temp.asRequestBody("image/*".toMediaType())
            )
        }

        networkService.registerNickname(regIdPart, nickPart, imagePart)
            .enqueue(object : Callback<RegNickResponse> {
                override fun onResponse(call: Call<RegNickResponse>, res: Response<RegNickResponse>) {
                    val body = res.body()
                    if (!res.isSuccessful || body == null) {
                        toast(res.errorBody()?.string()?.take(150) ?: "닉네임 등록 실패(${res.code()})")
                        return
                    }

                    requireContext().getSharedPreferences("auth", 0).edit {
                        val mid = body.memberId.toString()
                        putString("memberId", mid)
                        putString("userId",  mid) 
                        // 가입 시 입력한 닉네임 미리 저장
                        putString("nickname", nickname)
                        putString("nickName", nickname)
                    }

                    autoLoginThenGoHome()
                }
                override fun onFailure(call: Call<RegNickResponse>, t: Throwable) {
                    toast("네트워크 오류: ${t.message}")
                }
            })
    }

    private fun autoLoginThenGoHome() {
        if (email.isBlank() || password.isBlank()) {
            toast("가입 완료! 로그인해 주세요.")
            return
        }
        networkService.enterUserLogin(LoginInfo(email, password))
            .enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, res: Response<String>) {
                    if (!res.isSuccessful) { toast("로그인 실패(${res.code()})"); return }
                    val token = res.body().orEmpty()
                    requireContext().getSharedPreferences("auth", android.content.Context.MODE_PRIVATE)
                        .edit {
                            val nick = binding.editNickname.text.toString().trim()
                            putString("token", token)
                            putString("email", email)
                            putString("nickname", nick)
                            putString("nickName", nick)
                        }
                    startActivity(Intent(requireActivity(), MainActivity::class.java).apply {
                        putExtra("user_token", token)
                    })
                    requireActivity().finish()
                }
                override fun onFailure(call: Call<String>, t: Throwable) {
                    toast("로그인 네트워크 오류: ${t.message}")
                }
            })
    }

    private fun toast(msg: String) =
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()

    override fun onDestroyView() {
        super.onDestroyView()
        nickCheckRunnable?.let { uiHandler.removeCallbacks(it) }
        lastCall?.cancel()
        _binding = null
    }
}