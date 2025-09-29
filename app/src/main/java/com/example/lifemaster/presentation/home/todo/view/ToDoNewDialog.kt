package com.example.lifemaster.presentation.home.todo.view

import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import com.example.lifemaster.R
import com.example.lifemaster.databinding.DialogTodoNewBinding
import com.example.lifemaster.presentation.home.todo.adapter.ToDoNewAdapter
import com.example.lifemaster.presentation.home.todo.model.TodoModel

class ToDoNewDialog(private val remoteTodoItem: List<TodoModel>): DialogFragment(R.layout.dialog_todo_new) {

    private lateinit var binding: DialogTodoNewBinding
    private val todoNewAdapter = ToDoNewAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DialogTodoNewBinding.bind(view)
        initViews()
        initListeners()
    }

    private fun initViews() = with(binding) {
        recyclerviewTodoDialog.adapter = todoNewAdapter
        todoNewAdapter.submitList(remoteTodoItem)
    }

    private fun initListeners() = with(binding) {
        btnTodoDialogApply.setOnClickListener {

        }
        btnTodoDialogCancel.setOnClickListener {
            dismiss()
        }
    }

    companion object {
        const val TAG = "ToDoNewDialog"
    }
}