package com.example.lifemaster.presentation.home

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.lifemaster.R
import com.example.lifemaster.databinding.FragmentHomeBinding
import com.example.lifemaster.presentation.home.pomodoro.model.PomodoroItem
import com.example.lifemaster.network.RetrofitInstance
import com.example.lifemaster.presentation.home.sleep.viewmodel.SleepViewModel
import com.example.lifemaster.presentation.home.sleep.viewmodel.SleepViewModelFactory
import com.example.lifemaster.presentation.home.todo.model.TODO
import com.example.lifemaster.presentation.home.todo.adapter.ToDoAdapter
import com.example.lifemaster.presentation.home.todo.view.ToDoDialog
import com.example.lifemaster.presentation.home.todo.viewmodel.ToDoViewModel
import com.example.lifemaster.presentation.home.todo.model.TodoItem
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate

class HomeFragment : Fragment() {

    lateinit var binding: FragmentHomeBinding
    lateinit var todoItems: ArrayList<TodoItem>
    private val toDoViewModel: ToDoViewModel by activityViewModels()
    private val sleepViewModel: SleepViewModel by activityViewModels {
        SleepViewModelFactory(RetrofitInstance.networkService)
    }
    private var userToken: String? = null

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
        binding = FragmentHomeBinding.bind(view)
//        initViews()
        initListeners()
//        initObservers()
        // 수면 정보 UI 업데이트
        binding.tvSleepDate.text = "${LocalDate.now().monthValue}월 ${LocalDate.now().dayOfMonth}일"
        binding.tvHomeSleepDuration.text = if(sleepViewModel.isMeasured) "나중에 수정 예정" else "수면 시간 미측정"
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

    private fun initListeners() = with(binding) {
//        btnAddTodoItem.setOnClickListener {
//            val dialog = ToDoDialog(caller = TODO.ADD, userToken = userToken)
//            dialog.show(childFragmentManager, ToDoDialog.Companion.TAG)
//        }
        cvGoToAlarm.setOnClickListener { findNavController().navigate(R.id.action_homeFragment_to_alarmListFragment) }
        btnSleepReport.setOnClickListener { findNavController().navigate(R.id.action_homeFragment_to_sleepPlaylistFragment) }
    }

    private fun initObservers() {
        toDoViewModel.todoItems.observe(viewLifecycleOwner) { updateItems ->
            val newList = updateItems.map { it.copy() }
            (binding.recyclerview.adapter as ToDoAdapter).submitList(newList)
        }
    }
}