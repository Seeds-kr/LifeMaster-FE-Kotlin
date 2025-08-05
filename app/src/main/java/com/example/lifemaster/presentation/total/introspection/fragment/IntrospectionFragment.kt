package com.example.lifemaster.presentation.total.introspection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
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
            binding.etDiary.hint = "오늘 하루 동안 있었던 일을 적어보세요"
        }

        binding.btnThanks.setOnClickListener {
            currentMode = Mode.THANKS
            binding.etDiary.hint = "오늘 하루 감사한 일을 적어보세요"
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
    } else {
        binding.etDiary.visibility = View.GONE
        binding.scrollThanksContainer.visibility = View.VISIBLE
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
