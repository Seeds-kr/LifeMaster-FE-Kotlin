package com.example.lifemaster.presentation.total.introspection

import ThankViewModel
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIntrospectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
                        Toast.makeText(
                            requireContext(),
                            "감사 내용을 한 가지 이상 입력해주세요",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.btnSubmit.isEnabled = false
                }

                is UiState.Success -> {
                    binding.btnSubmit.isEnabled = true
                    Toast.makeText(requireContext(), "감사일기가 저장되었습니다.", Toast.LENGTH_SHORT).show()
                }

                is UiState.Error -> {
                    binding.btnSubmit.isEnabled = true
                    Toast.makeText(requireContext(), "오류: ${state.message}", Toast.LENGTH_SHORT)
                        .show()
                }

                else -> {
                    // Loading, Success, Error가 아닌 나머지 모든 경우 (여기서는 Idle)
                    binding.btnSubmit.isEnabled = true
                }
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



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    enum class Mode {
        TODAY, THANKS
    }
}
