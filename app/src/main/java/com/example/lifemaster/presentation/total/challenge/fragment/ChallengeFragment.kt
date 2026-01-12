// 패키지 이름은 그대로 유지합니다.
package com.example.lifemaster.presentation.total.challenge.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lifemaster.databinding.FragmentChallengeBinding
// 3. Adapter의 경로를 확인해주세요. (현재 경로가 맞는지 확인 필요)
import com.example.lifemaster.presentation.total.challenge.adapter.ChallengeAdapter
import com.example.lifemaster.presentation.total.challenge.viewmodel.ChallengeViewModel // 4. ViewModel을 import 합니다.
import kotlinx.coroutines.flow.collectLatest // 5. Flow의 데이터를 수집하기 위해 import 합니다.
import kotlinx.coroutines.launch // 6. 코루틴을 실행하기 위해 import 합니다.

class ChallengeFragment : Fragment() {

    private var _binding: FragmentChallengeBinding? = null
    private val binding get() = _binding!!

    // 7. 'by viewModels()'를 통해 ChallengeViewModel의 인스턴스를 가져옵니다.
    // 이 코드로 Fragment는 ViewModel을 소유하고 생명주기를 관리하게 됩니다.
    private val viewModel: ChallengeViewModel by viewModels()

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

        setupRecyclerView()
        setupClickListeners()
        setupSearchView()

        // 8. ViewModel의 데이터를 관찰하는 함수를 호출합니다.
        observeChallengeData()
        observeSearchResults()
    }

    /**
     * RecyclerView와 Adapter를 초기 설정하는 함수입니다.
     */
    private fun setupRecyclerView() {
        // adapter 경로에 오타가 있다면(adatper), 폴더명을 수정하고 import 경로도 바꿔주세요.
        challengeAdapter = ChallengeAdapter()
        binding.rvChallenges.apply {
            adapter = challengeAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    /**
     * ViewModel의 PagingData Flow를 관찰(구독)하고,
     * 데이터에 변경이 있을 때마다 Adapter에 새로운 데이터를 전달합니다.
     */
    private fun observeChallengeData() {
        // Fragment의 View 생명주기(화면에 보일 때)에 맞춰 코루틴을 안전하게 실행합니다.
        viewLifecycleOwner.lifecycleScope.launch {
            // viewModel.challenges Flow에서 새로운 PagingData가 발행될 때마다,
            // collectLatest는 이전 작업을 취소하고 새 데이터로 블록을 실행합니다.
            viewModel.challenges.collectLatest { pagingData ->
                // 검색 모드가 아닐 때만 일반 목록을 표시
                if (!viewModel.isSearchMode.value) {
                    challengeAdapter.submitData(pagingData)
                }
            }
        }
    }

    /**
     * 검색 결과를 관찰하고 UI를 업데이트합니다.
     */
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

    /**
     * Adapter에 정의된 클릭 리스너들을 설정합니다.
     */
    private fun setupClickListeners() {
        challengeAdapter.onItemClickListener = { challenge ->
            Toast.makeText(context, "${challenge.challName} 클릭됨", Toast.LENGTH_SHORT).show()
            // TODO: 상세 화면으로 이동하는 로직 구현
        }

        challengeAdapter.onJoinButtonClickListener = { challenge ->
            val token = readAuthToken()
            if (token == null) {
                Toast.makeText(context, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                return@onJoinButtonClickListener
            }
            
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
        }
    }

    /**
     * SearchView를 설정하고 검색 기능을 연결합니다.
     */
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

    /**
     * 검색을 수행합니다.
     */
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

    /**
     * SharedPreferences에서 인증 토큰을 읽어옵니다.
     * @return Bearer 토큰 문자열 또는 null (로그인하지 않은 경우)
     */
    private fun readAuthToken(): String? {
        val raw = requireContext().getSharedPreferences("auth", 0).getString("token", null).orEmpty()
        if (raw.isBlank()) return null
        return if (raw.startsWith("Bearer ")) raw else "Bearer $raw"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
