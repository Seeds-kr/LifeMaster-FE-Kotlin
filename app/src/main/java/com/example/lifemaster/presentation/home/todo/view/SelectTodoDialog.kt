package com.example.lifemaster.presentation.home.todo.view

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.DialogFragment
import com.example.lifemaster.R
import com.example.lifemaster.databinding.DialogSelectTodoBinding
import com.example.lifemaster.presentation.home.todo.adapter.ToDoSelectAdapter
import com.example.lifemaster.presentation.home.todo.model.TodoModel

class SelectTodoDialog(
    private val todoItems: List<TodoModel>,
    private val currentItem: TodoModel,
    private val onItemSelected: (TodoModel) -> Unit
): DialogFragment(R.layout.dialog_select_todo) {

    private lateinit var binding: DialogSelectTodoBinding
    private val todoSelectAdapter = ToDoSelectAdapter(currentItem)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return Dialog(requireContext()).apply {
            window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DialogSelectTodoBinding.bind(view)
        initViews()
        initListeners()
    }

    private fun initViews() = with(binding) {
        todoSelectRecyclerview.adapter = todoSelectAdapter
        todoSelectAdapter.submitList(todoItems)
    }

    private fun initListeners() = with(binding) {
        btnSelect.setOnClickListener {
            val todoItem = todoSelectAdapter.getSelectedItem()
            if(todoItem != null) {
                onItemSelected(todoItem)
                dismiss()
            } else {
                Toast.makeText(context, "할일을 선택해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
        btnCancel.setOnClickListener {
            dismiss()
        }
    }

    companion object {
        const val TAG = "SelectTodoDialog"
    }
}