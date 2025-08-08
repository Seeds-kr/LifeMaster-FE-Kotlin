package com.example.lifemaster.presentation.total.introspection

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

class IntrospectionFragment : Fragment() {

    private var _binding: FragmentIntrospectionBinding? = null
    private val binding get() = _binding!!

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

        // 버튼 클릭 이벤트
        binding.btnToday.setOnClickListener {
            currentMode = Mode.TODAY
            switchMode()

            // 배경 뷰 애니메이션
            val constraintSet = ConstraintSet()
            constraintSet.clone(binding.toggleContainer)
            constraintSet.connect(binding.toggleBackground.id, ConstraintSet.START, binding.btnToday.id, ConstraintSet.START)
            constraintSet.connect(binding.toggleBackground.id, ConstraintSet.END, binding.btnToday.id, ConstraintSet.END)
            TransitionManager.beginDelayedTransition(binding.toggleContainer)
            constraintSet.applyTo(binding.toggleContainer)

            // 텍스트 색상 변경
            binding.btnToday.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.btnThanks.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        }

        binding.btnThanks.setOnClickListener {
            currentMode = Mode.THANKS
            switchMode()

            // 배경 뷰 애니메이션
            val constraintSet = ConstraintSet()
            constraintSet.clone(binding.toggleContainer)
            constraintSet.connect(binding.toggleBackground.id, ConstraintSet.START, binding.btnThanks.id, ConstraintSet.START)
            constraintSet.connect(binding.toggleBackground.id, ConstraintSet.END, binding.btnThanks.id, ConstraintSet.END)
            TransitionManager.beginDelayedTransition(binding.toggleContainer)
            constraintSet.applyTo(binding.toggleContainer)

            // 텍스트 색상 변경
            binding.btnToday.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            binding.btnThanks.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
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

                    if (thanksList.all { it.isBlank() }) {
                        Toast.makeText(requireContext(), "감사 내용을 한 가지 이상 입력해주세요", Toast.LENGTH_SHORT).show()
                    } else {
                        // 저장 로직 (5감사)
                        Toast.makeText(requireContext(), "감사 내용이 저장되었습니다", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // 초기 화면 설정
        switchMode()
    }

private fun switchMode() {
    if (currentMode == Mode.TODAY) {
        binding.etDiary.visibility = View.VISIBLE
        binding.scrollThanksContainer.visibility = View.GONE
        // 초기 텍스트 색상 설정
        binding.btnToday.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        binding.btnThanks.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
    } else {
        binding.etDiary.visibility = View.GONE
        binding.scrollThanksContainer.visibility = View.VISIBLE
        // 초기 텍스트 색상 설정
        binding.btnToday.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        binding.btnThanks.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
    }
}

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    enum class Mode {
        TODAY, THANKS
    }
}
