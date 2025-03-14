package com.example.lifemaster.presentation.home

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.lifemaster.R
import com.example.lifemaster.databinding.FragmentHomeBinding
import com.example.lifemaster.model.TodoItem
import com.example.lifemaster.network.RetrofitInstance
import com.example.lifemaster.presentation.home.todo.TODO
import com.example.lifemaster.presentation.home.todo.ToDoAdapter
import com.example.lifemaster.presentation.home.todo.ToDoDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ToDoFragment : Fragment(R.layout.fragment_home) {

    lateinit var binding: FragmentHomeBinding
    private val toDoViewModel: ToDoViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)
        initViews()
        initListeners()
        initObservers()
    }

    private fun initViews() {
        binding.recyclerview.adapter = ToDoAdapter(requireContext(), toDoViewModel, childFragmentManager)
        RetrofitInstance.networkService.getTodoItems().enqueue(object : Callback<List<TodoItem>> {
            override fun onResponse(
                call: Call<List<TodoItem>>,
                response: Response<List<TodoItem>>
            ) {
                if(response.isSuccessful) {
                    val todoItems = response.body() as ArrayList<TodoItem>
                    toDoViewModel.updateTodoItems(todoItems)
                } else {
                    Log.d("server success", "else")
                }
            }

            override fun onFailure(call: Call<List<TodoItem>>, t: Throwable) {
                Log.d("server error", ""+t.message)
            }
        })
    }

    private fun initListeners() {
        binding.btnAddTodoItem.setOnClickListener {
            val dialog = ToDoDialog(TODO.ADD)
            dialog.isCancelable = false
            dialog.show(childFragmentManager, ToDoDialog.TAG)
        }
    }

    private fun initObservers() {
        toDoViewModel.todoItems.observe(viewLifecycleOwner) { updateItems ->
            updateItems?.let { (binding.recyclerview.adapter as ToDoAdapter).submitList(it.toList()) }
        }
    }
}