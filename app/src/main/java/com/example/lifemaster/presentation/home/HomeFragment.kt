package com.example.lifemaster.presentation.home

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.lifemaster.R
import com.example.lifemaster.databinding.FragmentHomeBinding
import com.example.lifemaster.network.NetworkService
import com.example.lifemaster.network.TokenProvider
import com.example.lifemaster.presentation.home.alarm.model.AlarmModel
import com.example.lifemaster.presentation.home.alarm.model.DataResource
import com.example.lifemaster.presentation.home.alarm.model.mapper.toPresentation
import com.example.lifemaster.presentation.home.calendar.view.CalendarFragment
import com.example.lifemaster.presentation.home.calendar.viewmodel.CalendarMode
import com.example.lifemaster.presentation.home.calendar.viewmodel.CalendarViewModel
import com.example.lifemaster.presentation.home.edit.view.HomeEditActivity
import com.example.lifemaster.presentation.home.group.adapter.HomeGroupPreviewAdapter
import com.example.lifemaster.presentation.home.pomodoro.viewmodel.PomodoroViewModel
import com.example.lifemaster.presentation.home.sleep.model.Result
import com.example.lifemaster.presentation.home.sleep.model.SleepResponse
import com.example.lifemaster.presentation.home.sleep.viewmodel.SleepViewModel
import com.example.lifemaster.presentation.home.sleep.viewmodel.SleepViewModelFactory
import com.example.lifemaster.presentation.home.todo.adapter.ToDoAdapter
import com.example.lifemaster.presentation.home.todo.model.TODO
import com.example.lifemaster.presentation.home.todo.model.TodoModel
import com.example.lifemaster.presentation.home.todo.view.ToDoDialog
import com.example.lifemaster.presentation.home.todo.viewmodel.ToDoViewModel
import com.example.lifemaster.presentation.total.challenge.model.ChallengeItem
import com.example.lifemaster.presentation.total.challenge.model.toPresentation as toChallengePresentation
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {

    @Inject
    lateinit var networkService: NetworkService

    private lateinit var binding: FragmentHomeBinding

    // 할일 관련 변수
    private val toDoViewModel: ToDoViewModel by activityViewModels()
    private val todoAddDialog = ToDoDialog(origin = TODO.ADD)
    private lateinit var todoEditDialog: ToDoDialog

    // 포모도로 관련 변수
    private val pomodoroViewModel: PomodoroViewModel by activityViewModels()

    // 수면 관련 변수
    private val sleepViewModel: SleepViewModel by activityViewModels {
        SleepViewModelFactory(networkService)
    }
    private val calendarVM: CalendarViewModel by activityViewModels()

    private var tvAlarmDate: TextView? = null
    private var tvAlarmTime: TextView? = null
    private var btnAlarmSetting: View? = null

    private var tvDetoxTime: TextView? = null
    private var btnDetox: View? = null

    private lateinit var homeGroupAdapter: HomeGroupPreviewAdapter

    // 알람 미리보기용 캐시
    private var cachedAlarmList: List<AlarmModel> = emptyList()

    // 수면 미리보기용 캐시
    private var cachedSleepList: List<SleepResponse> = emptyList()

    // 디톡스 미리보기용 캐시(ms)
    private var cachedDetoxTimeMillis: Long = 0L

    // 챌린지 미리보기용 캐시
    private var cachedChallengeList: List<ChallengeItem> = emptyList()

    private var challengePreviewContainer: LinearLayout? = null
    private var challengePreviewScrollView: View? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()

        binding.containerCalendar.post {
            if (childFragmentManager.findFragmentById(R.id.container_calendar) == null) {
                childFragmentManager.beginTransaction()
                    .replace(R.id.container_calendar, CalendarFragment())
                    .commit()
            }
        }

        val (visible, ordered) = loadHomeConfiguration()
        applyHomeLayout(visible, ordered)

        bindAlarmPreviewViews()
        bindDetoxPreviewViews()
        bindChallengePreviewViews()

        setupCalendarHeader()
        observeCalendarState()
        fetchRemoteData()

        initListeners()
        initObservers()

        setupHomeCardNavigation()
        setupIntrospectionPreviewClicks()

        setupHomeGroupPreviewRecycler()
        fetchHomeJoinedGroups()

        fetchAlarmPreviewList()
        fetchSleepPreviewList()
        fetchMyChallengePreviewList()

        val today = calendarVM.selectedDate.value ?: LocalDate.now()
        loadAlarmPreviewForDate(today)
        loadSleepPreviewForDate(today)
        showDetoxPreview()
    }

    override fun onResume() {
        super.onResume()

        val (visible, ordered) = loadHomeConfiguration()
        applyHomeLayout(visible, ordered)
        rebindCalendarHeaderUI()

        bindAlarmPreviewViews()
        bindDetoxPreviewViews()
        bindChallengePreviewViews()
        setupIntrospectionPreviewClicks()
        setupHomeCardNavigation()

        ensureHomeGroupRecyclerAttached()
        fetchHomeJoinedGroups()

        fetchAlarmPreviewList()
        fetchSleepPreviewList()
        fetchMyChallengePreviewList()

        val currentDate = calendarVM.selectedDate.value ?: LocalDate.now()
        loadAlarmPreviewForDate(currentDate)
        loadSleepPreviewForDate(currentDate)
        showDetoxPreview()
    }

    private fun setupHomeCardNavigation() {
        binding.cvGoToAlarm.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_alarmListFragment)
        }

        binding.cardSleep.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_sleepPlaylistDetailFragment)
        }

        binding.cardDetox.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_detoxFragment)
        }

        binding.cardGroup.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_groupFragment)
        }

        binding.cardChallenge.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_challengeFragment)
        }
    }

    private fun bindAlarmPreviewViews() {
        tvAlarmDate = binding.root.findViewById(R.id.tv_alarm_date)
        tvAlarmTime = binding.root.findViewById(R.id.tv_alarm_time)
        btnAlarmSetting = binding.root.findViewById(R.id.btn_alarm_setting)

        btnAlarmSetting?.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_alarmListFragment)
        }
    }

    private fun bindDetoxPreviewViews() {
        tvDetoxTime = binding.root.findViewById(R.id.tv_detox_time)
        btnDetox = binding.root.findViewById(R.id.btn_detox)

        btnDetox?.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_detoxFragment)
        }
    }

    private fun bindChallengePreviewViews() {
        challengePreviewScrollView = binding.root.findViewById(R.id.sv_challenge_preview)
        challengePreviewContainer = binding.root.findViewById(R.id.layout_challenge_preview_container)
    }

    private fun setupIntrospectionPreviewClicks() {
        binding.cardIntrospection.setOnClickListener { goIntrospection("TODAY") }

        val todayCard = binding.cardIntrospection.findViewById<View>(R.id.card_go_today_diary)
        val thanksCard = binding.cardIntrospection.findViewById<View>(R.id.card_go_thanks)

        todayCard?.setOnClickListener { goIntrospection("TODAY") }
        thanksCard?.setOnClickListener { goIntrospection("THANKS") }
    }

    private fun goIntrospection(startTab: String) {
        val selectedDate = calendarVM.selectedDate.value ?: LocalDate.now()

        val args = Bundle().apply {
            putString("startTab", startTab)
            putString("selectedDate", selectedDate.toString())
        }

        findNavController().navigate(R.id.action_homeFragment_to_introspectionFragment, args)
    }

    private fun fetchRemoteData() {
        toDoViewModel.getTodoItems()
        pomodoroViewModel.getPomodoroAllItems()
    }

    private fun loadHomeConfiguration(): Pair<Set<String>, List<String>> {
        val prefs = requireContext().getSharedPreferences("home_pref", Context.MODE_PRIVATE)
        val visible = prefs.getStringSet("visible_components", null)
        val orderedString = prefs.getString("component_order", null)
        val finalVisible = visible ?: HomeConfig.DEFAULT_VISIBLE
        val finalOrdered = orderedString?.split(",") ?: HomeConfig.DEFAULT_ORDER
        return finalVisible to finalOrdered
    }

    private fun applyHomeLayout(visible: Set<String>, ordered: List<String>) {
        binding.cardCalendar.isVisible = true
        binding.cardTodo.isVisible = true

        val cardMap = mapOf(
            "sleep" to binding.cardSleep,
            "detox" to binding.cardDetox,
            "group" to binding.cardGroup,
            "introspection" to binding.cardIntrospection,
            "alarm" to binding.cvGoToAlarm,
            "challenge" to binding.cardChallenge
        )

        val parent =
            binding.root.findViewById<androidx.appcompat.widget.LinearLayoutCompat>(R.id.linearLayoutMain)
        parent.removeAllViews()

        parent.addView(binding.cardCalendar)
        parent.addView(binding.cardTodo)

        for (key in ordered) {
            val card = cardMap[key]
            if (card != null) {
                card.isVisible = visible.contains(key)
                if (visible.contains(key)) parent.addView(card)
            }
        }

        parent.addView(binding.tvHomeEdit)
    }

    private fun initViews() = with(binding) {
        todoRecyclerview.adapter = ToDoAdapter(
            context = requireContext(),
            onToggleClicked = { id ->
                toDoViewModel.toggleItem(id = id)
            },
            onEditClicked = { item ->
                todoEditDialog = ToDoDialog(origin = TODO.EDIT, item = item)
                todoEditDialog.show(childFragmentManager, ToDoDialog.TAG)
            },
            onDeleteClicked = { todoId ->
                pomodoroViewModel.deletePomodoroItemsByTodo(todoId = todoId)
            },
            onViewClicked = { item ->
                val action = HomeFragmentDirections.actionHomeFragmentToPomodoroFragment(todoItem = item)
                findNavController().navigate(action)
            }
        )

        val today = LocalDate.now()
        itemSleepPreview.tvAlarmDate.text = "${today.monthValue}월 ${today.dayOfMonth}일"
        itemSleepPreview.tvAlarmTime.text = "수면 기록 없음"
    }

    private fun initListeners() = with(binding) {
        btnAddTodoItem.setOnClickListener {
            todoAddDialog.show(childFragmentManager, ToDoDialog.TAG)
        }

        tvHomeEdit.setOnClickListener {
            val intent = Intent(requireContext(), HomeEditActivity::class.java)
            startActivity(intent)
        }

        itemSleepPreview.btnSleepReport.setOnClickListener {
            val selectedDate = calendarVM.selectedDate.value ?: LocalDate.now()

            val args = Bundle().apply {
                putString("selectedDate", selectedDate.toString())
            }

            findNavController().navigate(
                R.id.action_homeFragment_to_sleepReportFragment,
                args
            )
        }
    }

    private fun initObservers() = with(binding) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    toDoViewModel.newItem.collect { resource ->
                        when (resource) {
                            is DataResource.Error -> {
                                Toast.makeText(context, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                            }
                            DataResource.Idle -> {}
                            DataResource.Loading -> {}
                            is DataResource.Success<TodoModel> -> {
                                Toast.makeText(context, "할일이 추가되었습니다.", Toast.LENGTH_SHORT).show()
                                todoAddDialog.dismiss()
                                val newItem = resource.data
                                val oldList = (binding.todoRecyclerview.adapter as ToDoAdapter).currentList
                                val newList = oldList.toMutableList().apply {
                                    add(newItem)
                                }
                                (binding.todoRecyclerview.adapter as ToDoAdapter).submitList(newList)
                            }
                        }
                    }
                }

                launch {
                    toDoViewModel.currentItems.combine(pomodoroViewModel.allPomodoroItems) { todoRes, pomoRes ->
                        todoRes to pomoRes
                    }.collect { (todoRes, pomoRes) ->
                        if (todoRes is DataResource.Success && pomoRes is DataResource.Success) {
                            val todoItems = todoRes.data
                            val allPomodoroItems = pomoRes.data

                            val pomodoroTodoItems = todoItems.map { todoItem ->
                                val pomodoroItems = allPomodoroItems.filter { it.todo.id == todoItem.id }
                                val timer25Number = pomodoroItems.count { it.focusTime == 25 }
                                val timer50Number = pomodoroItems.count { it.focusTime == 50 }
                                todoItem.copy(timer25Number = timer25Number, timer50Number = timer50Number)
                            }
                            (binding.todoRecyclerview.adapter as ToDoAdapter).submitList(pomodoroTodoItems)

                            cachedDetoxTimeMillis =
                                allPomodoroItems.sumOf { (it.focusTime.coerceAtLeast(0)) * 60_000L }
                            showDetoxPreview()
                        } else if (todoRes is DataResource.Error) {
                            Toast.makeText(context, "할일 목록을 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
                        }

                        if (pomoRes is DataResource.Error) {
                            cachedDetoxTimeMillis = 0L
                            showNoDetox()
                        }
                    }
                }

                launch {
                    toDoViewModel.deletionState.collect { resource ->
                        when (resource) {
                            is DataResource.Error -> {
                                Toast.makeText(context, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                            }
                            DataResource.Idle -> {}
                            DataResource.Loading -> {}
                            is DataResource.Success<Int> -> {
                                val deletedAlarmId = resource.data
                                val oldList = (todoRecyclerview.adapter as ToDoAdapter).currentList
                                val updatedList = oldList.toMutableList().filterNot { it.id == deletedAlarmId }
                                (todoRecyclerview.adapter as ToDoAdapter).submitList(updatedList)
                                Toast.makeText(context, "할일이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

                launch {
                    toDoViewModel.updateItem.collect { resource ->
                        when (resource) {
                            is DataResource.Error -> {
                                Toast.makeText(context, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                            }
                            DataResource.Idle -> {}
                            DataResource.Loading -> {}
                            is DataResource.Success<TodoModel> -> {
                                val updateItem = resource.data
                                val oldList = (todoRecyclerview.adapter as ToDoAdapter).currentList
                                val updatedList = oldList.map { if (it.id == updateItem.id) updateItem else it }
                                (todoRecyclerview.adapter as ToDoAdapter).submitList(updatedList)
                                todoEditDialog.dismiss()
                                Toast.makeText(context, "할일이 수정되었습니다.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

                launch {
                    toDoViewModel.toggleItem.collect { resource ->
                        when (resource) {
                            is DataResource.Error -> {
                                Toast.makeText(context, resources.getString(R.string.server_error_message), Toast.LENGTH_SHORT).show()
                            }
                            DataResource.Idle -> {}
                            DataResource.Loading -> {}
                            is DataResource.Success<TodoModel> -> {
                                val data = resource.data
                                val oldList = (todoRecyclerview.adapter as ToDoAdapter).currentList
                                val newList = oldList.map { if (it.id == data.id) data else it }
                                (todoRecyclerview.adapter as ToDoAdapter).submitList(newList)
                                if (data.isCompleted) {
                                    Toast.makeText(context, "할일이 체크되었습니다.", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "할일이 해제되었습니다.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                }

                launch {
                    pomodoroViewModel.deletePomodoroItemsResult.collect { resource ->
                        when (resource) {
                            is DataResource.Error -> {}
                            DataResource.Idle -> {}
                            DataResource.Loading -> {}
                            is DataResource.Success<Int> -> {
                                val deleteTodoId = resource.data
                                toDoViewModel.deleteTodoItem(deleteId = deleteTodoId)
                            }
                        }
                    }
                }
            }
        }

        sleepViewModel.userSleepRecordList.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Success -> {
                    cachedSleepList = result.data
                    val selectedDate = calendarVM.selectedDate.value ?: LocalDate.now()
                    showSleepPreviewForDate(selectedDate)
                }
                is Result.Error -> {
                    cachedSleepList = emptyList()
                    showNoSleepForDate()
                }
                Result.Loading -> {}
            }
        }
    }

    // 그룹
    private fun setupHomeGroupPreviewRecycler() {
        homeGroupAdapter = HomeGroupPreviewAdapter { group ->
            val args = Bundle().apply {
                putLong("groupId", group.id)
                putString("groupName", group.name)
            }
            findNavController().navigate(R.id.action_homeFragment_to_groupStatsFragment, args)
        }

        binding.recyclerviewGroup.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = homeGroupAdapter
            setHasFixedSize(false)
            isNestedScrollingEnabled = false
        }
    }

    private fun ensureHomeGroupRecyclerAttached() {
        if (!::homeGroupAdapter.isInitialized) return

        val rv = binding.recyclerviewGroup
        if (rv.adapter !== homeGroupAdapter) {
            rv.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            rv.adapter = homeGroupAdapter
        }
        rv.setHasFixedSize(false)
        rv.isNestedScrollingEnabled = false
        rv.requestLayout()
        rv.invalidate()
    }

    private fun fetchHomeJoinedGroups() {
        if (!::homeGroupAdapter.isInitialized) return

        val raw = TokenProvider.getBearerToken(requireContext())
        if (raw.isNullOrBlank()) {
            homeGroupAdapter.submitList(emptyList())
            return
        }

        val token = if (raw.startsWith("Bearer ")) raw else "Bearer $raw"

        viewLifecycleOwner.lifecycleScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    networkService.getMyGroups(token)
                }
            }.onSuccess { list ->
                val sortedList = list.sortedBy { it.id }

                homeGroupAdapter.submitList(sortedList)

                binding.cardGroup.requestLayout()
                binding.recyclerviewGroup.requestLayout()
                binding.recyclerviewGroup.invalidate()

                if (sortedList.isNotEmpty()) homeGroupAdapter.setSelectedByIndex(0)
            }.onFailure {
                homeGroupAdapter.submitList(emptyList())
            }
        }
    }

    private fun setupCalendarHeader() = with(binding) {
        if (calendarVM.mode.value == null) calendarVM.setMode(CalendarMode.MONTH)
        if (calendarVM.selectedDate.value == null) calendarVM.selectDate(LocalDate.now())

        btnMonth.setOnClickListener {
            calendarVM.setMode(CalendarMode.MONTH)
            updateModeButtons(CalendarMode.MONTH)
            updateSelectedDateText(CalendarMode.MONTH, calendarVM.selectedDate.value ?: LocalDate.now())
        }
        btnWeek.setOnClickListener {
            calendarVM.setMode(CalendarMode.WEEK)
            updateModeButtons(CalendarMode.WEEK)
            updateSelectedDateText(CalendarMode.WEEK, calendarVM.selectedDate.value ?: LocalDate.now())
        }
        btnDay.setOnClickListener {
            calendarVM.setMode(CalendarMode.DAY)
            updateModeButtons(CalendarMode.DAY)
            updateSelectedDateText(CalendarMode.DAY, calendarVM.selectedDate.value ?: LocalDate.now())
        }

        rebindCalendarHeaderUI()
    }

    private fun observeCalendarState() = with(binding) {
        calendarVM.selectedDate.observe(viewLifecycleOwner) { date ->
            val mode = calendarVM.mode.value ?: CalendarMode.MONTH
            updateSelectedDateText(mode, date)
            loadAlarmPreviewForDate(date)
            loadSleepPreviewForDate(date)
        }

        calendarVM.mode.observe(viewLifecycleOwner) { mode ->
            updateModeButtons(mode)
            updateSelectedDateText(mode, calendarVM.selectedDate.value ?: LocalDate.now())
        }
    }

    private fun rebindCalendarHeaderUI() {
        val mode = calendarVM.mode.value ?: CalendarMode.MONTH
        val date = calendarVM.selectedDate.value ?: LocalDate.now()
        updateModeButtons(mode)
        updateSelectedDateText(mode, date)
    }

    private fun updateModeButtons(selected: CalendarMode) = with(binding) {
        val selectedBtn = when (selected) {
            CalendarMode.MONTH -> btnMonth
            CalendarMode.WEEK -> btnWeek
            CalendarMode.DAY -> btnDay
        }
        val buttons = listOf(btnMonth, btnWeek, btnDay)
        buttons.forEach { btn ->
            if (btn == selectedBtn) {
                btn.setBackgroundResource(R.drawable.bg_round_and_mint)
                btn.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
            } else {
                btn.setBackgroundResource(R.drawable.bg_calendar_unselected)
                btn.setTextColor(ContextCompat.getColor(requireContext(), R.color.mint_60))
            }
        }
    }

    private fun updateSelectedDateText(mode: CalendarMode, date: LocalDate) {
        binding.tvSelectedDate.text = when (mode) {
            CalendarMode.MONTH -> "${date.monthValue}월"
            CalendarMode.WEEK -> "${date.monthValue}월 ${weekOfMonth(date)}째주"
            CalendarMode.DAY -> "${date.monthValue}월 ${date.dayOfMonth}일"
        }
    }

    private fun weekOfMonth(date: LocalDate): Int = ((date.dayOfMonth - 1) / 7) + 1

    // -------------------------------
    // 알람 미리보기
    // -------------------------------
    private fun fetchAlarmPreviewList() {
        viewLifecycleOwner.lifecycleScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    networkService.fetchAlarmList()
                }
            }.onSuccess { responseList ->
                cachedAlarmList = responseList.map { it.toPresentation() }
                val selectedDate = calendarVM.selectedDate.value ?: LocalDate.now()
                showAlarmPreviewForDate(selectedDate)
            }.onFailure {
                cachedAlarmList = emptyList()
                val selectedDate = calendarVM.selectedDate.value ?: LocalDate.now()
                showNoAlarmForDate(selectedDate)
            }
        }
    }

    private fun loadAlarmPreviewForDate(date: LocalDate) {
        tvAlarmDate?.text = "${date.monthValue}월 ${date.dayOfMonth}일"

        if (cachedAlarmList.isEmpty()) {
            showNoAlarmForDate(date)
            return
        }

        showAlarmPreviewForDate(date)
    }

    private fun showAlarmPreviewForDate(date: LocalDate) {
        val firstAlarm = cachedAlarmList
            .filter { it.switchOnOff }
            .filter { isAlarmScheduledOnDate(it, date) }
            .sortedWith(compareBy<AlarmModel> { it.hour }.thenBy { it.minute })
            .firstOrNull()

        if (firstAlarm == null) {
            showNoAlarmForDate(date)
            return
        }

        tvAlarmTime?.text = firstAlarm.toPreviewKoreanTime()
    }

    private fun isAlarmScheduledOnDate(alarm: AlarmModel, date: LocalDate): Boolean {
        return when (date.dayOfWeek) {
            DayOfWeek.MONDAY -> alarm.alarmMon
            DayOfWeek.TUESDAY -> alarm.alarmTue
            DayOfWeek.WEDNESDAY -> alarm.alarmWed
            DayOfWeek.THURSDAY -> alarm.alarmThu
            DayOfWeek.FRIDAY -> alarm.alarmFri
            DayOfWeek.SATURDAY -> alarm.alarmSat
            DayOfWeek.SUNDAY -> alarm.alarmSun
        }
    }

    private fun showNoAlarmForDate(date: LocalDate) {
        val isToday = date == LocalDate.now()
        tvAlarmTime?.text = if (isToday) "오늘 알람 없음" else "알람 없음"
    }

    private fun AlarmModel.toPreviewKoreanTime(): String {
        val hour24 = hour
        val minuteText = "%02d".format(minute)

        return when {
            hour24 == 0 -> "오전 12:$minuteText"
            hour24 < 12 -> "오전 $hour24:$minuteText"
            hour24 == 12 -> "오후 12:$minuteText"
            else -> "오후 ${hour24 - 12}:$minuteText"
        }
    }

    // -------------------------------
    // 수면 미리보기
    // -------------------------------
    private fun fetchSleepPreviewList() {
        val raw = TokenProvider.getBearerToken(requireContext())
        if (raw.isNullOrBlank()) {
            cachedSleepList = emptyList()
            showNoSleepForDate()
            return
        }

        val token = if (raw.startsWith("Bearer ")) raw else "Bearer $raw"

        viewLifecycleOwner.lifecycleScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    networkService.getMe(token)
                }
            }.onSuccess { response ->
                val me = response.body()
                val userId = me?.id

                if (userId != null) {
                    sleepViewModel.getUserSleepInfo(userId.toInt())
                } else {
                    cachedSleepList = emptyList()
                    showNoSleepForDate()
                }
            }.onFailure {
                cachedSleepList = emptyList()
                showNoSleepForDate()
            }
        }
    }

    private fun loadSleepPreviewForDate(date: LocalDate) {
        binding.itemSleepPreview.tvAlarmDate.text = "${date.monthValue}월 ${date.dayOfMonth}일"

        if (cachedSleepList.isEmpty()) {
            showNoSleepForDate()
            return
        }

        showSleepPreviewForDate(date)
    }

    private fun showSleepPreviewForDate(date: LocalDate) {
        val targetDate = date.toString()

        val sleep = cachedSleepList.firstOrNull { it.sleepDate == targetDate }

        if (sleep == null) {
            showNoSleepForDate()
            return
        }

        binding.itemSleepPreview.tvAlarmTime.text = "${sleep.sleepDurationText} 수면"
    }

    private fun showNoSleepForDate() {
        binding.itemSleepPreview.tvAlarmTime.text = "수면 기록 없음"
    }

    // -------------------------------
    // 디톡스 미리보기
    // -------------------------------
    private fun showDetoxPreview() {
        if (cachedDetoxTimeMillis <= 0L) {
            showNoDetox()
            return
        }

        tvDetoxTime?.text = cachedDetoxTimeMillis.toDetoxTimeText()
    }

    private fun showNoDetox() {
        tvDetoxTime?.text = "집중 기록 없음"
    }

    private fun Long.toDetoxTimeText(): String {
        val totalMinutes = this / 1000L / 60L
        val hour = totalMinutes / 60L
        val minute = totalMinutes % 60L
        return "${hour}시간 ${minute}분"
    }

    // -------------------------------
    // 챌린지 미리보기
    // -------------------------------
    private fun fetchMyChallengePreviewList() {
        val raw = TokenProvider.getBearerToken(requireContext())
        if (raw.isNullOrBlank()) {
            cachedChallengeList = emptyList()
            renderChallengePreview(emptyList())
            return
        }

        val token = if (raw.startsWith("Bearer ")) raw else "Bearer $raw"

        viewLifecycleOwner.lifecycleScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    networkService.getMyChallengeList(token)
                }
            }.onSuccess { response ->
                cachedChallengeList = response.map { it.toChallengePresentation() }
                renderChallengePreview(cachedChallengeList)
            }.onFailure {
                cachedChallengeList = emptyList()
                renderChallengePreview(emptyList())
            }
        }
    }

    private fun renderChallengePreview(challenges: List<ChallengeItem>) {
        val container = challengePreviewContainer ?: return

        container.removeAllViews()

        if (challenges.isEmpty()) {
            challengePreviewScrollView?.isVisible = false
            return
        }

        challengePreviewScrollView?.isVisible = true

        challenges.forEachIndexed { index, challenge ->
            val itemView = layoutInflater.inflate(
                R.layout.item_home_challenge_preview,
                container,
                false
            )

            val root = itemView.findViewById<LinearLayout>(R.id.layout_challenge_preview_item)
            val frame = itemView.findViewById<FrameLayout>(R.id.frame_challenge_preview_bg)
            val bg = itemView.findViewById<ImageView>(R.id.iv_challenge_preview_bg)
            val icon = itemView.findViewById<ImageView>(R.id.iv_challenge_preview_icon)
            val title = itemView.findViewById<TextView>(R.id.tv_challenge_preview_name)

            title.text = challenge.challName
            title.setTextColor(Color.parseColor("#BDBDBD"))

            bg.setImageResource(R.drawable.bg_circle_default)

            if (challenge.challImg.isNotBlank()) {
                Glide.with(icon.context)
                    .load(challenge.challImg)
                    .error(getFallbackChallengeIconRes(challenge.challName))
                    .circleCrop()
                    .into(icon)
            } else {
                Glide.with(icon.context)
                    .load(getFallbackChallengeIconRes(challenge.challName))
                    .circleCrop()
                    .into(icon)
            }

            root.setOnClickListener {
                val args = Bundle().apply {
                    putLong("challId", challenge.challId)
                }
                findNavController().navigate(
                    R.id.action_homeFragment_to_challengeDetailFragment,
                    args
                )
            }

            frame.setOnClickListener {
                val args = Bundle().apply {
                    putLong("challId", challenge.challId)
                }
                findNavController().navigate(
                    R.id.action_homeFragment_to_challengeDetailFragment,
                    args
                )
            }

            itemView.setOnClickListener {
                val args = Bundle().apply {
                    putLong("challId", challenge.challId)
                }
                findNavController().navigate(
                    R.id.action_homeFragment_to_challengeDetailFragment,
                    args
                )
            }

            if (index == challenges.lastIndex) {
                val lp = itemView.layoutParams as? ViewGroup.MarginLayoutParams
                lp?.marginEnd = 0
                itemView.layoutParams = lp
            }

            container.addView(itemView)
        }
    }

    private fun getFallbackChallengeIconRes(challName: String): Int {
        val key = challName.lowercase()

        return when {
            key.contains("샤워") || key.contains("찬물") -> R.drawable.ic_shower
            key.contains("스트레칭") -> R.drawable.ic_stretching
            else -> R.drawable.ic_shower
        }
    }
}