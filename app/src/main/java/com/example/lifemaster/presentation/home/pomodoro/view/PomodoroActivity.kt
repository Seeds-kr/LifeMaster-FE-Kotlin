package com.example.lifemaster.presentation.home.pomodoro.view

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.lifemaster.R
import com.example.lifemaster.databinding.ActivityPomodoroBinding
import com.example.lifemaster.presentation.home.pomodoro.model.PomodoroItem
import com.example.lifemaster.presentation.MainActivity
import com.example.lifemaster.presentation.home.pomodoro.model.SharedData
import com.example.lifemaster.presentation.home.pomodoro.model.PomodoroStatus
import com.example.lifemaster.presentation.home.pomodoro.viewmodel.PomodoroViewModel
import com.example.lifemaster.presentation.home.todo.model.TodoItem
import java.util.Timer
import kotlin.concurrent.timer

class PomodoroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPomodoroBinding
    private var userToken: String? = null

    private val pomodoroViewModel: PomodoroViewModel by viewModels()

    private var time = 0
    private var timer: Timer? = null
    private var todoItem: TodoItem? = null
    var totalDeciSecond = 0

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPomodoroBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViews()
        initListeners()
        initObservers()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun initViews() = with(binding) {
        todoItem = intent.getParcelableExtra("item", TodoItem::class.java)
        todoItem?.let { tvTodoItemTitle.text = it.title }
        val sharedPreference = getSharedPreferences("USER_TABLE", MODE_PRIVATE)
        userToken = sharedPreference.getString("token", "null")
    }

    private fun initListeners() = with(binding) {
        rb25Minutes.setOnClickListener {
            tvMinutesAndSeconds.text = getString(R.string.tv_pomodoro_timer_25)
        }
        rb50Minutes.setOnClickListener {
            tvMinutesAndSeconds.text = getString(R.string.tv_pomodoro_timer_50)
        }
        btnStartPomodoro.setOnClickListener {
            if (tvMinutesAndSeconds.text.toString() == getString(R.string.tv_pomodoro_timer_25)) {
                rgTimer.visibility = View.INVISIBLE
                ivTimer25.visibility = View.VISIBLE
                cardviewTodo.strokeColor = ContextCompat.getColor(this@PomodoroActivity, R.color.blue_100)
                tvTimerTitle.text = "다음 휴식 시간까지"
                ObjectAnimator.ofFloat(
                    cardviewTodo,
                    "translationY",
                    cardviewTodo.translationY,
                    rgTimer.y - cardviewTodo.y
                ).apply {
//                    ObjectAnimator.setDuration = 500
                    start()
                }

                pomodoroViewModel.clickButton() // _buttonCount = 0 -> 1

                val firstStudyTime = 10 * 60 * 10
//                val firstStudyTime = 2 * 10 test
                val breakTime = 5 * 60 * 10
//                val breakTime = 3 * 10 test
                val secondStudyTime = 10 * 60 * 10
//                val secondStudyTime = 2 * 10 test
                val totalTime = firstStudyTime + breakTime + secondStudyTime  // 테스트용 3초 ( 25 * 60 * 10 )

                // worker thread
                timer = timer(
                    initialDelay = 0,
                    period = 100 // period 단위 = millisecond ( 100 millisecond = 0.1 second = 1 decisecond )
                ) {
                    pomodoroTimer(totalTime, firstStudyTime, breakTime, secondStudyTime)
                }
            } else if (tvMinutesAndSeconds.text.toString() == getString(R.string.tv_pomodoro_timer_50)) {
                rgTimer.visibility = View.INVISIBLE
                ivTimer50.visibility = View.VISIBLE
                cardviewTodo.setStrokeColor(
                    ContextCompat.getColor(
                        this@PomodoroActivity,
                        R.color.blue_100
                    )
                )
                tvTimerTitle.text = "다음 휴식 시간까지"
                ObjectAnimator.ofFloat(
                    cardviewTodo,
                    "translationY",
                    cardviewTodo.translationY,
                    rgTimer.y - cardviewTodo.y
                ).apply {
//                    ObjectAnimator.setDuration = 500
                    start()
                }

                pomodoroViewModel.clickButton() // _buttonCount = 0 -> 1

                val firstStudyTime = 20 * 60 * 10
//                val firstStudyTime = 1 * 10
                val breakTime = 10 * 60 * 10
//                val breakTime = 1 * 10
                val secondStudyTime = 20 * 60 * 10
//                val secondStudyTime = 1 * 10
                val totalTime = firstStudyTime + breakTime + secondStudyTime

                timer = timer(initialDelay = 0, period = 100) { // worker thread
                    pomodoroTimer(totalTime, firstStudyTime, breakTime, secondStudyTime)
                }

            } else if (tvMinutesAndSeconds.text == getString(R.string.tv_pomodoro_timer_not_set)) {
                Toast.makeText(this@PomodoroActivity, "시간을 설정해주세요!", Toast.LENGTH_SHORT).show()
            } else {
                pomodoroViewModel.clickButton()
            }
        }
        onBackPressedDispatcher.addCallback(
            this@PomodoroActivity,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    showExitDialog()
                }
            })
    }

    private fun pomodoroTimer(
        totalTime: Int,
        firstStudyTime: Int,
        breakTime: Int,
        secondStudyTime: Int
    ) = with(binding) {
        var remainTime = totalTime
        if (remainTime > 0) {
            if (remainTime == totalTime) {
                time = firstStudyTime
                val minutes = time.div(10) / 60
                val seconds = time.div(10) % 60
                val deciseconds = time % 10
                runOnUiThread {
                    tvMinutesAndSeconds.text =
                        String.format(
                            "%02d:%02d",
                            minutes,
                            seconds
                        )
                    tvDecisecond.text = deciseconds.toString()
                    circularTimerView.startTimer((firstStudyTime * 100).toLong())
                }
                remainTime -= 1
                time -= 1
            } else if (remainTime == totalTime - firstStudyTime) {
                // 휴식 시간 시작
                time = breakTime
                val minutes = time.div(10) / 60
                val seconds = time.div(10) % 60
                val deciseconds = time % 10
                runOnUiThread {
                    tvTimerTitle.text = "휴식 시간 종료까지"
                    tvMinutesAndSeconds.text =
                        String.format(
                            "%02d:%02d",
                            minutes,
                            seconds
                        )
                    tvDecisecond.text = deciseconds.toString()
                    circularTimerView.startTimer((breakTime * 100).toLong()) // decisecond → millisecond
                }
                remainTime -= 1
                time -= 1
            } else if (remainTime == secondStudyTime) {
                // 두번째 공부 시간 시작
                time = secondStudyTime
                val minutes = time.div(10) / 60
                val seconds = time.div(10) % 60
                val deciseconds = time % 10
                runOnUiThread {
                    tvTimerTitle.text = "할일 종료까지"
                    tvMinutesAndSeconds.text =
                        String.format(
                            "%02d:%02d",
                            minutes,
                            seconds
                        )
                    tvDecisecond.text = deciseconds.toString()
                    circularTimerView.startTimer((secondStudyTime * 100).toLong())
                }
                remainTime -= 1
                time -= 1
            } else {
                val minutes = time.div(10) / 60
                val seconds = time.div(10) % 60
                val deciseconds = time % 10
                runOnUiThread {
                    tvMinutesAndSeconds.text =
                        String.format(
                            "%02d:%02d",
                            minutes,
                            seconds
                        )
                    tvDecisecond.text = deciseconds.toString()
                }
                remainTime -= 1
                time -= 1
            }
        } else {
            runOnUiThread {
                Toast.makeText(
                    this@PomodoroActivity,
                    "시간이 종료되었습니다!",
                    Toast.LENGTH_SHORT
                ).show()
                // sharedViewModel.resetButtonCount()
            }

            timer?.cancel()
            timer = null

            if (totalTime == 25 * 60 * 10) {
                // 25분
                val pomodoroItem = PomodoroItem(
                    taskName = todoItem!!.title,
                    focusTime = 20,
                    breakTime = 5,
                    cycles = 0,
                    date = todoItem!!.calendar.date,
                    currentTimer = 0
                )
            } else if (totalTime == 50 * 60 * 10) {
                val pomodoroItem = PomodoroItem(
                    taskName = todoItem!!.title,
                    focusTime = 40,
                    breakTime = 10,
                    cycles = 0,
                    date = todoItem!!.calendar.date,
                    currentTimer = 0
                )
            }

//            RetrofitInstance.networkService.registerPomodoroTimer(
//                token = "Bearer $userToken",
//                pomodoroItem = pomodoroItem
//            ).enqueue(object : Callback<Any> {
//                override fun onResponse(
//                    call: Call<in Any>,
//                    response: Response<in Any>
//                ) {
//                    if (response.isSuccessful) {
//                        Toast.makeText(
//                            this@PomodoroActivity,
//                            "포모도로 타이머 등록 성공!",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    } else {
//                        Toast.makeText(
//                            this@PomodoroActivity,
//                            "포모도로 타이머 등록 실패!",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    }
//                }
//
//                override fun onFailure(
//                    call: Call<in Any>,
//                    t: Throwable
//                ) {
//                    Toast.makeText(this@PomodoroActivity, "서버 통신 실패!", Toast.LENGTH_SHORT)
//                        .show()
//                }
//
//            })

            val intent = Intent(this@PomodoroActivity, MainActivity::class.java).apply {
                putExtra("todoItemTitle", todoItem?.title)
//                Intent.setFlags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            }

            startActivity(intent)
        }
    }

    private fun showExitDialog() {
        AlertDialog.Builder(this@PomodoroActivity).apply {
            setTitle("포모도로 타이머 종료하시겠습니까?")
            setMessage("뒤로가면 타이머 저장이 되지 않고, 해당 시간 기록도 삭제됩니다. 😢")
            setPositiveButton("예") { _, _ ->
                finish()
            }
            setNegativeButton("아니요") { dialog, _ -> dialog.dismiss() }
            setCancelable(false)
            create().show()
        }
    }

    private fun initObservers() {
//        pomodoroViewModel.selectedPosition.observe(viewLifecycleOwner) { selectedPosition ->
//            binding.tvSelectTodoItem.text = SharedData.todoItems[selectedPosition]
//        }
        pomodoroViewModel.buttonCount.observe(this@PomodoroActivity) { btnCount ->
            when (btnCount) {
                0 -> {
                    binding.btnStartPomodoro.text = "시작하기" // 이 코드가 좀 아쉬운데
                }

                1 -> {
                    binding.btnStartPomodoro.text = "비상 탈출"
                }

                else -> {
                    Toast.makeText(this@PomodoroActivity, "비상탈출을 시도합니다!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, EmergencyEscapeActivity::class.java)
                    intent.putExtra(
                        "remainMinutesAndSeconds",
                        binding.tvMinutesAndSeconds.text.toString()
                    )
                    intent.putExtra("remainDeciSeconds", binding.tvDecisecond.text.toString())
                    startActivity(intent)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (SharedData.pomodoroStatus == PomodoroStatus.ESCAPE_SUCCESS) {
            // 해당 할 일을 아직 다 끝내지 못한 상태
//            sharedViewModel.resetButtonCount()
            timer?.cancel()
            timer = null
            totalDeciSecond = 0
//            binding.tvPomodoroTimer.text =
//                getString(R.string.tv_pomodoro_timer_not_set) // [?] zero? not set?
            binding.rb25Minutes.isChecked = false
            binding.rb50Minutes.isChecked = false
            binding.rb25Minutes.isClickable = true
            binding.rb50Minutes.isClickable = true
            Toast.makeText(this, "비상탈출을 완료했습니다!", Toast.LENGTH_SHORT).show()
        } else if (SharedData.pomodoroStatus == PomodoroStatus.ESCAPE_FAILURE) {
            timer = timer(
                initialDelay = 1000,
                period = 100
            ) { // period 단위 = millisecond ( 1000 millisecond = 1 second )
                if (totalDeciSecond > 0) {
                    totalDeciSecond -= 1
                    val currentHour = totalDeciSecond / 3600
                    val currentMinute = totalDeciSecond / 60
                    val currentSecond = totalDeciSecond % 60
                    runOnUiThread {
                        // worker thread 는 ui 에 접근할 수 없다. → Activity.runOnUiThread(Runnable) 이용
//                        binding.tvPomodoroTimer.text =
//                            String.format(
//                                "%02d:%02d:%02d",
//                                currentHour,
//                                currentMinute,
//                                currentSecond
//                            )
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@PomodoroActivity, "시간이 종료되었습니다!", Toast.LENGTH_SHORT)
                            .show()
                        binding.rb25Minutes.isChecked = false
                        binding.rb25Minutes.isClickable = true
                        binding.rb50Minutes.isClickable = true
//                        sharedViewModel.resetButtonCount()
                    } // [?] 왜 25분을 다시 클릭했을 때 ui 색상이 안변하지?
                    timer?.cancel()
                    timer = null
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        Log.d("Fragment_Pomodoro", "onStop")
        timer?.cancel()
        timer = null
    }

}
