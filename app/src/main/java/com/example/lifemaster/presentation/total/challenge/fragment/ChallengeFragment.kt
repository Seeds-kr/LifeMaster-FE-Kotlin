package com.example.lifemaster.presentation.total.challenge.fragment

import android.graphics.RenderEffect
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lifemaster.R
import com.example.lifemaster.data.repository.challenge.ChallengeRepository
import com.example.lifemaster.databinding.FragmentChallengeBinding
import com.example.lifemaster.network.RetrofitInstance
import com.example.lifemaster.presentation.total.challenge.fragment.adapter.ChallengeAdapter
import com.example.lifemaster.presentation.total.challenge.viewmodel.ChallengeViewModel
import com.example.lifemaster.presentation.total.challenge.viewmodel.ChallengeViewModelFactory
import kotlinx.coroutines.flow.collectLatest // Flow의 데이터를 수집
import kotlinx.coroutines.launch
import android.widget.PopupMenu
import androidx.paging.PagingData

data class MyChallenge(
    val imageRes: Int,
    val isCompleted: Boolean,
    val completionTime: String? = null
)

class ChallengeFragment : Fragment() {

    private var _binding: FragmentChallengeBinding? = null
    private val binding get() = _binding!!

    // DI를 사용하여 ViewModel 생성
    private val viewModel: ChallengeViewModel by lazy {
        val repository = ChallengeRepository(RetrofitInstance.networkService)
        val factory = ChallengeViewModelFactory(repository, RetrofitInstance.networkService)
        ViewModelProvider(this, factory)[ChallengeViewModel::class.java]
    }

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
        setupSortListener()
        viewModel.loadChallenges()
    }

    private fun observeViewModel() {
        viewModel.sortedChallengeList.observe(viewLifecycleOwner) { sortedList ->
            if (sortedList != null) {
                challengeAdapter.submitData(PagingData.from(sortedList))
                Log.d("ChallengeFragment", "챌린지 목록 UI 업데이트: ${sortedList.size}개")
            } else {
                Log.e("ChallengeFragment", "ViewModel에서 정렬된 리스트가 null입니다.")
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

        setupRecyclerView()
        setupClickListeners()
        setupSearchView()

        observeChallengeData()
        observeSearchResults()
    }

    private fun setupRecyclerView()=with(binding) {
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

    private fun setupSortListener() {
        binding.tvFilter.setOnClickListener {
            showSortPopupMenu(it)
        }
    }

    private fun showSortPopupMenu(view: View) {
        val popup = PopupMenu(requireContext(), view)
        popup.menuInflater.inflate(R.menu.challenge_sort_menu, popup.menu)

        popup.setOnMenuItemClickListener { menuItem ->
            val newCriteria: String
            val newText: String

            when (menuItem.itemId) {
                R.id.action_sort_latest -> {
                    newCriteria = "latest"
                    newText = "최신순"
                }
                R.id.action_sort_popularity -> {
                    newCriteria = "popularity"
                    newText = "참여자순"
                }
                else -> return@setOnMenuItemClickListener false
            }
            viewModel.sortChallenges(newCriteria)
            binding.tvFilter.text = newText

            true
        }

        popup.show()
    }

    private fun setupRecyclerView() {
        challengeAdapter = ChallengeAdapter()
        binding.rvChallenges.apply {
            adapter = challengeAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun observeChallengeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            // collectLatest: viewModel.challenges Flow에서
            // 새로운 PagingData가 발행될 때마다 이전 작업을 취소하고 새 데이터로 블록을 실행
            viewModel.challenges.collectLatest { pagingData ->
                // 검색 모드가 아닐 때 일반 목록을 표시
                if (!viewModel.isSearchMode.value) {
                    challengeAdapter.submitData(pagingData)
                }
            }
        }
    }

    private fun observeSearchResults() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.searchResults.collect { searchResults ->
                if (viewModel.isSearchMode.value) {
                    // 검색 결과를 PagingData로 Adapter에 제출
                    challengeAdapter.submitData(searchResults)
                }
            }
        }
    }

    private fun setupClickListeners() {
        challengeAdapter.onItemClickListener = { challenge ->
            val bundle = Bundle().apply {
                putLong("challId", challenge.challId)
            }
            findNavController().navigate(R.id.action_challengeFragment_to_challengeDetailFragment, bundle)
            val action = ChallengeFragmentDirections.actionChallengeFragmentToChallengeDetailFragment(
                challenge.challId.toString()
            )
            findNavController().navigate(action)
        }

        challengeAdapter.onJoinButtonClickListener = { challenge ->
            val token = readAuthToken()
            if (token != null) {
                viewModel.joinChallenge(
                    token = token,
                    challId = challenge.challId,
                    onSuccess = { message ->
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
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

    // SearchView 설정, 검색 기능 연결
    private fun setupSearchView() {
        binding.svSearch.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { searchQuery ->
                    if (searchQuery.isNotBlank()) {
                        performSearch(searchQuery)
                    } else {
                        // 검색어가 비어있으면 검색 모드 해제
                        viewModel.clearSearch()
                        // 일반 목록으로 복귀
                        viewLifecycleOwner.lifecycleScope.launch {
                            viewModel.challenges.collectLatest { pagingData ->
                                challengeAdapter.submitData(pagingData)
                            }
                        }
                    }
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // 실시간 검색이 필요하면 여기서 처리
                // 현재는 검색 버튼 클릭 시에만 검색하도록 구현
                if (newText.isNullOrBlank()) {
                    // 검색어가 비어있으면 검색 모드 해제
                    viewModel.clearSearch()
                    viewLifecycleOwner.lifecycleScope.launch {
                        viewModel.challenges.collectLatest { pagingData ->
                            challengeAdapter.submitData(pagingData)
                        }
                    }
                }
                return false
            }
        })

        // SearchView 닫기 버튼 클릭 시 검색 모드 해제
        binding.svSearch.setOnCloseListener {
            viewModel.clearSearch()
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.challenges.collectLatest { pagingData ->
                    challengeAdapter.submitData(pagingData)
                }
            }
            false
        }
    }

    // 검색 수행
    private fun performSearch(query: String) {
        val token = readAuthToken()
        if (token == null) {
            Toast.makeText(context, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.searchChallenges(
            token = token,
            searchQuery = query,
            onSuccess = {
                // 검색 결과는 observeSearchResults에서 자동으로 처리됨
            },
            onError = { errorMessage ->
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            }
        )
    }

    // SharedPreferences에서 인증 토큰을 읽어옴옴
    // @return Bearer 토큰 문자열 또는 null (로그인하지 않은 경우)
    private fun readAuthToken(): String? {
        val raw = requireContext().getSharedPreferences("auth", 0).getString("token", null).orEmpty()
        if (raw.isBlank()) return null
        return if (raw.startsWith("Bearer ")) raw else "Bearer $raw"
    }
        binding.rvChallenges.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = challengeAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
