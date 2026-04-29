package com.example.lifemaster.presentation.total.challenge.fragment

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import androidx.navigation.fragment.navArgs
import com.example.lifemaster.R
import com.example.lifemaster.databinding.FragmentChallengeDetailBinding
import com.example.lifemaster.network.NetworkService
import com.example.lifemaster.presentation.total.challenge.viewmodel.ChallengeViewModel
import com.example.lifemaster.presentation.total.challenge.viewmodel.ChallengeViewModelFactory
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChallengeDetailFragment : Fragment(R.layout.fragment_challenge_detail) {
    private lateinit var binding: FragmentChallengeDetailBinding

    @Inject lateinit var networkService: NetworkService

    // DI를 사용하여 ViewModel 생성
    private val viewModel: ChallengeViewModel by lazy {
        val factory = ChallengeViewModelFactory(networkService)
        ViewModelProvider(this, factory)[ChallengeViewModel::class.java]
    }
    // Hilt를 사용하여 ViewModel 생성
    private val viewModel: ChallengeViewModel by viewModels()

    private var challId: String = "0"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            challId = it.getString("challId", "0") ?: "0"
        }
    }

    private val args: ChallengeDetailFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentChallengeDetailBinding.bind(view)

        if (challId != "0") {
            loadChallengeDetail()
        } else {
            Toast.makeText(requireContext(), "챌린지 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
        }

        val challengeId = args.challId
        Log.d("ChallengeDetail", "전달받은 챌린지 ID: $challengeId")

        initListeners()
    }

    // 서버에서 챌린지 상세 정보 로드
    private fun loadChallengeDetail() {
        val token = readAuthToken()
        if (token == null) {
            Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.getChallengeDetail(
            token = token,
            challId = challId.toLongOrNull() ?: 0L,
            onSuccess = { challengeDetail ->
                updateUI(challengeDetail)
            },
            onError = { errorMessage ->
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
            }
        )
    }

    // 챌린지 상세 정보로 UI를 업데이트
    private fun updateUI(challengeDetail: com.example.lifemaster.presentation.total.challenge.model.ChallengeItemDto) {
        // 챌린지 제목 설정
        binding.tvChallengeTitle.text = challengeDetail.challName

        // 챌린지 이미지 로드
        Glide.with(this)
            .load(challengeDetail.challImg)
            .into(binding.ivChallengeBanner)

        // 챌린지 설명 설정
        binding.tvSection1Body.text = challengeDetail.challDesc

        // 섹션 제목도 챌린지 이름으로 설정
        binding.tvSection1Title.text = challengeDetail.challName
    }

    private fun initListeners() {
        // '참여하기' 버튼(ID: btn_join)에 클릭 리스너 설정
        binding.btnJoin.setOnClickListener {
            val challIdAsLong = challId.toLongOrNull()
            if (challIdAsLong == null || challIdAsLong == 0L) {
                Toast.makeText(requireContext(), "챌린지 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val token = readAuthToken()
            if (token == null) {
                Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.joinChallenge(
                token = token,
                challId = challId,
                onSuccess = { message ->
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                },
                onError = { errorMessage ->
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
                }
            )
        }

        // '참여 취소' 버튼(ID: btn_leave)에 클릭 리스너 설정
        binding.btnLeave.setOnClickListener {
            val challIdAsLong = challId.toLongOrNull()
            if (challIdAsLong == null || challIdAsLong == 0L) {
                Toast.makeText(requireContext(), "챌린지 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val token = readAuthToken()
            if (token == null) {
                Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.leaveChallenge(
                token = token,
                challId = challIdAsLong,
                onSuccess = { message ->
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                },
                onError = { errorMessage ->
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    // SharedPreferences에서 인증 토큰을 읽어옴옴
    // @return Bearer 토큰 문자열 또는 null (로그인하지 않은 경우)
    private fun readAuthToken(): String? {
        val raw = requireContext().getSharedPreferences("auth", 0).getString("token", null).orEmpty()
        if (raw.isBlank()) return null
        return if (raw.startsWith("Bearer ")) raw else "Bearer $raw"
    }
}