package com.example.lifemaster.presentation.total.introspection

import com.example.lifemaster.presentation.total.introspection.viewmodel.ThankViewModel
import android.os.Bundle
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.lifemaster.R
import com.example.lifemaster.databinding.FragmentIntrospectionBinding
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import com.example.lifemaster.network.TokenManager
import javax.inject.Inject
import com.example.lifemaster.presentation.home.calendar.view.CalendarFragment
import com.example.lifemaster.presentation.home.calendar.viewmodel.CalendarMode
import com.example.lifemaster.presentation.home.calendar.viewmodel.CalendarViewModel
import com.example.lifemaster.presentation.total.introspection.viewmodel.UiState
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@AndroidEntryPoint
class IntrospectionFragment : Fragment() {

    @Inject
    lateinit var tokenManager: TokenManager

    private var _binding: FragmentIntrospectionBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ThankViewModel by viewModels()
    private val calendarVM: CalendarViewModel by activityViewModels()
    private var currentMode: Mode = Mode.TODAY
    private var isEditMode = false
    private var thankId: Long? = null
    private var diaryId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val startTab = arguments?.getString("startTab") ?: "TODAY"

        currentMode = if (startTab == "THANKS") {
            Mode.THANKS
        } else {
            Mode.TODAY
        }

        arguments?.let {
            val thankIdArg = it.getLong(ARG_THANK_ID, 0L)
            val diaryIdArg = it.getLong(ARG_DIARY_ID, 0L)
            if (thankIdArg != 0L) {
                isEditMode = true
                thankId = thankIdArg
            }
            if (diaryIdArg != 0L) {
                isEditMode = true
                diaryId = diaryIdArg
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

        //수정 모드일 경우 UI 설정
        if (isEditMode) {
            binding.btnSubmit.text = "수정하기"
            // 다이어리 수정 모드인지 감사일기 수정 모드인지 확인
            when {
                diaryId != null -> {
                    currentMode = Mode.TODAY
                    // 기존 데이터 불러오기
                    tokenManager.accessToken?.let { token ->
                        diaryId?.let { viewModel.loadDiaryEntry(token, it) }
                    } ?: run {
                        Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                    }
                }
                thankId != null -> {
                    currentMode = Mode.THANKS
                    // 기존 데이터 불러오기
                    tokenManager.accessToken?.let { token ->
                        thankId?.let { viewModel.loadThankEntry(token, it) }
                    } ?: run {
                        Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            // 수정 모드에서는 탭 전환을 막아 혼동을 방지
            binding.btnToday.isEnabled = false
            binding.btnThanks.isEnabled = false
        }

        // 초기 화면 설정
        updateUI(animated = false) // 처음에는 애니메이션 없이 UI 설정

        // 달력 연결 (홈과 동일한 CalendarFragment + CalendarViewModel 사용)
        setupIntrospectionCalendar()
        observeIntrospectionCalendarDate()

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

        // 삭제 버튼 클릭 이벤트 (상단 전용 버튼)
        binding.btnDeleteTop.setOnClickListener {
            showDeleteConfirmDialog()
        }

        binding.btnSubmit.setOnClickListener {
            when (currentMode) {
                Mode.TODAY -> {
                    val text = binding.etDiary.text.toString()
                    if (text.isBlank()) {
                        Toast.makeText(requireContext(), "내용을 입력해주세요", Toast.LENGTH_SHORT).show()
                    } else {
                        // 달력에서 선택한 날짜 사용 (선택 없으면 오늘)
                        val selectedDateStr = formatSelectedDateForApi(calendarVM.selectedDate.value)
                        val token = tokenManager.accessToken ?: run {
                            Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }

                        if (diaryId != null && diaryId!! > 0) {
                            viewModel.updateDiaryEntry(
                                token = token,
                                diaryId = diaryId!!,
                                diaryContent = text,
                                diaryDate = selectedDateStr,
                                date = selectedDateStr
                            )
                        } else {
                            viewModel.createDiaryEntry(
                                token = token,
                                diaryContent = text,
                                diaryDate = selectedDateStr,
                                date = selectedDateStr
                            )
                        }

                        // 홈 달력에 자아성찰 기록 있음 표시
                        calendarVM.addIntrospectionDate(calendarVM.selectedDate.value ?: LocalDate.now())
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

                    if (thanksList.any { it.isNotBlank() }) {
                        val selectedDateStr = formatSelectedDateForApi(calendarVM.selectedDate.value)
                        val token = tokenManager.accessToken ?: run {
                            Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }

                        if (thankId != null && thankId!! > 0) {
                            viewModel.updateThankEntry(
                                token = token,
                                thankId = thankId!!,
                                thankOne = thanksList[0],
                                thankTwo = thanksList[1],
                                thankThree = thanksList[2],
                                thankFour = thanksList[3],
                                thankFive = thanksList[4]
                            )
                        } else {
                            viewModel.createThankEntry(
                                token = token,
                                thankOne = thanksList[0],
                                thankTwo = thanksList[1],
                                thankThree = thanksList[2],
                                thankFour = thanksList[3],
                                thankFive = thanksList[4],
                                thankDate = selectedDateStr
                            )
                        }

                        // 홈 달력에 자아성찰 기록 있음 표시
                        calendarVM.addIntrospectionDate(calendarVM.selectedDate.value ?: LocalDate.now())
                    } else {
                        Toast.makeText(requireContext(), "감사 내용을 한 가지 이상 입력해주세요", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // 감사일기 조회 데이터를 UI에 반영
        viewModel.thankData.observe(viewLifecycleOwner) { thankData ->
            thankData?.let {
                binding.etThanks1.setText(it.thankOne)
                binding.etThanks2.setText(it.thankTwo)
                binding.etThanks3.setText(it.thankThree)
                binding.etThanks4.setText(it.thankFour)
                binding.etThanks5.setText(it.thankFive)
            }
        }

        // 날짜별 자아성찰 조회 결과를 보고, 해당 날짜에 기록이 있으면 달력에 별 표시
        viewModel.selfReflectionByDate.observe(viewLifecycleOwner) { data ->
            val date = calendarVM.selectedDate.value ?: LocalDate.now()
            if (data == null) {
                Log.d(
                    "INTROSPECTION_DEBUG",
                    "selfReflectionByDate null (selectedDate=$date, isEditMode=$isEditMode)"
                )
                // 선택한 날짜에 데이터가 없으면 UI도 비워줍니다.
                if (!isEditMode) {
                    diaryId = null
                    thankId = null
                    binding.etDiary.setText("")
                    clearThankYouFields()
                    updateDeleteButtonVisibility()

                    // 데이터가 없으므로 달력의 별 표시(기록 표시) 제거
                    calendarVM.removeIntrospectionDate(date)
                }
                return@observe
            }

            // 조회 모드라면(수정 모드 제외) 선택 날짜의 일기/5감사를 UI에 채웁니다.
            if (!isEditMode) {
                Log.d(
                    "INTROSPECTION_DEBUG",
                    "selfReflectionByDate success (selectedDate=$date, diary=${data.diaryContent?.take(20)}, thanks=${listOf(data.thankOne, data.thankTwo, data.thankThree, data.thankFour, data.thankFive).joinToString { (it ?: "").take(10) }})"
                )
                binding.etDiary.setText(data.diaryContent ?: "")
                binding.etThanks1.setText(data.thankOne ?: "")
                binding.etThanks2.setText(data.thankTwo ?: "")
                binding.etThanks3.setText(data.thankThree ?: "")
                binding.etThanks4.setText(data.thankFour ?: "")
                binding.etThanks5.setText(data.thankFive ?: "")

                // 불러온 데이터의 ID를 저장하여 이후 '작성하기' 클릭 시 '수정' API가 호출되도록 함
                diaryId = data.diaryId
                thankId = data.thankId

                // 현재 모드에 맞춰 삭제 버튼 가시성 업데이트
                updateDeleteButtonVisibility()
            }

            val hasDiary = !data.diaryContent.isNullOrBlank()
            val hasThanks = listOf(
                data.thankOne,
                data.thankTwo,
                data.thankThree,
                data.thankFour,
                data.thankFive
            ).any { !it.isNullOrBlank() }

            if (hasDiary || hasThanks) {
                calendarVM.addIntrospectionDate(date)
            } else {
                calendarVM.removeIntrospectionDate(date)
            }
        }

        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            // 버튼 활성화/비활성화 로직을 한 곳에서 관리
            binding.btnSubmit.isEnabled = state !is UiState.Loading

            when (state) {
                is UiState.Success -> {
                    Toast.makeText(requireContext(), "완료되었습니다.", Toast.LENGTH_SHORT).show()
                    if (!isEditMode) {
                        // 저장/삭제 성공 후 데이터를 다시 불러와서 UI와 ID를 동기화
                        val token = tokenManager.accessToken
                        val dateStr = formatSelectedDateForApi(calendarVM.selectedDate.value)
                        if (token != null) {
                            viewModel.loadSelfReflectionByDate(token, dateStr)
                        }
                    } else {
                        parentFragmentManager.popBackStack()
                    }
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

        // 2. 삭제 버튼 가시성 업데이트 (현재 선택된 탭에 기록이 있을 때만 노출)
        updateDeleteButtonVisibility()

        // 3. 토글 버튼 애니메이션 및 색상 변경을 위한 목표 버튼 설정
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

    private fun updateDeleteButtonVisibility() {
        val hasData = when (currentMode) {
            Mode.TODAY -> diaryId != null && diaryId!! > 0
            Mode.THANKS -> thankId != null && thankId!! > 0
        }
        binding.btnDeleteTop.visibility = if (hasData) View.VISIBLE else View.GONE
    }

    private fun clearThankYouFields() {
        binding.etThanks1.text.clear()
        binding.etThanks2.text.clear()
        binding.etThanks3.text.clear()
        binding.etThanks4.text.clear()
        binding.etThanks5.text.clear()
    }

    /** 달력 초기화: CalendarFragment 삽입 + 월/주/일 버튼 (홈과 동일한 방식) */
    private fun setupIntrospectionCalendar() {
        if (childFragmentManager.findFragmentById(R.id.container_introspection_calendar) == null) {
            childFragmentManager.beginTransaction()
                .replace(R.id.container_introspection_calendar, CalendarFragment())
                .commit()
        }
        if (calendarVM.mode.value == null) calendarVM.setMode(CalendarMode.MONTH)
        if (calendarVM.selectedDate.value == null) calendarVM.selectDate(LocalDate.now())

        binding.btnIntrospectionMonth.setOnClickListener {
            calendarVM.setMode(CalendarMode.MONTH)
            updateIntrospectionDateButtons(CalendarMode.MONTH)
            updateIntrospectionSelectedDateText(calendarVM.mode.value ?: CalendarMode.MONTH, calendarVM.selectedDate.value ?: LocalDate.now())
        }
        binding.btnIntrospectionWeek.setOnClickListener {
            calendarVM.setMode(CalendarMode.WEEK)
            updateIntrospectionDateButtons(CalendarMode.WEEK)
            updateIntrospectionSelectedDateText(calendarVM.mode.value ?: CalendarMode.WEEK, calendarVM.selectedDate.value ?: LocalDate.now())
        }
        binding.btnIntrospectionDay.setOnClickListener {
            calendarVM.setMode(CalendarMode.DAY)
            updateIntrospectionDateButtons(CalendarMode.DAY)
            updateIntrospectionSelectedDateText(calendarVM.mode.value ?: CalendarMode.DAY, calendarVM.selectedDate.value ?: LocalDate.now())
        }
        val mode = calendarVM.mode.value ?: CalendarMode.MONTH
        val date = calendarVM.selectedDate.value ?: LocalDate.now()
        updateIntrospectionDateButtons(mode)
        updateIntrospectionSelectedDateText(mode, date)
    }

    private fun observeIntrospectionCalendarDate() {
        calendarVM.selectedDate.observe(viewLifecycleOwner) { date ->
            val mode = calendarVM.mode.value ?: CalendarMode.MONTH
            updateIntrospectionSelectedDateText(mode, date)

            // 날짜가 변경될 때 기존 입력값과 ID 초기화 (다른 날짜 데이터 혼선 방지)
            if (!isEditMode) {
                diaryId = null
                thankId = null
                binding.etDiary.setText("")
                clearThankYouFields()
                updateDeleteButtonVisibility()
            }

            // 날짜별 자아성찰 조회 → 있으면 홈/자아성찰 달력 모두에 별 표시
            val token = tokenManager.accessToken
            if (token != null) {
                val dateStr = formatSelectedDateForApi(date)
                viewModel.loadSelfReflectionByDate(token, dateStr)
            }
        }
        calendarVM.mode.observe(viewLifecycleOwner) { mode ->
            updateIntrospectionDateButtons(mode)
            updateIntrospectionSelectedDateText(mode, calendarVM.selectedDate.value ?: LocalDate.now())
        }
    }

    private fun updateIntrospectionDateButtons(selected: CalendarMode) {
        val selectedBtn = when (selected) {
            CalendarMode.MONTH -> binding.btnIntrospectionMonth
            CalendarMode.WEEK -> binding.btnIntrospectionWeek
            CalendarMode.DAY -> binding.btnIntrospectionDay
        }
        listOf(binding.btnIntrospectionMonth, binding.btnIntrospectionWeek, binding.btnIntrospectionDay).forEach { btn ->
            if (btn == selectedBtn) {
                btn.setBackgroundResource(R.drawable.bg_round_and_mint)
                btn.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
            } else {
                btn.setBackgroundResource(R.drawable.bg_calendar_unselected)
                btn.setTextColor(ContextCompat.getColor(requireContext(), R.color.mint_60))
            }
        }
    }

    private fun updateIntrospectionSelectedDateText(mode: CalendarMode, date: LocalDate) {
        binding.tvIntrospectionSelectedDate.text = when (mode) {
            CalendarMode.MONTH -> "${date.monthValue}월"
            CalendarMode.WEEK -> "${date.monthValue}월 ${weekOfMonth(date)}째주"
            CalendarMode.DAY -> "${date.monthValue}월 ${date.dayOfMonth}일"
        }
    }

    private fun weekOfMonth(date: LocalDate): Int = ((date.dayOfMonth - 1) / 7) + 1

    /** API 요청용 날짜 문자열 (yyyy-MM-dd). null이면 오늘. */
    private fun formatSelectedDateForApi(date: LocalDate?): String {
        val d = date ?: LocalDate.now()
        return d.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    enum class Mode {
        TODAY, THANKS
    }

    private fun showDeleteConfirmDialog() {
        when (currentMode) {
            Mode.TODAY -> {
                if (diaryId != null && diaryId!! > 0) {
                    AlertDialog.Builder(requireContext())
                        .setMessage("오늘의 일기를 삭제할까요?")
                        .setNegativeButton("취소", null)
                        .setPositiveButton("삭제") { _, _ ->
                            deleteDiaryEntry()
                        }
                        .show()
                } else {
                    Toast.makeText(requireContext(), "삭제할 일기가 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }
            Mode.THANKS -> {
                if (thankId != null && thankId!! > 0) {
                    AlertDialog.Builder(requireContext())
                        .setMessage("5감사를 삭제할까요?")
                        .setNegativeButton("취소", null)
                        .setPositiveButton("삭제") { _, _ ->
                            deleteThankEntry()
                        }
                        .show()
                } else {
                    Toast.makeText(requireContext(), "삭제할 5감사가 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun deleteThankEntry() {
        val token = tokenManager.accessToken ?: run {
            Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        thankId?.let { id ->
            viewModel.deleteThankEntry(token, id)
        } ?: run {
            Toast.makeText(requireContext(), "삭제할 항목이 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteDiaryEntry() {
        val token = tokenManager.accessToken ?: run {
            Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        diaryId?.let { id ->
            viewModel.deleteDiaryEntry(token, id)
        } ?: run {
            Toast.makeText(requireContext(), "삭제할 항목이 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val ARG_THANK_ID = "thank_id"
        private const val ARG_DIARY_ID = "diary_id"

        // '새로 작성' 모드로 Fragment를 열 때 사용
        fun newInstance(): IntrospectionFragment {
            return IntrospectionFragment()
        }

        // '수정' 모드로 Fragment를 열 때 사용 (수정할 감사일기의 ID 전달)
        fun newInstance(thankId: Long): IntrospectionFragment {
            val fragment = IntrospectionFragment()
            val args = Bundle()
            args.putLong(ARG_THANK_ID, thankId)
            fragment.arguments = args
            return fragment
        }

        // '수정' 모드로 Fragment를 열 때 사용 (수정할 다이어리의 ID 전달)
        fun newInstanceForDiary(diaryId: Long): IntrospectionFragment {
            val fragment = IntrospectionFragment()
            val args = Bundle()
            args.putLong(ARG_DIARY_ID, diaryId)
            fragment.arguments = args
            return fragment
        }
    }
}