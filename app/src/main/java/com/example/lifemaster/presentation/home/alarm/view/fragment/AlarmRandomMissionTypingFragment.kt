package com.example.lifemaster.presentation.home.alarm.view.fragment

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.lifemaster.R
import com.example.lifemaster.databinding.FragmentAlarmRandomMissionTypingBinding
import com.example.lifemaster.presentation.home.alarm.model.DataResource
import com.example.lifemaster.presentation.home.alarm.view.service.AlarmService
import com.example.lifemaster.presentation.home.alarm.viewmodel.AlarmMissionViewModel
import com.google.android.material.card.MaterialCardView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDateTime

@AndroidEntryPoint
class AlarmRandomMissionTypingFragment : Fragment(R.layout.fragment_alarm_random_mission_typing) {

    private lateinit var binding: FragmentAlarmRandomMissionTypingBinding
    private val alarmMissionViewModel: AlarmMissionViewModel by activityViewModels()
    private val questionList by lazy {
        listOf(binding.tvQuestionFirst, binding.tvQuestionSecond, binding.tvQuestionThird)
    }
    private val answerList by lazy {
        listOf(binding.etAnswerFirst, binding.etAnswerSecond, binding.etAnswerThird)
    }

    private val currentPage by lazy {
        val args: AlarmRandomMissionMathFragmentArgs by navArgs()
        args.currentPageNum
    }

    private val alarmItem by lazy {
        val args: AlarmRandomMissionMathFragmentArgs by navArgs()
        args.alarmItem
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAlarmRandomMissionTypingBinding.bind(view)
        setupBackPressHandler()
        initViews()
        fetchRemoteData()
        initObservers()
        initListeners()
    }

    private fun setupBackPressHandler() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    Toast.makeText(context, "뒤로 갈 수 없습니다!", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun initViews() = with(binding) {
        val localDateTime = LocalDateTime.now()
        tvAlarmRandomMissionTypingDate.text = "${localDateTime.year}년 ${localDateTime.monthValue}월 ${localDateTime.dayOfMonth}일"
        tvAlarmRandomMissionTypingTime.text = String.format("%02d:%02d", localDateTime.hour, localDateTime.minute)
        tvAlarmRandomMissionTypingAmPm.text = if(localDateTime.hour in 0..11) "am" else "pm"
    }

    private fun fetchRemoteData() {
        alarmMissionViewModel.generateTypingSentence(count = questionList.size, alarmId = alarmItem.id)
    }

    private fun initObservers() = with(binding) {
        answerList.forEachIndexed { position, answerEditText ->
            answerEditText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
                override fun afterTextChanged(currentEditText: Editable) {
                    val size = currentEditText.length
                    val parentCardView = answerEditText.parent as MaterialCardView
                    if (currentEditText.contentEquals(questionList[position].text.toString().take(size))
                    ) {
                        parentCardView.strokeWidth = resources.getDimensionPixelSize(R.dimen.text_watcher_success)
                        answerEditText.setTextColor(getColor(requireContext(), R.color.edit_text_hint))
                    } else {
                        parentCardView.strokeColor = getColor(requireContext(), R.color.red_100)
                        parentCardView.strokeWidth = resources.getDimensionPixelSize(R.dimen.text_watcher_error)
                        answerEditText.setTextColor(getColor(requireContext(), R.color.red_100))
                    }
                }
            })
            answerEditText.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                if(hasFocus) {
                    if(answerEditText.text.isEmpty()) alarmMissionViewModel.increaseSentenceCount()
                } else {
                    if(answerEditText.text.isEmpty()) alarmMissionViewModel.decreaseSentenceCount()
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    alarmMissionViewModel.typingSentenceInfo.collect { resource ->
                        when (resource) {
                            is DataResource.Error -> {}
                            DataResource.Idle -> {}
                            DataResource.Loading -> {}
                            is DataResource.Success<List<String>> -> {
                                val sentences: List<String> = resource.data
                                sentences.forEachIndexed { position, sentence ->
                                    questionList[position].text = sentence
                                }
                            }
                        }
                    }
                }
                launch {
                    alarmMissionViewModel.writtenSentenceCount.collect { currentCount ->
                        btnRandomMissionTypingNextPage.text = "${currentCount}/${TOTAL_SENTENCE_COUNT} 진행 중"
                    }
                }
            }
        }
    }

    private fun initListeners() = with(binding) {
        btnRandomMissionTypingNextPage.setOnClickListener {
            if (etAnswerFirst.text.toString() == tvQuestionFirst.text.toString()
                && etAnswerSecond.text.toString() == tvQuestionSecond.text.toString()
                && etAnswerThird.text.toString() == tvQuestionThird.text.toString()
            ) {
                if(currentPage < 5) {
                    val action = AlarmRandomMissionTypingFragmentDirections.actionAlarmRandomMissionTypingFragmentSelf(currentPageNum = currentPage + 1, alarmItem = alarmItem)
                    findNavController().navigate(action)
                } else {
                    Toast.makeText(context, "수고하셨습니다!", Toast.LENGTH_SHORT).show()
//                    alarmMissionViewModel.clearData()
//                    sleepViewModel.getUserSleepInfo(Constants.USER_ID) 수면 연동
                    requireActivity().stopService(Intent(requireContext(), AlarmService::class.java))
                    // TODO: 현재 액티비티 끄고 메인 액티비티 화면으로 이동하기 (nav_graph_main 연결??)
                    // TODO: 만약 알람이 일회성 알람인 경우 (반복 요일이 없는 경우) 스위치 상태 OFF 로 변경하기 (업데이트) + 세부 화면 들어가면 이미 꺼진 알람입니다.. (이미 지난 시간인 지 구별하는 지금보다 더 명확한 로직 작성 필요)
                    // TODO: 추가 기능 작성하기
                }
            } else if (etAnswerFirst.text.isBlank() || etAnswerSecond.text.isBlank() || etAnswerThird.text.isBlank()) {
                Toast.makeText(context, "아직 입력하지 않은 문장이 있습니다!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "문장을 정확하게 입력해주세요!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val TOTAL_SENTENCE_COUNT = 15
    }

}