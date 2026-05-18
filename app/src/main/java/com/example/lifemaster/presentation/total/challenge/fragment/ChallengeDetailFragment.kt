package com.example.lifemaster.presentation.total.challenge.fragment

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import androidx.navigation.fragment.navArgs
import com.example.lifemaster.SubscriptionHelper
import com.example.lifemaster.R
import com.example.lifemaster.databinding.FragmentChallengeDetailBinding
import com.example.lifemaster.presentation.total.challenge.model.ChallengeItemDto
import com.example.lifemaster.presentation.total.challenge.viewmodel.ChallengeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChallengeDetailFragment : Fragment(R.layout.fragment_challenge_detail) {
    private lateinit var binding: FragmentChallengeDetailBinding

    private val viewModel: ChallengeViewModel by hiltNavGraphViewModels(R.id.nav_graph_main)

    private var challId: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // arguments 처리는 safeArgs가 대신함
    }

    private val args: ChallengeDetailFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentChallengeDetailBinding.bind(view)

        challId = args.challId

        Log.d("ChallengeDetail", "전달받은 챌린지 ID: $challId")

        initListeners()
        observeViewModel()

        viewLifecycleOwner.lifecycleScope.launch {
            readAuthToken()?.let { viewModel.refreshMyParticipatingChallenges(it) }
            if (challId != 0L) {
                loadChallengeDetail()
            } else {
                Toast.makeText(requireContext(), "챌린지 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.myParticipatingIds.collect { ids ->
                updateParticipationUi(challId in ids)
            }
        }
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
            challId = challId,
            onSuccess = { challengeDetail ->
                updateUI(challengeDetail)
            },
            onError = { errorMessage ->
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
            }
        )
    }

    // 챌린지 상세 정보로 UI를 업데이트
    private fun updateUI(challengeDetail: ChallengeItemDto) {
        binding.tvChallengeTitle.text = challengeDetail.challName

        Glide.with(this)
            .load(challengeDetail.challImg)
            .into(binding.ivChallengeBanner)

        binding.tvSection1Body.text = challengeDetail.challDesc

        binding.tvSection1Title.text = challengeDetail.challName

        val joined = when (challengeDetail.challMe) {
            true -> true
            false -> false
            null -> viewModel.isUserParticipating(challId)
        }
        updateParticipationUi(joined)
    }

    private fun updateParticipationUi(isJoined: Boolean) {
        val margin = (16 * resources.displayMetrics.density).toInt()
        val joinLp = binding.btnJoin.layoutParams as ConstraintLayout.LayoutParams
        val leaveLp = binding.btnLeave.layoutParams as ConstraintLayout.LayoutParams
        if (isJoined) {
            binding.tvParticipationStatus.text = "참여중"
            binding.btnJoin.visibility = View.GONE
            binding.btnLeave.visibility = View.VISIBLE
            leaveLp.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            leaveLp.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            leaveLp.startToEnd = ConstraintLayout.LayoutParams.UNSET
            leaveLp.marginStart = margin
            leaveLp.marginEnd = margin
            binding.btnLeave.layoutParams = leaveLp
        } else {
            binding.tvParticipationStatus.text = "참여 가능"
            binding.btnJoin.visibility = View.VISIBLE
            binding.btnLeave.visibility = View.GONE
            joinLp.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            joinLp.endToStart = R.id.btn_leave
            joinLp.marginStart = margin
            joinLp.marginEnd = (8 * resources.displayMetrics.density).toInt() // margin / 2
            binding.btnJoin.layoutParams = joinLp
            leaveLp.startToEnd = R.id.btn_join
            leaveLp.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            leaveLp.startToStart = ConstraintLayout.LayoutParams.UNSET
            leaveLp.marginStart = (8 * resources.displayMetrics.density).toInt() // margin / 2
            leaveLp.marginEnd = margin
            binding.btnLeave.layoutParams = leaveLp
        }
    }

    private fun initListeners() {
        binding.btnJoin.setOnClickListener {
            val token = readAuthToken()
            if (token == null) {
                Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Basic 유저는 챌린지 1개만 참여 가능
            var canJoin = true
            if (!SubscriptionHelper.isPremium(requireContext())) {
                val currentParticipatingCount = viewModel.myParticipatingIds.value.size
                if (currentParticipatingCount >= 1) {
                    SubscriptionHelper.checkPremiumAndRun(requireContext()) { }
                    canJoin = false
                }
            }

            if (canJoin) {
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
        }

        binding.btnLeave.setOnClickListener {
            val token = readAuthToken()
            if (token == null) {
                Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.leaveChallenge(
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
    }

    private fun readAuthToken(): String? {
        val raw = requireContext().getSharedPreferences("auth", 0).getString("token", null).orEmpty()
        if (raw.isBlank()) return null
        return if (raw.startsWith("Bearer ")) raw else "Bearer $raw"
    }
}
