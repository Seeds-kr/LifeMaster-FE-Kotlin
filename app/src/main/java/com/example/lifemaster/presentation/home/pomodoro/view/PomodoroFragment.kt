package com.example.lifemaster.presentation.home.pomodoro.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import com.example.lifemaster.R
import com.example.lifemaster.databinding.FragmentPomodoroBinding
import com.example.lifemaster.presentation.home.alarm.model.DataResource
import com.example.lifemaster.presentation.home.pomodoro.model.PomodoroButtonStatus
import com.example.lifemaster.presentation.home.pomodoro.model.PomodoroModel
import com.example.lifemaster.presentation.home.pomodoro.model.PomodoroRequest
import com.example.lifemaster.presentation.home.pomodoro.model.PomodoroTimeType
import com.example.lifemaster.presentation.home.pomodoro.viewmodel.PomodoroViewModel
import com.example.lifemaster.presentation.home.todo.model.TodoModel
import com.example.lifemaster.presentation.home.todo.view.SelectTodoDialog
import com.example.lifemaster.presentation.home.todo.viewmodel.ToDoViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PomodoroFragment : Fragment(R.layout.fragment_pomodoro) {
    private lateinit var binding: FragmentPomodoroBinding
    private val args: PomodoroFragmentArgs by navArgs()
    private val pomodoroViewModel: PomodoroViewModel by activityViewModels()
    private val todoViewModel: ToDoViewModel by activityViewModels()
    private lateinit var todoItems: List<TodoModel>
    private lateinit var currentTodoItem: TodoModel
    private var pomodoroTimeType: PomodoroTimeType = PomodoroTimeType.TIMER_25

    // 타이머 관련 변수
    private var timerJob: Job? = null
    private var timeLeftInSeconds = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentPomodoroBinding.bind(view)
        fetchData()
        initViews()
        initListeners()
        initObservers()
    }

    private fun fetchData() {
        pomodoroViewModel.getPomodoroItemsByTodo(todoId = args.todoItem.id)
    }

    private fun initViews() = with(binding) {
        currentTodoItem = args.todoItem
        tvTodoItemTitle.text = currentTodoItem.title
        rb25Minutes.isChecked = true
        updateTimerText(TIMER_25)
    }

    private fun initListeners() = with(binding) {
        rb25Minutes.setOnClickListener {
            updateTimerText(TIMER_25)
            pomodoroTimeType = PomodoroTimeType.TIMER_25
        }
        rb50Minutes.setOnClickListener {
            updateTimerText(TIMER_50)
            pomodoroTimeType = PomodoroTimeType.TIMER_50
        }
        cardviewTodo.setOnClickListener {
            val dialog = SelectTodoDialog(todoItems = todoItems, currentItem = currentTodoItem) { item ->
                tvTodoItemTitle.text = item.title
                currentTodoItem = item
                pomodoroViewModel.getPomodoroItemsByTodo(todoId = item.id)
            }
            dialog.show(childFragmentManager, SelectTodoDialog.TAG)
        }
        btnStartPomodoro.setOnClickListener {
            when(pomodoroViewModel.currentStatus) {
                PomodoroButtonStatus.TODO -> {
                    if(pomodoroTimeType == PomodoroTimeType.TIMER_25) {
                        tvTimerTitle.text = "다음 휴식 시간까지"
                        btnStartPomodoro.text = "비상 탈출"
                        startTimer(TIMER_25)
                    } else if(pomodoroTimeType == PomodoroTimeType.TIMER_50) {
                        tvTimerTitle.text = "다음 휴식 시간까지"
                        btnStartPomodoro.text = "비상 탈출"
                        startTimer(TIMER_50)
                    } else {
                        Toast.makeText(context, "시간을 설정해 주세요.", Toast.LENGTH_SHORT).show()
                    }
                }
                PomodoroButtonStatus.REST -> {
                    if(pomodoroTimeType == PomodoroTimeType.TIMER_25) {
                        // 25분 끝난 후 휴식
                        startTimer(TIMER_25_REST)
                    } else {
                        // 50분 끝난 후 휴식
                        startTimer(TIMER_50_REST)
                    }
                }
            }
        }
    }

    private fun startTimer(totalSeconds: Int) {
        timeLeftInSeconds = totalSeconds
        timerJob = viewLifecycleOwner.lifecycleScope.launch {
            while(timeLeftInSeconds>0) {
                delay(1000L) // 1초
                timeLeftInSeconds--
                updateTimerText(timeLeftInSeconds)
            }
            when(pomodoroViewModel.currentStatus) {
                PomodoroButtonStatus.TODO -> {
                    onTimerTodoFinished(totalSeconds)
                }
                PomodoroButtonStatus.REST -> {
                    onTimerRestFinished(totalSeconds)
                }
            }
        }
    }

    // 남은 초 단위를 MM:SS 형식으로 변환 및 관련 UI 업데이트
    private fun updateTimerText(seconds: Int) = with(binding) {
        val minutes = seconds/60
        val remainingSeconds = seconds%60
        tvMinutesAndSeconds.text = String.format("%02d:%02d", minutes, remainingSeconds)
    }

    private fun onTimerTodoFinished(focusTotalSeconds: Int) = with(binding) {
        pomodoroViewModel.currentStatus = PomodoroButtonStatus.REST
        tvTimerTitle.text = "휴식을 취하세요"
        btnStartPomodoro.text = "휴식하기"
        val restTotalSeconds = if(focusTotalSeconds == TIMER_25) TIMER_25_REST else TIMER_50_REST
        updateTimerText(restTotalSeconds)
    }

    private fun onTimerRestFinished(focusRestSeconds: Int) {
        when(focusRestSeconds) {
            TIMER_25_REST -> {
                val pomodoroModel = PomodoroRequest(
                    todoId = currentTodoItem.id,
                    taskName = currentTodoItem.title,
                    focusTime = 25,
                    breakTime = 5,
                    currentTimer = 1, // 로직 바꾸기
                    date = currentTodoItem.date
                )
                pomodoroViewModel.registerPomodoroItem(pomodoroRequest = pomodoroModel)
            }
            TIMER_50_REST -> {
                val pomodoroModel = PomodoroRequest(
                    todoId = currentTodoItem.id,
                    taskName = currentTodoItem.title,
                    focusTime = 50,
                    breakTime = 10,
                    currentTimer = 1, // 로직 바꾸기
                    date = currentTodoItem.date
                )
                pomodoroViewModel.registerPomodoroItem(pomodoroRequest = pomodoroModel)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timerJob?.cancel()
    }

    private fun initObservers() = with(binding) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    todoViewModel.currentItems.collect { dataResource ->
                        when(dataResource) {
                            is DataResource.Success -> {
                                val data = dataResource.data
                                todoItems = data
                            }
                            else -> {}
                        }
                    }
                }
                launch {
                    pomodoroViewModel.newPomodoroItem.collect { dataResource ->
                        when (dataResource) {
                            is DataResource.Success -> {
                                val data = dataResource.data
                                Toast.makeText(context, "한 개의 포모도로를 생성했습니다.", Toast.LENGTH_SHORT).show()
                                pomodoroViewModel.currentStatus = PomodoroButtonStatus.TODO
                                rb25Minutes.isChecked = true
                                tvTimerTitle.text = "해야할 일을 시작하세요"
                                updateTimerText(TIMER_25)
                                pomodoroTimeType = PomodoroTimeType.TIMER_25
                                btnStartPomodoro.text = "시작하기"
                                pomodoroViewModel.getPomodoroItemsByTodo(todoId = data.todo.id)
                            }
                            is DataResource.Error -> {
                                Toast.makeText(context, getString(R.string.server_error_message), Toast.LENGTH_SHORT).show()
                            }
                            else -> {}
                        }
                    }
                }
                launch {
                    pomodoroViewModel.pomodoroItemsByTodo.collect { resource ->
                        when(resource) {
                            is DataResource.Error -> {}
                            DataResource.Idle -> {}
                            DataResource.Loading -> {}
                            is DataResource.Success<List<PomodoroModel>> -> {
                                val pomodoroItems = resource.data
                                val timer25Num = pomodoroItems.filter { it.focusTime == 25 }.size
                                val timer50Num = pomodoroItems.filter { it.focusTime == 50 }.size
                                ivPomodoroTodoTimer25.isVisible = timer25Num != 0
                                tvItemPomodoroTimer25Count.isVisible = timer25Num != 0
                                tvItemPomodoroTimer25Count.text = timer25Num.toString()
                                ivPomodoroTodoTimer50.isVisible = timer50Num != 0
                                tvItemPomodoroTimer50Count.isVisible = timer50Num != 0
                                tvItemPomodoroTimer50Count.text = timer50Num.toString()
                            }
                        }
                    }
                }
            }
        }
    }

    private companion object {
        const val TIMER_25 = 3
        const val TIMER_25_REST = 2
        const val TIMER_50 = 4
        const val TIMER_50_REST = 3
    }
}