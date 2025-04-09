package com.example.lifemaster.presentation.home.todo

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.lifemaster.R
import androidx.fragment.app.DialogFragment
import com.example.lifemaster.databinding.DialogLongClickTodoBinding
import com.example.lifemaster.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ToDoLongClickDialog(
    private val todoItem: TodoItem,
    private val todoViewModel: ToDoViewModel,
    private val userToken: String?
): DialogFragment(R.layout.dialog_long_click_todo) {

    lateinit var binding: DialogLongClickTodoBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DialogLongClickTodoBinding.bind(view)
        initListeners()
    }

    private fun initListeners() = with(binding) {
        btnModify.setOnClickListener {
            dismiss()
            val dialog = ToDoDialog(TODO.EDIT, todoItem, userToken)
            dialog.isCancelable = false
            dialog.show(parentFragmentManager, ToDoDialog.TAG)
        }
        btnDelete.setOnClickListener {
            // 삭제
            Log.d("ttest", ""+todoItem)
            RetrofitInstance.networkService.deleteTodoItem(token = "Bearer $userToken", todoItem.id)
                .enqueue(object : Callback<Any> {
                    override fun onResponse(call: Call<Any>, response: Response<Any>) {
                        if (response.isSuccessful) {
                            Toast.makeText(context, "할일이 삭제되었습니다!", Toast.LENGTH_SHORT).show()
                            todoViewModel.deleteTodoItems(todoItem)
                            dismiss()
                        } else {
                            Log.d("ttest", response.message())
                        }
                    }

                    override fun onFailure(call: Call<Any>, t: Throwable) {
                        Log.d("ttest", "" + t.message)
                    }
                })
        }
        btnCancel.setOnClickListener { dismiss() }
    }

    companion object {
        const val TAG = "ToDoLongClickDialog"
    }
}