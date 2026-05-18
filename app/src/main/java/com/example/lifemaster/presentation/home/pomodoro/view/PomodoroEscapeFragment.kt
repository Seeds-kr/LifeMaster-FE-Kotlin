package com.example.lifemaster.presentation.home.pomodoro.view

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat.getColor
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.lifemaster.R
import com.example.lifemaster.databinding.FragmentPomodoroEscapeBinding
import com.example.lifemaster.presentation.home.alarm.model.DataResource
import com.example.lifemaster.presentation.home.pomodoro.model.PomodoroButtonStatus
import com.example.lifemaster.presentation.home.pomodoro.model.PomodoroTimeType
import com.example.lifemaster.presentation.home.pomodoro.viewmodel.PomodoroViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDateTime

@AndroidEntryPoint
class PomodoroEscapeFragment : Fragment(R.layout.fragment_pomodoro_escape) {

    private lateinit var binding: FragmentPomodoroEscapeBinding
    private val args: PomodoroEscapeFragmentArgs by navArgs()
    private val pomodoroViewModel: PomodoroViewModel by activityViewModels()

    private val questionList by lazy {
        listOf(
            binding.tvPomodoroEscapeQuestionFirst,
            binding.tvPomodoroEscapeQuestionSecond,
            binding.tvPomodoroEscapeQuestionThird
        )
    }

    private val answerList by lazy {
        listOf(
            binding.etPomodoroEscapeAnswerFirst,
            binding.etPomodoroEscapeAnswerSecond,
            binding.etPomodoroEscapeAnswerThird
        )
    }

    private var currentPage: Int = 1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentPomodoroEscapeBinding.bind(view)

        initData()
        setupBackPressHandler()
        initViews()
        fetchData()
        initObservers()
        initListeners()
    }

    private fun initData() {
        currentPage = args.currentPageNum
    }

    private fun setupBackPressHandler() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    Toast.makeText(context, "뒤로 갈 수 없습니다!", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun initViews() = with(binding) {
        val localDateTime = LocalDateTime.now()

        tvPomodoroEscapeTypingDate.text =
            "${localDateTime.year}년 ${localDateTime.monthValue}월 ${localDateTime.dayOfMonth}일"

        tvPomodoroEscapeTypingTime.text =
            String.format("%02d:%02d", localDateTime.hour, localDateTime.minute)

        tvPomodoroEscapeTypingAmPm.text =
            if (localDateTime.hour in 0..11) "am" else "pm"

        requireActivity()
            .findViewById<BottomNavigationView>(R.id.bottomNavigation)
            .isVisible = false
    }

    private fun initListeners() = with(binding) {
        btnPomodoroEscapeTypingNextPage.setOnClickListener {
            val isCorrect =
                etPomodoroEscapeAnswerFirst.text.toString() == tvPomodoroEscapeQuestionFirst.text.toString() &&
                        etPomodoroEscapeAnswerSecond.text.toString() == tvPomodoroEscapeQuestionSecond.text.toString() &&
                        etPomodoroEscapeAnswerThird.text.toString() == tvPomodoroEscapeQuestionThird.text.toString()

            val hasBlank =
                etPomodoroEscapeAnswerFirst.text.isBlank() ||
                        etPomodoroEscapeAnswerSecond.text.isBlank() ||
                        etPomodoroEscapeAnswerThird.text.isBlank()

            when {
                isCorrect -> {
                    if (currentPage < 2) {
                        val action =
                            PomodoroEscapeFragmentDirections.actionPomodoroEscapeFragmentSelf(
                                currentPageNum = currentPage + 1
                            )
                        findNavController().navigate(action)
                    } else {
                        Toast.makeText(context, "비상 탈출을 완료했습니다!", Toast.LENGTH_SHORT).show()
                        pomodoroViewModel.clearData()
                        pomodoroViewModel.pomodoroStatus = PomodoroButtonStatus.TODO
                        pomodoroViewModel.pomodoroTimeType = PomodoroTimeType.NONE
                        findNavController().popBackStack()
                    }
                }

                hasBlank -> {
                    Toast.makeText(context, "아직 입력하지 않은 문장이 있습니다!", Toast.LENGTH_SHORT).show()
                }

                else -> {
                    Toast.makeText(context, "문장을 정확하게 입력해주세요!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun fetchData() {
        pomodoroViewModel.getPomodoroEscapeSentence(count = questionList.size)
    }

    private fun initObservers() = with(binding) {
        answerList.forEachIndexed { position, answerEditText ->
            answerEditText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) = Unit

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) = Unit

                override fun afterTextChanged(currentEditText: Editable) {
                    val size = currentEditText.length
                    val parentCardView = answerEditText.parent as MaterialCardView
                    val expectedText = questionList[position].text.toString().take(size)

                    if (currentEditText.contentEquals(expectedText)) {
                        parentCardView.strokeWidth =
                            resources.getDimensionPixelSize(R.dimen.text_watcher_success)

                        answerEditText.setTextColor(
                            getColor(requireContext(), R.color.edit_text_hint)
                        )
                    } else {
                        parentCardView.strokeWidth =
                            resources.getDimensionPixelSize(R.dimen.text_watcher_error)

                        parentCardView.strokeColor =
                            getColor(requireContext(), R.color.red_100)

                        answerEditText.setTextColor(
                            getColor(requireContext(), R.color.red_100)
                        )
                    }
                }
            })

            answerEditText.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    if (answerEditText.text.isEmpty()) {
                        pomodoroViewModel.increaseSentenceCount()
                    }
                } else {
                    if (answerEditText.text.isEmpty()) {
                        pomodoroViewModel.decreaseSentenceCount()
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    pomodoroViewModel.escapeSentences.collect { resource ->
                        when (resource) {
                            is DataResource.Error -> Unit
                            DataResource.Idle -> Unit
                            DataResource.Loading -> Unit

                            is DataResource.Success<List<String>> -> {
                                val sentences = resource.data

                                sentences.forEachIndexed { position, sentence ->
                                    questionList.getOrNull(position)?.text = sentence
                                }
                            }
                        }
                    }
                }

                launch {
                    pomodoroViewModel.writtenSentenceCount.collect { currentCount ->
                        btnPomodoroEscapeTypingNextPage.text =
                            "${currentCount}/${TOTAL_SENTENCE_COUNT} 진행 중"
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        requireActivity()
            .findViewById<BottomNavigationView>(R.id.bottomNavigation)
            .isVisible = true
    }

    companion object {
        private const val TOTAL_SENTENCE_COUNT = 6
    }
}