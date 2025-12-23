package com.example.lifemaster.presentation.total.introspection

import com.example.lifemaster.presentation.total.introspection.viewmodel.ThankViewModel
import android.os.Bundle
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.lifemaster.R
import com.example.lifemaster.databinding.FragmentIntrospectionBinding
import androidx.fragment.app.viewModels
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class IntrospectionFragment : Fragment() {

    private var _binding: FragmentIntrospectionBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ThankViewModel by viewModels()
    private var currentMode: Mode = Mode.TODAY
    private var isEditMode = false
    private var thankId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val id = it.getLong(ARG_THANK_ID, 0L)
            if (id != 0L) {
                isEditMode = true
                thankId = id
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIntrospectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //수정 모드일 경우 UI를 '감사일기'로 강제 설정
        if (isEditMode) {
            currentMode = Mode.THANKS
            // 수정 모드에서는 탭 전환을 막아 혼동을 방지
            binding.btnToday.isEnabled = false
            binding.btnThanks.isEnabled = false

            // 기존 데이터 불러오기
            readAuthToken()?.let { token ->
                thankId?.let { viewModel.loadThankEntry(token, it) }
            } ?: run {
                Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }

        // 초기 화면 설정
        updateUI(animated = false) // 처음에는 애니메이션 없이 UI 설정

        // 버튼 클릭 이벤트
        binding.btnToday.setOnClickListener {
            if (currentMode != Mode.TODAY) {
                currentMode = Mode.TODAY
                updateUI(animated = true)
            }
        }

        binding.btnThanks.setOnClickListener {
            if (currentMode != Mode.THANKS) {
                currentMode = Mode.THANKS
                updateUI(animated = true)
            }
        }

        binding.btnSubmit.setOnClickListener {
            when (currentMode) {
                Mode.TODAY -> {
                    val text = binding.etDiary.text.toString()
                    if (text.isBlank()) {
                        Toast.makeText(requireContext(), "내용을 입력해주세요", Toast.LENGTH_SHORT).show()
                    } else {
                        // 저장 로직 (오늘의 일기)
                        Toast.makeText(requireContext(), "일기가 저장되었습니다", Toast.LENGTH_SHORT).show()
                    }
                }

                Mode.THANKS -> {
                    val thanksList = listOf(
                        binding.etThanks1.text.toString(),
                        binding.etThanks2.text.toString(),
                        binding.etThanks3.text.toString(),
                        binding.etThanks4.text.toString(),
                        binding.etThanks5.text.toString()
                    )

                    // "비어있지 않은 항목이 하나라도 있는가?"를 직접적으로 확인
                    if (thanksList.any { it.isNotBlank() }) {
                        // 현재 날짜를 "yyyy-MM-dd" 형식의 문자열로 변환
                        val currentDate =
                            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                        val token = readAuthToken() ?: run {
                            Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }

                        // ViewModel의 함수를 호출하여 서버에 데이터 전송 요청
                        viewModel.createThankEntry(
                            token = "YOUR_TOKEN",
                            thankOne = thanksList[0],
                            thankTwo = thanksList[1],
                            thankThree = thanksList[2],
                            thankFour = thanksList[3],
                            thankFive = thanksList[4],
                            thankDate = currentDate
                        )
                    } else {
                        Toast.makeText(requireContext(), "감사 내용을 한 가지 이상 입력해주세요", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            // 버튼 활성화/비활성화 로직을 한 곳에서 관리
            binding.btnSubmit.isEnabled = state !is UiState.Loading

            when (state) {
                is UiState.Success -> {
                    Toast.makeText(requireContext(), "저장되었습니다.", Toast.LENGTH_SHORT).show()
                    clearThankYouFields() // 입력창 초기화
                    // TODO: 저장이 완료되면 현재 Fragment를 닫는 로직 추가 (필요시)
                    // 예: parentFragmentManager.popBackStack()
                }

                is UiState.Error -> {
                    Toast.makeText(requireContext(), "오류: ${state.message}", Toast.LENGTH_SHORT).show()
                }
                // Loading, Idle 상태는 버튼 활성화 로직에서 이미 처리됨
                else -> {}
            }
        }
    }

    private fun updateUI(animated: Boolean) {
        // 1. 입력창 가시성 변경
        binding.etDiary.visibility = if (currentMode == Mode.TODAY) View.VISIBLE else View.GONE
        binding.scrollThanksContainer.visibility = if (currentMode == Mode.THANKS) View.VISIBLE else View.GONE

        // 2. 토글 버튼 애니메이션 및 색상 변경을 위한 목표 버튼 설정
        val targetButton = if (currentMode == Mode.TODAY) binding.btnToday else binding.btnThanks

        // 3. ConstraintSet을 이용한 배경 뷰 애니메이션
        val constraintSet = ConstraintSet()
        constraintSet.clone(binding.toggleContainer)
        constraintSet.connect(binding.toggleBackground.id, ConstraintSet.START, targetButton.id, ConstraintSet.START)
        constraintSet.connect(binding.toggleBackground.id, ConstraintSet.END, targetButton.id, ConstraintSet.END)

        if (animated) {
            TransitionManager.beginDelayedTransition(binding.toggleContainer)
        }
        constraintSet.applyTo(binding.toggleContainer)

        // 4. 텍스트 색상 변경
        binding.btnToday.setTextColor(ContextCompat.getColor(requireContext(), if (currentMode == Mode.TODAY) R.color.white else R.color.black))
        binding.btnThanks.setTextColor(ContextCompat.getColor(requireContext(), if (currentMode == Mode.THANKS) R.color.white else R.color.black))
    }

    private fun clearThankYouFields() {
        binding.etThanks1.text.clear()
        binding.etThanks2.text.clear()
        binding.etThanks3.text.clear()
        binding.etThanks4.text.clear()
        binding.etThanks5.text.clear()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    enum class Mode {
        TODAY, THANKS
    }

    private fun readAuthToken(): String? {
        val raw = requireContext().getSharedPreferences("auth", 0).getString("token", null).orEmpty()
        if (raw.isBlank()) return null
        return if (raw.startsWith("Bearer ")) raw else "Bearer $raw"
    }

    companion object {
        private const val ARG_THANK_ID = "thank_id"

        // '새로 작성' 모드로 Fragment를 열 때 사용
        fun newInstance(): IntrospectionFragment {
            return IntrospectionFragment()
        }

        // '수정' 모드로 Fragment를 열 때 사용 (수정할 일기의 ID 전달)
        fun newInstance(thankId: Long): IntrospectionFragment {
            val fragment = IntrospectionFragment()
            val args = Bundle()
            args.putLong(ARG_THANK_ID, thankId)
            fragment.arguments = args
            return fragment
        }
    }
}
