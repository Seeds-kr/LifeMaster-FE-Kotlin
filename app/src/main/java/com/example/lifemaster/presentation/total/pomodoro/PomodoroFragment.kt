package com.example.lifemaster.presentation.total.pomodoro

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.lifemaster.R
import com.example.lifemaster.presentation.data.SharedData
import com.example.lifemaster.databinding.FragmentPomodoroBinding
import com.example.lifemaster.presentation.total.pomodoro.emergency_escape.EmergencyEscapeActivity
import java.util.*
import kotlin.concurrent.timer

class PomodoroFragment : Fragment() {

    lateinit var binding: FragmentPomodoroBinding

    private val sharedViewModel: PomodoroViewModel by activityViewModels()

    private var totalSecond = 0

    private var timer: Timer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("Fragment_Pomodoro", "onCreate")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("Fragment_Pomodoro", "onCreateView")
        binding = FragmentPomodoroBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("Fragment_Pomodoro", "onViewCreated")
        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.ivOpenTodoDialog.setOnClickListener {
            val dialog = PomodoroDialog()
            dialog.isCancelable = false
            dialog.show(requireActivity().supportFragmentManager, PomodoroDialog.TAG)
        }
        binding.rb25Minutes.setOnClickListener {
            binding.tvPomodoroTimer.text = getString(R.string.tv_pomodoro_timer_25)
        }
        binding.rb50Minutes.setOnClickListener {
            binding.tvPomodoroTimer.text = getString(R.string.tv_pomodoro_timer_50)
        }
        binding.btnStartPomodoro.setOnClickListener {
            if(binding.tvPomodoroTimer.text.toString() == getString(R.string.tv_pomodoro_timer_25) && binding.tvSelectTodoItem.text.toString()!=getString(R.string.item_is_not_selected)) {
                binding.rb25Minutes.isClickable = false
                binding.rb50Minutes.isClickable = false // [refactor] binding 도 많이 중복되니 scope function 으로 빼서 중복 피하기
                sharedViewModel.clickButton()
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
                        requireActivity().runOnUiThread {
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
                        requireActivity().runOnUiThread {
                            Toast.makeText(context, "시간이 종료되었습니다!", Toast.LENGTH_SHORT).show()
                            binding.rb25Minutes.isChecked = false
                            binding.rb25Minutes.isClickable = true
                            binding.rb50Minutes.isClickable = true
                            sharedViewModel.resetButtonCount()
                        } // [fix] 왜 25분을 다시 클릭했을 때 ui 색상이 안변하지?
                        timer?.cancel()
                        timer = null
                        // totalSecond = 0
                    }
                }
            } else if(binding.tvPomodoroTimer.text.toString() == getString(R.string.tv_pomodoro_timer_50) && binding.tvSelectTodoItem.text.toString()!=getString(R.string.item_is_not_selected)) {
                binding.rb25Minutes.isClickable = false
                binding.rb50Minutes.isClickable = false
                sharedViewModel.clickButton()
                totalSecond = 50 * 60
                timer = timer(initialDelay = 0, period = 1000) { // worker thread
                    if (totalSecond > 0) { // [refactor] 반복되는 부분 함수로 빼기
                        totalSecond -= 1
                        val currentHour = totalSecond / 3600
                        val currentMinute = totalSecond / 60
                        val currentSecond = totalSecond % 60
                        requireActivity().runOnUiThread {
                            binding.tvPomodoroTimer.text =
                                String.format(
                                    "%02d:%02d:%02d",
                                    currentHour,
                                    currentMinute,
                                    currentSecond
                                )
                        }
                    } else {
                        requireActivity().runOnUiThread {
                            Toast.makeText(context, "시간이 종료되었습니다!", Toast.LENGTH_SHORT)
                                .show()
                            binding.rb50Minutes.isChecked = false
                            binding.rb25Minutes.isClickable = true
                            binding.rb50Minutes.isClickable = true
                            sharedViewModel.resetButtonCount()
                        }
                        timer?.cancel()
                        timer = null
                    }
                }
            } else if(binding.tvSelectTodoItem.text == getString(R.string.item_is_not_selected) || binding.tvPomodoroTimer.text == getString(R.string.tv_pomodoro_timer_not_set)) {
                Toast.makeText(requireContext(), "아직 설정되지 않은 값이 있습니다!", Toast.LENGTH_SHORT).show()
            }  else {
                sharedViewModel.clickButton()
            }
        }
    }

    fun observeViewModel() {
        sharedViewModel.selectedPosition.observe(viewLifecycleOwner) { selectedPosition ->
            binding.tvSelectTodoItem.text = SharedData.todoItems[selectedPosition]
        }
        sharedViewModel.buttonCount.observe(viewLifecycleOwner) { btnCount ->
            when (btnCount) {
                0 -> {
                    binding.btnStartPomodoro.text = "시작하기"
                }
                1 -> {
                    binding.btnStartPomodoro.text = "비상 탈출"
                }
                else -> {
                    val intent = Intent(requireContext(), EmergencyEscapeActivity::class.java)
                    intent.putExtra("remain_timer", binding.tvPomodoroTimer.text.toString())
                    startActivity(intent)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d("Fragment_Pomodoro", "onStart")
        if(SharedData.pomodoroStatus == PomodoroStatus.ESCAPE_SUCCESS) {
            // 해당 할 일을 아직 다 끝내지 못한 상태
            sharedViewModel.resetButtonCount()
            timer?.cancel()
            timer = null
            totalSecond = 0
            binding.tvPomodoroTimer.text =
                getString(R.string.tv_pomodoro_timer_not_set) // [?] zero? not set?
            binding.rb25Minutes.isChecked = false
            binding.rb50Minutes.isChecked = false
            binding.rb25Minutes.isClickable = true
            binding.rb50Minutes.isClickable = true
            Toast.makeText(requireContext(), "비상탈출을 완료했습니다!", Toast.LENGTH_SHORT).show()
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
                    requireActivity().runOnUiThread {
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
                    requireActivity().runOnUiThread {
                        Toast.makeText(context, "시간이 종료되었습니다!", Toast.LENGTH_SHORT)
                            .show()
                        binding.rb25Minutes.isChecked = false
                        binding.rb25Minutes.isClickable = true
                        binding.rb50Minutes.isClickable = true
                        sharedViewModel.resetButtonCount()
                    } // [?] 왜 25분을 다시 클릭했을 때 ui 색상이 안변하지?
                    timer?.cancel()
                    timer = null
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("Fragment_Pomodoro", "onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d("Fragment_Pomodoro", "onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d("Fragment_Pomodoro", "onStop")
        timer?.cancel()
        timer = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("Fragment_Pomodoro", "onDestroyView")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("Fragment_Pomodoro", "onDestroy")
    }
}
