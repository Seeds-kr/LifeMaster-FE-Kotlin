package com.example.lifemaster.presentation.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.lifemaster.R
import com.example.lifemaster.databinding.FragmentHomeBinding
import com.example.lifemaster.network.RetrofitInstance
import com.example.lifemaster.presentation.home.alarm.model.DataResource
import com.example.lifemaster.presentation.home.calendar.view.CalendarFragment
import com.example.lifemaster.presentation.home.calendar.viewmodel.CalendarMode
import com.example.lifemaster.presentation.home.calendar.viewmodel.CalendarViewModel
import com.example.lifemaster.presentation.home.edit.view.HomeEditActivity
import com.example.lifemaster.presentation.home.sleep.viewmodel.SleepViewModel
import com.example.lifemaster.presentation.home.sleep.viewmodel.SleepViewModelFactory
import com.example.lifemaster.presentation.home.todo.adapter.ToDoAdapter
import com.example.lifemaster.presentation.home.todo.model.TODO
import com.example.lifemaster.presentation.home.todo.model.TodoModel
import com.example.lifemaster.presentation.home.todo.view.ToDoDialog
import com.example.lifemaster.presentation.home.todo.viewmodel.ToDoViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate

class HomeFragment : Fragment() {

    lateinit var binding: FragmentHomeBinding
    private val toDoViewModel: ToDoViewModel by activityViewModels()
    private val sleepViewModel: SleepViewModel by activityViewModels {
        SleepViewModelFactory(RetrofitInstance.networkService)
    }
    private val calendarVM: CalendarViewModel by activityViewModels()
    private lateinit var remoteTodoItems: List<TodoModel>

    private val todoAddDialog = ToDoDialog(origin = TODO.ADD)
    private lateinit var todoEditDialog: ToDoDialog

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

        binding.containerCalendar.post {
            if (childFragmentManager.findFragmentById(R.id.container_calendar) == null) {
                childFragmentManager.beginTransaction()
                    .replace(R.id.container_calendar, CalendarFragment())
                    .commit()
            }
        }

        val (visible, ordered) = loadHomeConfiguration()
        applyHomeLayout(visible, ordered)

        setupCalendarHeader()
        observeCalendarState()
        fetchRemoteData()
        initViews()
        initListeners()
        initObservers()

        // 알람 화면 이동
        binding.cvGoToAlarm.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_alarmListFragment)
        }
        // 디톡스 화면 이동
        binding.cardDetox.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_detoxFragment)
        }
        // 그룹 화면 이동
        binding.cardGroup.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_groupFragment)
        }

    }

    override fun onResume() {
        super.onResume()
        val (visible, ordered) = loadHomeConfiguration()
        applyHomeLayout(visible, ordered)
        rebindCalendarHeaderUI()
    }

    private fun fetchRemoteData() {
//        toDoViewModel.getRemoteTodoItems(token = getString(R.string.user_token))
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

        toDoViewModel.getTodoItems()

        todoRecyclerview.adapter = ToDoAdapter(context = requireContext(), onToggleClicked = { id ->
            toDoViewModel.toggleItem(id = id)
        }, onEditClicked = { item ->
            todoEditDialog = ToDoDialog(origin = TODO.EDIT, item = item)
            todoEditDialog.show(childFragmentManager, ToDoDialog.TAG)
        }, onDeleteClicked = { alarmId ->
            toDoViewModel.deleteTodoItem(deleteId = alarmId)
        })

//        RetrofitInstance.networkService.getTodoItems(token = "Bearer $userToken")
//            .enqueue(object : Callback<List<TodoModel>> {
//                override fun onResponse(
//                    call: Call<List<TodoModel>>,
//                    response: Response<List<TodoModel>>
//                ) {
//                    if (response.isSuccessful) {
//                        todoModels = response.body() as ArrayList<TodoModel>
//                        RetrofitInstance.networkService.getPomodoroItems(token = "Bearer $userToken")
//                            .enqueue(object : Callback<List<PomodoroItem>> {
//                                override fun onResponse(
//                                    call: Call<List<PomodoroItem>?>,
//                                    response: Response<List<PomodoroItem>?>
//                                ) {
//                                    if (response.isSuccessful) {
//                                        val response = response.body()
//                                        val filterData1 = response?.groupBy { it.taskName }
//                                        val filterData2 = filterData1?.mapValues { (_, list) ->
//                                            val pomodoro25 =
//                                                list.count { it.focusTime == 20 } // 25분
//                                            val pomodoro50 =
//                                                list.count { it.focusTime == 40 } // 50분
//                                            Pair(pomodoro25, pomodoro50)
//                                        }
//                                        todoModels.forEach { todoItem ->
//                                            val pair = filterData2?.get(todoItem.title)
//                                            if (pair != null) {
//                                                todoItem.timer25Number = pair.first
//                                                todoItem.timer50Number = pair.second
//                                            }
//                                        }
//                                        toDoViewModel.getTodoItems(todoModels)
//                                    }
//                                }
//
//                                override fun onFailure(
//                                    call: Call<List<PomodoroItem>?>,
//                                    t: Throwable
//                                ) {
//                                    TODO("Not yet implemented")
//                                }
//
//                            })
//                    } else {
//                        Log.d("server success", "else")
//                    }
//                }
//
//                override fun onFailure(call: Call<List<TodoModel>>, t: Throwable) {
//                    Log.d("server error", "" + t.message)
//                }
//            })

        // 수면
        itemSleepPreview.tvAlarmDate.text = "${LocalDate.now().monthValue}월 ${LocalDate.now().dayOfMonth}일"
        itemSleepPreview.tvAlarmTime.text = if(sleepViewModel.isMeasured) "${sleepViewModel.sleepDurationHour}시간 ${sleepViewModel.sleepDurationMinutes}분 수면" else "금일 수면 미측정"
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
            findNavController().navigate(R.id.action_homeFragment_to_sleepReportFragment)
        }

    }

    private fun initObservers() = with(binding) {

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    toDoViewModel.newItem.collect { resource ->
                        when(resource) {
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
                    toDoViewModel.currentItems.collect { resource ->
                        when(resource) {
                            is DataResource.Error -> {
                                Toast.makeText(context, "할일 목록을 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
                            }
                            DataResource.Idle -> {}
                            DataResource.Loading -> {}
                            is DataResource.Success<List<TodoModel>> -> {
                                val todoItems: List<TodoModel> = resource.data
                                (binding.todoRecyclerview.adapter as ToDoAdapter).submitList(todoItems)
                            }
                        }
                    }
                }
                launch {
                    toDoViewModel.deletionState.collect { resource ->
                        when(resource) {
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
                        when(resource) {
                            is DataResource.Error -> {
                                Toast.makeText(context, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                            }
                            DataResource.Idle -> {}
                            DataResource.Loading -> {}
                            is DataResource.Success<TodoModel> -> {
                                val updateItem = resource.data
                                val oldList = (todoRecyclerview.adapter as ToDoAdapter).currentList
                                val updatedList = oldList.map { if(it.id == updateItem.id) updateItem else it }
                                (todoRecyclerview.adapter as ToDoAdapter).submitList(updatedList)
                                todoEditDialog.dismiss()
                                Toast.makeText(context, "할일이 수정되었습니다.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
                launch {
                    toDoViewModel.toggleItem.collect { resource ->
                        when(resource) {
                            is DataResource.Error -> {
                                Toast.makeText(context, resources.getString(R.string.server_error_message), Toast.LENGTH_SHORT).show()
                            }
                            DataResource.Idle -> {}
                            DataResource.Loading -> {}
                            is DataResource.Success<TodoModel> -> {
                                val data = resource.data
                                val oldList = (todoRecyclerview.adapter as ToDoAdapter).currentList
                                val newList = oldList.map { if(it.id == data.id) data else it }
                                (todoRecyclerview.adapter as ToDoAdapter).submitList(newList)
                                if(data.isCompleted) {
                                    Toast.makeText(context, "할일이 체크되었습니다.", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "할일이 해제되었습니다.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                }
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

    private fun weekOfMonth(date: LocalDate): Int {
        return ((date.dayOfMonth - 1) / 7) + 1
    }

    companion object {
        const val TAG_TODO = "TODO"
    }
}