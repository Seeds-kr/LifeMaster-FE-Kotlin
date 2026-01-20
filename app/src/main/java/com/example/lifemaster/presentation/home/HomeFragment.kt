package com.example.lifemaster.presentation.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.lifemaster.R
import com.example.lifemaster.databinding.FragmentHomeBinding
import com.example.lifemaster.network.RetrofitInstance
import com.example.lifemaster.presentation.home.calendar.view.CalendarFragment
import com.example.lifemaster.presentation.home.calendar.viewmodel.CalendarMode
import com.example.lifemaster.presentation.home.calendar.viewmodel.CalendarViewModel
import com.example.lifemaster.presentation.home.edit.view.HomeEditActivity
import com.example.lifemaster.presentation.home.pomodoro.model.PomodoroItem
import com.example.lifemaster.presentation.home.todo.adapter.ToDoAdapter
import com.example.lifemaster.presentation.home.todo.model.TODO
import com.example.lifemaster.presentation.home.todo.model.TodoItem
import com.example.lifemaster.presentation.home.todo.view.ToDoDialog
import com.example.lifemaster.presentation.home.todo.viewmodel.ToDoViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate
import java.time.format.DateTimeFormatterBuilder

class HomeFragment : Fragment() {

    lateinit var binding: FragmentHomeBinding
    lateinit var todoItems: ArrayList<TodoItem>
    private val toDoViewModel: ToDoViewModel by activityViewModels()
    private var userToken: String? = null
    private val calendarVM: CalendarViewModel by activityViewModels()

    private var tvAlarmDate: TextView? = null
    private var tvAlarmTime: TextView? = null
    private var btnAlarmSetting: View? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater)
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

        setupCalendarHeader()
        observeCalendarState()
        initListeners()
        initObservers()

        setupHomeCardNavigation()

        setupIntrospectionPreviewClicks()

        val today = calendarVM.selectedDate.value ?: LocalDate.now()
        loadAlarmPreviewForDate(today)
    }

    override fun onResume() {
        super.onResume()

        val (visible, ordered) = loadHomeConfiguration()
        applyHomeLayout(visible, ordered)
        rebindCalendarHeaderUI()

        bindAlarmPreviewViews()
        setupIntrospectionPreviewClicks()

        setupHomeCardNavigation()

        val currentDate = calendarVM.selectedDate.value ?: LocalDate.now()
        loadAlarmPreviewForDate(currentDate)
    }

    private fun setupHomeCardNavigation() {
        // 알람 화면 이동
        binding.cvGoToAlarm.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_alarmListFragment)
        }

        // 수면 화면 이동
        binding.cardSleep.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_sleepReportFragment)
        }

        // 디톡스 화면 이동
        binding.cardDetox.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_detoxFragment)
        }
        // 그룹 화면 이동
        binding.cardGroup.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_groupFragment)
        }

        // 챌린지
        binding.cardChallenge.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_challengeFragment)
        }
    }

    private fun bindAlarmPreviewViews() {
        tvAlarmDate = binding.root.findViewById(R.id.tv_alarm_date)
        tvAlarmTime = binding.root.findViewById(R.id.tv_alarm_time)
        btnAlarmSetting = binding.root.findViewById(R.id.btn_alarm_setting)
        btnAlarmSetting?.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_alarmSettingFragment)
        }
    }

    private fun setupIntrospectionPreviewClicks() {
        binding.cardIntrospection.setOnClickListener { goIntrospection("TODAY") }

        val todayCard = binding.cardIntrospection.findViewById<View?>(R.id.card_go_today_diary)
        val thanksCard = binding.cardIntrospection.findViewById<View?>(R.id.card_go_thanks)

        todayCard?.setOnClickListener { goIntrospection("TODAY") }
        thanksCard?.setOnClickListener { goIntrospection("THANKS") }
    }

    private fun goIntrospection(startTab: String) {
        val args = Bundle().apply { putString("startTab", startTab) }
        findNavController().navigate(R.id.action_homeFragment_to_introspectionFragment, args)
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

        val parent = binding.root.findViewById<androidx.appcompat.widget.LinearLayoutCompat>(R.id.linearLayoutMain)
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

        val sharedPreference =
            requireContext().getSharedPreferences("USER_TABLE", Context.MODE_PRIVATE)
        userToken = sharedPreference.getString("token", "null")

        recyclerview.adapter =
            ToDoAdapter(requireContext(), toDoViewModel, childFragmentManager, userToken)

        RetrofitInstance.networkService.getTodoItems(token = "Bearer $userToken")
            .enqueue(object : Callback<List<TodoItem>> {
                override fun onResponse(
                    call: Call<List<TodoItem>>,
                    response: Response<List<TodoItem>>
                ) {
                    if (response.isSuccessful) {
                        todoItems = response.body() as ArrayList<TodoItem>
                        RetrofitInstance.networkService.getPomodoroItems(token = "Bearer $userToken")
                            .enqueue(object : Callback<List<PomodoroItem>> {
                                override fun onResponse(
                                    call: Call<List<PomodoroItem>?>,
                                    response: Response<List<PomodoroItem>?>
                                ) {
                                    if (response.isSuccessful) {
                                        val response = response.body()
                                        val filterData1 = response?.groupBy { it.taskName }
                                        val filterData2 = filterData1?.mapValues { (_, list) ->
                                            val pomodoro25 =
                                                list.count { it.focusTime == 20 } // 25분
                                            val pomodoro50 =
                                                list.count { it.focusTime == 40 } // 50분
                                            Pair(pomodoro25, pomodoro50)
                                        }
                                        todoItems.forEach { todoItem ->
                                            val pair = filterData2?.get(todoItem.title)
                                            if (pair != null) {
                                                todoItem.timer25Number = pair.first
                                                todoItem.timer50Number = pair.second
                                            }
                                        }
                                        toDoViewModel.getTodoItems(todoItems)
                                    }
                                }

                                override fun onFailure(
                                    call: Call<List<PomodoroItem>?>,
                                    t: Throwable
                                ) {
                                    TODO("Not yet implemented")
                                }

                            })
                    } else {
                        Log.d("server success", "else")
                    }
                }

                override fun onFailure(call: Call<List<TodoItem>>, t: Throwable) {
                    Log.d("server error", "" + t.message)
                }
            })
    }

    private fun initListeners() {
        binding.btnAddTodoItem.setOnClickListener {
            val dialog = ToDoDialog(caller = TODO.ADD, userToken = userToken)
            dialog.show(childFragmentManager, ToDoDialog.Companion.TAG)
        }

        binding.tvHomeEdit.setOnClickListener {
            val intent = Intent(requireContext(), HomeEditActivity::class.java)
            startActivity(intent)
        }
    }

    private fun initObservers() {
        toDoViewModel.todoItems.observe(viewLifecycleOwner) { updateItems ->
            val newList = updateItems.map { it.copy() }
            (binding.recyclerview.adapter as ToDoAdapter).submitList(newList)
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
            CalendarMode.WEEK  -> btnWeek
            CalendarMode.DAY   -> btnDay
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
            CalendarMode.WEEK  -> "${date.monthValue}월 ${weekOfMonth(date)}째주"
            CalendarMode.DAY   -> "${date.monthValue}월 ${date.dayOfMonth}일"
        }
    }

    private fun weekOfMonth(date: LocalDate): Int = ((date.dayOfMonth - 1) / 7) + 1

    private fun loadAlarmPreviewForDate(date: LocalDate) {
        tvAlarmDate?.text = "${date.monthValue}월 ${date.dayOfMonth}일"
        showNoAlarmForDate(date)
    }

    private fun showNoAlarmForDate(date: LocalDate) {
        val isToday = date == LocalDate.now()
        tvAlarmTime?.text = if (isToday) "오늘 알람 없음" else "알람 없음"
    }
}