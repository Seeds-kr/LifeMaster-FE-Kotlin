package com.example.lifemaster.presentation.home.todo

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.lifemaster.R
import com.example.lifemaster.databinding.DialogTodoBinding
import com.example.lifemaster.model.TodoItem
import com.example.lifemaster.network.RetrofitInstance
import retrofit2.Call
import java.time.LocalDate
import retrofit2.Callback
import retrofit2.Response
import java.time.format.DateTimeFormatter

class ToDoDialog(
    private val caller: TODO,
    private val todoItem: TodoItem? = null
) : DialogFragment(R.layout.dialog_todo) {

    lateinit var binding: DialogTodoBinding
    private val toDoViewModel: ToDoViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DialogTodoBinding.bind(view)
        initViews()
        initListeners()
    }

    private fun initViews() = with(binding) {
        when(caller) {
            TODO.ADD -> {
                tvTitle.text = "할일 추가"
                btnChange.text = "추가하기"
            }
            TODO.EDIT -> {
                etTitle.setText(todoItem?.title)
                tvTitle.text = "할일 수정"
                btnChange.text = "수정하기"
            }
        }
    }

    private fun initListeners() = with(binding) {

        when(caller) {
            TODO.ADD -> {
                btnChange.setOnClickListener {
                    val title = etTitle.text.toString()
                    if (title.isBlank()) Toast.makeText(
                        requireContext(),
                        "내용을 입력해 주세요!",
                        Toast.LENGTH_SHORT
                    ).show()
                    else {
                        RetrofitInstance.networkService.registerTodoItem(
                            date = getTodayDate(),
                            title = title
                        ).enqueue(object : Callback<TodoItem> {
                            override fun onResponse(call: Call<TodoItem>, response: Response<TodoItem>) {
                                if (response.isSuccessful) {
                                    val newItem = response.body()
                                    newItem?.let { toDoViewModel.addTodoItems(it) }
                                    Toast.makeText(requireContext(), "할일이 등록되었습니다!", Toast.LENGTH_SHORT).show()
                                    dismiss()
                                } else {
                                    Log.d("server success", "else")
                                }
                            }

                            override fun onFailure(call: Call<TodoItem>, t: Throwable) {
                                Log.d("server error", "" + t.message)
                            }
                        })
                    }
                }
            }
            TODO.EDIT -> {
                btnChange.setOnClickListener {
                    val title = etTitle.text.toString()
                    if (title.isBlank()) Toast.makeText(
                        requireContext(),
                        "내용을 입력해 주세요!",
                        Toast.LENGTH_SHORT
                    ).show()
                    else {
                        RetrofitInstance.networkService.updateTodoItem(
                            id = todoItem?.id ?: 0,
                            title = title,
                            date = getTodayDate()
                        ).enqueue(object : Callback<TodoItem> {
                            override fun onResponse(
                                call: Call<TodoItem>,
                                response: Response<TodoItem>
                            ) {
                                if(response.isSuccessful) {
                                    val todoItem = response.body()
                                    todoItem?.let { toDoViewModel.changeTodoItems(it) }
                                    Toast.makeText(context, "할일이 수정되었습니다!", Toast.LENGTH_SHORT).show()
                                    dismiss()
                                }
                            }
                            override fun onFailure(call: Call<TodoItem>, t: Throwable) {
                                TODO("Not yet implemented")
                            }
                        })
                    }
                }
            }
        }

        // 공통
        btnCancel.setOnClickListener {
            dismiss()
        }

    }

    private fun getTodayDate(): String {
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
        return today.format(formatter)
    }

    companion object {
        const val TAG = "ToDoDialog"
    }
}