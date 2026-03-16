package com.example.lifemaster.presentation.total.introspection

import com.example.lifemaster.presentation.total.introspection.viewmodel.ThankViewModel
import android.os.Bundle
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.lifemaster.R
import com.example.lifemaster.databinding.FragmentIntrospectionBinding
import androidx.fragment.app.viewModels
import com.example.lifemaster.presentation.home.calendar.view.CalendarFragment
import com.example.lifemaster.presentation.home.calendar.viewmodel.CalendarMode
import com.example.lifemaster.presentation.home.calendar.viewmodel.CalendarViewModel
import com.example.lifemaster.presentation.total.introspection.viewmodel.UiState
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class IntrospectionFragment : Fragment() {

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
            // 다이어리 수정 모드인지 감사일기 수정 모드인지 확인
            when {
                diaryId != null -> {
                    currentMode = Mode.TODAY
                    // 다이어리 수정 모드에서는 조회 API가 필요하지만, 일단 수정 기능만 연결
                    // TODO: 다이어리 조회 API 추가 필요
                }
                thankId != null -> {
                    currentMode = Mode.THANKS
                    // 기존 데이터 불러오기
                    readAuthToken()?.let { token ->
                        thankId?.let { viewModel.loadThankEntry(token, it) }
                    } ?: run {
                        Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            // 수정 모드에서는 탭 전환을 막아 혼동을 방지
            binding.btnToday.isEnabled = false
            binding.btnThanks.isEnabled = false

            // 기존 데이터 불러오기
            val token = "YOUR_TOKEN" // TODO: 실제 토큰으로 교체
            thankId?.let { viewModel.loadThankEntry(token, it) }
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

        // 수정 모드일 때 제출 버튼을 길게 누르면 삭제 다이얼로그 표시
        if (isEditMode) {
            binding.btnSubmit.setOnLongClickListener {
                showDeleteConfirmDialog()
                true
            }
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
                        val token = readAuthToken() ?: run {
                            Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }

                        if (isEditMode && diaryId != null) {
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
                        val token = readAuthToken() ?: run {
                            Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }

                        if (isEditMode && thankId != null) {
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

        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            // 버튼 활성화/비활성화 로직을 한 곳에서 관리
            binding.btnSubmit.isEnabled = state !is UiState.Loading

            when (state) {
                is UiState.Success -> {
                    // 삭제 성공 여부를 확인하기 위해 상태를 구분해야 하지만,
                    // 현재는 Success로 통일되어 있으므로 메시지만 표시
                    if (!isEditMode) {
                        Toast.makeText(requireContext(), "저장되었습니다.", Toast.LENGTH_SHORT).show()
                        clearThankYouFields() // 입력창 초기화 (수정 모드가 아닐 때만)
                    } else {
                        // 수정 모드에서는 저장/삭제 성공 후 Fragment 닫기
                        Toast.makeText(requireContext(), "완료되었습니다.", Toast.LENGTH_SHORT).show()
                        parentFragmentManager.popBackStack()
                    }
                    Toast.makeText(requireContext(), "저장되었습니다.", Toast.LENGTH_SHORT).show()
                    if (!isEditMode) {
                        clearThankYouFields() // 입력창 초기화 (수정 모드가 아닐 때만)
                    }
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
        return d.format(DateTimeFormatter.ISO_LOCAL_DATE)
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
        // ViewModel에서 "Bearer "를 추가하므로 순수 토큰만 반환
        return if (raw.startsWith("Bearer ")) raw.substring(7) else raw
    }

    private fun showDeleteConfirmDialog() {
        when {
            thankId != null -> {
                AlertDialog.Builder(requireContext())
                    .setMessage("감사일기를 삭제할까요?")
                    .setNegativeButton("취소", null)
                    .setPositiveButton("삭제") { _, _ ->
                        deleteThankEntry()
                    }
                    .show()
            }
            diaryId != null -> {
                AlertDialog.Builder(requireContext())
                    .setMessage("다이어리를 삭제할까요?")
                    .setNegativeButton("취소", null)
                    .setPositiveButton("삭제") { _, _ ->
                        deleteDiaryEntry()
                    }
                    .show()
            }
            else -> {
                Toast.makeText(requireContext(), "삭제할 항목이 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteThankEntry() {
        val token = readAuthToken() ?: run {
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
        val token = readAuthToken() ?: run {
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
