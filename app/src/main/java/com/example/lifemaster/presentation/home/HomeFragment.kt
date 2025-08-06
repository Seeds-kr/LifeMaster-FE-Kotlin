package com.example.lifemaster.presentation.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.lifemaster.R
import com.example.lifemaster.databinding.FragmentHomeBinding
import com.example.lifemaster.presentation.home.pomodoro.model.PomodoroItem
import com.example.lifemaster.network.RetrofitInstance
import com.example.lifemaster.presentation.home.todo.model.TODO
import com.example.lifemaster.presentation.home.todo.adapter.ToDoAdapter
import com.example.lifemaster.presentation.home.todo.view.ToDoDialog
import com.example.lifemaster.presentation.home.todo.viewmodel.ToDoViewModel
import com.example.lifemaster.presentation.home.todo.model.TodoItem
import com.example.lifemaster.presentation.home.calendar.view.CalendarFragment
import com.example.lifemaster.presentation.home.edit.view.HomeEditActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment() {

    lateinit var binding: FragmentHomeBinding
    lateinit var todoItems: ArrayList<TodoItem>
    private val toDoViewModel: ToDoViewModel by activityViewModels()
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

        binding.containerCalendar.post {
            if (childFragmentManager.findFragmentById(R.id.container_calendar) == null) {
                childFragmentManager.beginTransaction()
                    .replace(R.id.container_calendar, CalendarFragment())
                    .commit()
            }
        }

        val (visible, ordered) = loadHomeConfiguration()
        applyHomeLayout(visible, ordered)

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
}