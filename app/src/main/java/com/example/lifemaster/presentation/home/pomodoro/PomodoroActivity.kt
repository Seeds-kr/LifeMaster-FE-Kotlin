package com.example.lifemaster.presentation.home.pomodoro

import android.animation.ObjectAnimator
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.lifemaster.R
import com.example.lifemaster.databinding.ActivityPomodoroBinding
import com.example.lifemaster.presentation.data.SharedData
import java.util.*
import kotlin.concurrent.timer

class PomodoroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPomodoroBinding

//    private val sharedViewModel: PomodoroViewModel by viewModels()

    private var totalDeciSecond = 0

    private var timer: Timer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPomodoroBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViews()
        initListeners()
        initObservers()
    }

    private fun initViews() = with(binding) {
        val title = intent.getStringExtra("title")
        tvTodoItemTitle.text = title
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
                ObjectAnimator.ofFloat(cardviewTodo, "translationY", cardviewTodo.translationY, rgTimer.y-cardviewTodo.y).apply {
                    duration = 500
                    start()
                }
//                sharedViewModel.clickButton()
                totalDeciSecond = 25 * 60 * 10  // 25분 × 60초
                // worker thread
                timer = timer(
                    initialDelay = 0,
                    period = 100 // period 단위 = millisecond ( 100 millisecond = 0.1 second = 1 decisecond )
                ) {
                    if (totalDeciSecond > 0) {
                        totalDeciSecond -= 1
                        val minutes = totalDeciSecond.div(10) / 60
                        val seconds = totalDeciSecond.div(10) % 60
                        val deciseconds = totalDeciSecond % 10
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
                    } else {
                        runOnUiThread {
                            Toast.makeText(
                                this@PomodoroActivity,
                                "시간이 종료되었습니다!",
                                Toast.LENGTH_SHORT
                            ).show()
                            // PomodoroActivity → MainActivity 의 Home Fragment 로 이동
                            // 해당 아이템의 할일 체크 상태 변경 알리기
                            // sharedViewModel.resetButtonCount()
                        }
                        timer?.cancel()
                        timer = null
                    }
                }
            }
            else if (tvMinutesAndSeconds.text.toString() == getString(R.string.tv_pomodoro_timer_50)) {
//                sharedViewModel.clickButton()
                rgTimer.visibility = View.INVISIBLE
                totalDeciSecond = 50 * 60 * 10
                timer = timer(initialDelay = 0, period = 100) { // worker thread
                    if (totalDeciSecond > 0) {
                        totalDeciSecond -= 1
                        val minutes = totalDeciSecond.div(10) / 60
                        val seconds = totalDeciSecond.div(10) % 60
                        val deciseconds = totalDeciSecond % 10
                        runOnUiThread {
                            tvMinutesAndSeconds.text =
                                String.format(
                                    "%02d:%02d",
                                    minutes,
                                    seconds
                                )
                            tvMillisecond.text = deciseconds.toString()
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
                    }
                }
            }
            else if (tvMinutesAndSeconds.text == getString(R.string.tv_pomodoro_timer_not_set)) {
                Toast.makeText(this@PomodoroActivity, "시간을 설정해주세요!", Toast.LENGTH_SHORT).show()
            }
            else {
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
