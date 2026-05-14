package com.example.lifemaster.presentation.total.challenge.fragment

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lifemaster.R
import com.example.lifemaster.databinding.FragmentChallengeBinding
import com.example.lifemaster.network.TokenProvider
import com.example.lifemaster.presentation.total.challenge.fragment.adapter.ChallengeAdapter
import com.example.lifemaster.presentation.total.challenge.viewmodel.ChallengeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class MyChallenge(
    val imageRes: Int,
    val isCompleted: Boolean,
    val completionTime: String? = null
)

@AndroidEntryPoint
class ChallengeFragment : Fragment() {

    private var _binding: FragmentChallengeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ChallengeViewModel by hiltNavGraphViewModels(R.id.nav_graph_main)

    private lateinit var challengeAdapter: ChallengeAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChallengeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMyChallenges()
        setupRecyclerView()
        observeViewModel()
        setupClickListeners()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.sortedChallengeList.collectLatest { sortedList ->
                    challengeAdapter.submitList(sortedList)
                    if (sortedList.isNotEmpty()) {
                        Log.d("ChallengeFragment", "챌린지 목록 UI 업데이트: ${sortedList.size}개")
                    } else {
                        Log.d("ChallengeFragment", "표시할 챌린지가 없습니다.")
                    }
                }
            }
        }
    }

    private fun setupMyChallenges() {
        val myChallenge1 = MyChallenge(
            imageRes = R.drawable.ic_cold_shower,
            isCompleted = true,
            completionTime = "9:12am"
        )
        val myChallenge2 = MyChallenge(
            imageRes = R.drawable.ic_stretching,
            isCompleted = false
        )

        setupMyChallengeView(binding.myChallenge1.root, myChallenge1)
        setupMyChallengeView(binding.myChallenge2.root, myChallenge2)
    }

    private fun setupMyChallengeView(challengeView: View, challengeData: MyChallenge) {
        val imageView = challengeView.findViewById<ImageView>(R.id.iv_challenge_image)
        val checkmark = challengeView.findViewById<ImageView>(R.id.iv_checkmark)
        val timeTextView = challengeView.findViewById<TextView>(R.id.tv_completion_time)

        imageView.setImageResource(challengeData.imageRes)

        if (challengeData.isCompleted) {
            challengeData.completionTime?.let {
                timeTextView.text = it
                timeTextView.visibility = View.VISIBLE
            }
            checkmark.visibility = View.VISIBLE

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val blurEffect = RenderEffect.createBlurEffect(20f, 20f, Shader.TileMode.CLAMP)
                imageView.setRenderEffect(blurEffect)
            }
        } else {
            checkmark.visibility = View.GONE
            timeTextView.visibility = View.GONE

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                imageView.setRenderEffect(null)
            }
        }
    }

    private fun setupRecyclerView() {
        challengeAdapter = ChallengeAdapter()
        binding.rvChallenges.apply {
            adapter = challengeAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun setupClickListeners() {
        challengeAdapter.onItemClickListener = { challenge ->
            val action = ChallengeFragmentDirections.actionChallengeFragmentToChallengeDetailFragment(
                challenge.challId.toLong()
            )
            findNavController().navigate(action)
        }

        challengeAdapter.onJoinButtonClickListener = { challenge ->
            if (!challenge.isJoined) {
                val token = readAuthToken()
                if (token != null) {
                    viewModel.joinChallenge(
                        token = token,
                        challId = challenge.challId,
                        onSuccess = { message ->
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            // 참여 성공 후 목록 갱신
                            viewModel.loadChallenges(token)
                        },
                        onError = { errorMessage ->
                            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                        }
                    )
                } else {
                    Toast.makeText(context, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun readAuthToken(): String? {
        return TokenProvider.getBearerToken(requireContext())
    }

    override fun onResume() {
        super.onResume()
        if (_binding == null || !::challengeAdapter.isInitialized) return
        
        val token = readAuthToken()
        if (token != null) {
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.refreshMyParticipatingChallenges(token)
            }
            viewModel.loadChallenges(token)
        } else {
            Log.e("ChallengeFragment", "인증 토큰이 없습니다. 로그인이 필요합니다.")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
