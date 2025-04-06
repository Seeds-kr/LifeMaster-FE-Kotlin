package com.example.lifemaster.presentation.home.pomodoro

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.lifemaster.R
import com.example.lifemaster.databinding.ActivityPomodoroBinding
import com.example.lifemaster.model.PomodoroItem
import com.example.lifemaster.network.RetrofitInstance
import com.example.lifemaster.presentation.MainActivity
import com.example.lifemaster.presentation.data.SharedData
import com.example.lifemaster.presentation.home.todo.TodoItem
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Timer
import kotlin.concurrent.timer

class PomodoroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPomodoroBinding
    private var userToken: String? = null

//    private val sharedViewModel: PomodoroViewModel by viewModels()

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
                val pomodoroItem = PomodoroItem(
                    taskName = todoItem!!.title,
                    focusTime = 20,
                    breakTime = 5,
                    cycles = 0,
                    date = todoItem!!.calendar.date,
                    currentTimer = 0
                )
                RetrofitInstance.networkService.registerPomodoroTimer(
                    token = "Bearer $userToken",
                    pomodoroItem = pomodoroItem
                ).enqueue(object : Callback<Any> {
                        override fun onResponse(
                            call: Call<in Any>,
                            response: Response<in Any>
                        ) {
                            if (response.isSuccessful) {
                                Toast.makeText(
                                    this@PomodoroActivity,
                                    "포모도로를 시작합니다!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(
                                    this@PomodoroActivity,
                                    response.message(),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        override fun onFailure(
                            call: Call<in Any>,
                            t: Throwable
                        ) {
                            Toast.makeText(this@PomodoroActivity, "서버 통신 실패!", Toast.LENGTH_SHORT)
                                .show()
                        }

                    })

                rgTimer.visibility = View.INVISIBLE
                ivTimer25.visibility = View.VISIBLE
                cardviewTodo.strokeColor =
                    ContextCompat.getColor(this@PomodoroActivity, R.color.blue_100)
                tvTimerTitle.text = "다음 휴식 시간까지"
                ObjectAnimator.ofFloat(
                    cardviewTodo,
                    "translationY",
                    cardviewTodo.translationY,
                    rgTimer.y - cardviewTodo.y
                ).apply {
                    duration = 500
                    start()
                }

//                sharedViewModel.clickButton()
                val firstStudyTime = 2 * 10
                val breakTime = 3 * 10
                val secondStudyTime = 2 * 10
                val totalTime =
                    firstStudyTime + breakTime + secondStudyTime  // 테스트용 3초 ( 25 * 60 * 10 )
                var remainTime = totalTime

                // worker thread
                timer = timer(
                    initialDelay = 0,
                    period = 100 // period 단위 = millisecond ( 100 millisecond = 0.1 second = 1 decisecond )
                ) {
                    if (remainTime > 0) {
                        if (remainTime == totalTime) {
                            time = firstStudyTime
                            val minutes = time.div(10) / 60
                            val seconds = time.div(10) % 60
                            val deciseconds = time % 10
                            runOnUiThread {
                                // worker thread 는 ui 에 접근할 수 없다. → Activity.runOnUiThread(Runnable) 이용
                                tvMinutesAndSeconds.text =
                                    String.format(
                                        "%02d:%02d",
                                        minutes,
                                        seconds
                                    )
                                tvMillisecond.text = deciseconds.toString()
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
                                tvMillisecond.text = deciseconds.toString()
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
                                tvMillisecond.text = deciseconds.toString()
                                circularTimerView.startTimer((secondStudyTime * 100).toLong())
                            }
                            remainTime -= 1
                            time -= 1
                        } else {
                            val minutes = time.div(10) / 60
                            val seconds = time.div(10) % 60
                            val deciseconds = time % 10
                            runOnUiThread {
                                // worker thread 는 ui 에 접근할 수 없다. → Activity.runOnUiThread(Runnable) 이용
                                tvMinutesAndSeconds.text =
                                    String.format(
                                        "%02d:%02d",
                                        minutes,
                                        seconds
                                    )
                                tvMillisecond.text = deciseconds.toString()
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

                        val intent = Intent(this@PomodoroActivity, MainActivity::class.java).apply {
                            putExtra("todoItemTitle", todoItem?.title)
                            flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                        }
                        startActivity(intent)
                    }
                }
            } else if (tvMinutesAndSeconds.text.toString() == getString(R.string.tv_pomodoro_timer_50)) {
//                sharedViewModel.clickButton()
                val pomodoroItem = PomodoroItem(
                    taskName = todoItem!!.title,
                    focusTime = 40,
                    breakTime = 10,
                    cycles = 0,
                    date = todoItem!!.calendar.date,
                    currentTimer = 0
                )
                RetrofitInstance.networkService.registerPomodoroTimer(
                    token = "Bearer $userToken",
                    pomodoroItem = pomodoroItem
                ).enqueue(object : Callback<Any> {
                    override fun onResponse(
                        call: Call<in Any>,
                        response: Response<in Any>
                    ) {
                        if (response.isSuccessful) {
                            Toast.makeText(
                                this@PomodoroActivity,
                                "서버 통신 성공!",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                this@PomodoroActivity,
                                response.message(),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(
                        call: Call<in Any>,
                        t: Throwable
                    ) {
                        Toast.makeText(this@PomodoroActivity, "서버 통신 실패!", Toast.LENGTH_SHORT)
                            .show()
                    }

                })

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
                    duration = 500
                    start()
                }

                val firstStudyTime = 2 * 10
                val breakTime = 3 * 10
                val secondStudyTime = 2 * 10
                val totalTime = firstStudyTime + breakTime + secondStudyTime
                var remainTime = totalTime

                timer = timer(initialDelay = 0, period = 100) { // worker thread
                    if (remainTime > 0) {
                        if (remainTime == totalTime) {
                            time = firstStudyTime
                            val minutes = time.div(10) / 60
                            val seconds = time.div(10) % 60
                            val deciseconds = time % 10
                            runOnUiThread {
                                // worker thread 는 ui 에 접근할 수 없다. → Activity.runOnUiThread(Runnable) 이용
                                tvMinutesAndSeconds.text =
                                    String.format(
                                        "%02d:%02d",
                                        minutes,
                                        seconds
                                    )
                                tvMillisecond.text = deciseconds.toString()
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
                                tvMillisecond.text = deciseconds.toString()
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
                                tvMillisecond.text = deciseconds.toString()
                                circularTimerView.startTimer((secondStudyTime * 100).toLong())
                            }
                            remainTime -= 1
                            time -= 1
                        } else {
                            val minutes = time.div(10) / 60
                            val seconds = time.div(10) % 60
                            val deciseconds = time % 10
                            runOnUiThread {
                                // worker thread 는 ui 에 접근할 수 없다. → Activity.runOnUiThread(Runnable) 이용
                                tvMinutesAndSeconds.text =
                                    String.format(
                                        "%02d:%02d",
                                        minutes,
                                        seconds
                                    )
                                tvMillisecond.text = deciseconds.toString()
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
                            // PomodoroActivity → MainActivity 의 Home Fragment 로 이동
                            // 해당 아이템의 할일 체크 상태 변경 알리기
//                            sharedViewModel.resetButtonCount()
                        }
                        timer?.cancel()
                        timer = null

                        val intent = Intent(this@PomodoroActivity, MainActivity::class.java).apply {
                            putExtra("todoItemTitle", todoItem?.title)
                            flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                        }

                        startActivity(intent)
                    }
                }
            } else if (tvMinutesAndSeconds.text == getString(R.string.tv_pomodoro_timer_not_set)) {
                Toast.makeText(this@PomodoroActivity, "시간을 설정해주세요!", Toast.LENGTH_SHORT).show()
            } else {
//                sharedViewModel.clickButton()
            }
        }
    }

    fun initObservers() {
//        sharedViewModel.selectedPosition.observe(viewLifecycleOwner) { selectedPosition ->
//            binding.tvSelectTodoItem.text = SharedData.todoItems[selectedPosition]
//        }
//        sharedViewModel.buttonCount.observe(viewLifecycleOwner) { btnCount ->
//            when (btnCount) {
//                0 -> {
//                    binding.btnStartPomodoro.text = "시작하기"
//                }
//                1 -> {
//                    binding.btnStartPomodoro.text = "비상 탈출"
//                }
//                else -> {
//                    val intent = Intent(requireContext(), EmergencyEscapeActivity::class.java)
//                    intent.putExtra("remain_timer", binding.tvPomodoroTimer.text.toString())
//                    startActivity(intent)
//                }
//            }
//        }
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
                period = 1000
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
