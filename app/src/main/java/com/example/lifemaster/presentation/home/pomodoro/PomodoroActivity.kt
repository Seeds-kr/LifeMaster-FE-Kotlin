package com.example.lifemaster.presentation.home.pomodoro

import android.os.Bundle
import android.util.Log
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

    private var totalSecond = 0

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
//        binding.ivOpenTodoDialog.setOnClickListener {
//            val dialog = PomodoroDialog()
//            dialog.isCancelable = false
//            dialog.show(supportFragmentManager, PomodoroDialog.TAG)
//        }
        rb25Minutes.setOnClickListener {
            tvPomodoroTimer.text = getString(R.string.tv_pomodoro_timer_25)
        }
        rb50Minutes.setOnClickListener {
            tvPomodoroTimer.text = getString(R.string.tv_pomodoro_timer_50)
        }
        btnStartPomodoro.setOnClickListener {
            if(tvPomodoroTimer.text.toString() == getString(R.string.tv_pomodoro_timer_25) && tvTodoItemTitle.text.toString()!=getString(R.string.item_is_not_selected)) {
                rb25Minutes.isClickable = false
                rb50Minutes.isClickable = false // [refactor] binding 도 많이 중복되니 scope function 으로 빼서 중복 피하기
//                sharedViewModel.clickButton()
                totalSecond = 25*60  // 25분 × 60초
//                totalSecond = 5 // test 용
                // worker thread
                timer = timer(
                    initialDelay = 0,
                    period = 1000
                ) { // period 단위 = millisecond ( 1000 millisecond = 1 second )
                    if (totalSecond > 0) {
                        totalSecond -= 1
                        val currentHour = totalSecond / 3600
                        val currentMinute = totalSecond / 60
                        val currentSecond = totalSecond % 60
                        runOnUiThread {
                            // worker thread 는 ui 에 접근할 수 없다. → Activity.runOnUiThread(Runnable) 이용
                            tvPomodoroTimer.text =
                                String.format(
                                    "%02d:%02d:%02d",
                                    currentHour,
                                    currentMinute,
                                    currentSecond
                                )
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@PomodoroActivity, "시간이 종료되었습니다!", Toast.LENGTH_SHORT).show()
                            rb25Minutes.isChecked = false
                            rb25Minutes.isClickable = true
                            rb50Minutes.isClickable = true
//                            sharedViewModel.resetButtonCount()
                        } // [fix] 왜 25분을 다시 클릭했을 때 ui 색상이 안변하지?
                        timer?.cancel()
                        timer = null
                        // totalSecond = 0
                    }
                }
            } else if(tvPomodoroTimer.text.toString() == getString(R.string.tv_pomodoro_timer_50) && binding.tvTodoItemTitle.text.toString()!=getString(R.string.item_is_not_selected)) {
                rb25Minutes.isClickable = false
                rb50Minutes.isClickable = false
//                sharedViewModel.clickButton()
                totalSecond = 50 * 60
                timer = timer(initialDelay = 0, period = 1000) { // worker thread
                    if (totalSecond > 0) { // [refactor] 반복되는 부분 함수로 빼기
                        totalSecond -= 1
                        val currentHour = totalSecond / 3600
                        val currentMinute = totalSecond / 60
                        val currentSecond = totalSecond % 60
                        runOnUiThread {
                            tvPomodoroTimer.text =
                                String.format(
                                    "%02d:%02d:%02d",
                                    currentHour,
                                    currentMinute,
                                    currentSecond
                                )
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@PomodoroActivity, "시간이 종료되었습니다!", Toast.LENGTH_SHORT)
                                .show()
                            rb50Minutes.isChecked = false
                            rb25Minutes.isClickable = true
                            rb50Minutes.isClickable = true
//                            sharedViewModel.resetButtonCount()
                        }
                        timer?.cancel()
                        timer = null
                    }
                }
            } else if(tvTodoItemTitle.text == getString(R.string.item_is_not_selected) || tvPomodoroTimer.text == getString(R.string.tv_pomodoro_timer_not_set)) {
                Toast.makeText(this@PomodoroActivity, "아직 설정되지 않은 값이 있습니다!", Toast.LENGTH_SHORT).show()
            }  else {
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
        if(SharedData.pomodoroStatus == PomodoroStatus.ESCAPE_SUCCESS) {
            // 해당 할 일을 아직 다 끝내지 못한 상태
//            sharedViewModel.resetButtonCount()
            timer?.cancel()
            timer = null
            totalSecond = 0
            binding.tvPomodoroTimer.text =
                getString(R.string.tv_pomodoro_timer_not_set) // [?] zero? not set?
            binding.rb25Minutes.isChecked = false
            binding.rb50Minutes.isChecked = false
            binding.rb25Minutes.isClickable = true
            binding.rb50Minutes.isClickable = true
            Toast.makeText(this, "비상탈출을 완료했습니다!", Toast.LENGTH_SHORT).show()
        } else if(SharedData.pomodoroStatus == PomodoroStatus.ESCAPE_FAILURE) {
            timer = timer(
                initialDelay = 1000,
                period = 1000
            ) { // period 단위 = millisecond ( 1000 millisecond = 1 second )
                if (totalSecond > 0) {
                    totalSecond -= 1
                    val currentHour = totalSecond / 3600
                    val currentMinute = totalSecond / 60
                    val currentSecond = totalSecond % 60
                    runOnUiThread {
                        // worker thread 는 ui 에 접근할 수 없다. → Activity.runOnUiThread(Runnable) 이용
                        binding.tvPomodoroTimer.text =
                            String.format(
                                "%02d:%02d:%02d",
                                currentHour,
                                currentMinute,
                                currentSecond
                            )
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
