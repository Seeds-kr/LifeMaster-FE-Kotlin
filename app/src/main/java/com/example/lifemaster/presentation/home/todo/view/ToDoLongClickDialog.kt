package com.example.lifemaster.presentation.home.todo.view

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.lifemaster.presentation.home.todo.model.TODO
import com.example.lifemaster.presentation.home.todo.model.TodoModel
import com.example.lifemaster.presentation.home.todo.viewmodel.ToDoViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ToDoLongClickDialog(
    private val todoItem: TodoModel
) : DialogFragment() {

    private val todoViewModel: ToDoViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext())
            .setItems(arrayOf("수정", "삭제", "취소")) { dialog, which ->
                when (which) {
                    0 -> showEditDialog()
                    1 -> todoViewModel.deleteTodoItem(deleteId = todoItem.id)
                    else -> dialog.dismiss()
                }
            }
            .create()
    }

    private fun showEditDialog() {
        ToDoDialog(TODO.EDIT, todoItem)
            .show(parentFragmentManager, ToDoDialog.TAG)
    }

    companion object {
        const val TAG = "ToDoLongClickDialog"
    }
}