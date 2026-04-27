package com.example.lifemaster.presentation.home.todo.view

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.lifemaster.R
import com.example.lifemaster.databinding.DialogTodoBinding
import com.example.lifemaster.network.NetworkService
import com.example.lifemaster.presentation.home.todo.model.TODO
import com.example.lifemaster.presentation.home.todo.model.TodoModel
import com.example.lifemaster.presentation.home.todo.model.TodoRequest
import com.example.lifemaster.presentation.home.todo.viewmodel.ToDoViewModel
import com.example.lifemaster.presentation.home.todo.model.TodoItem
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

// 할일 추가하기, 할일 수정하기에서 같이 사용하는 다이얼로그
@AndroidEntryPoint
class ToDoDialog(
    private val origin: TODO,
    private val item: TodoModel? = null
) : DialogFragment(R.layout.dialog_todo) {

    private lateinit var binding: DialogTodoBinding
    @Inject
    lateinit var networkService: NetworkService

    lateinit var binding: DialogTodoBinding
    private val toDoViewModel: ToDoViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return Dialog(requireContext()).apply {
            window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DialogTodoBinding.bind(view)
        initViews()
        initListeners()
    }

    private fun initViews() = with(binding) {
        when (origin) {
            TODO.ADD -> {
                tvTodoTitle.text = "할일 추가"
                btnChange.text = "추가하기"
            }
            TODO.EDIT -> {
                if(item == null) return@with
                tvTodoTitle.text = "할일 수정"
                btnChange.text = "수정하기"
                etTodoTitle.setText(item.title)
            }
        }
    }

    private fun initListeners() = with(binding) {

        when (origin) {
            TODO.ADD -> {
                btnChange.setOnClickListener {
                    val title = etTitle.text.toString()
                    if (title.isBlank()) Toast.makeText(
                        requireContext(),
                        "내용을 입력해 주세요!",
                        Toast.LENGTH_SHORT
                    ).show()
                    else {
                        networkService.registerTodoItem(
                            token = "Bearer $userToken",
                            todoItem = TodoItem(
                                date = getTodayDate(),
                                title = title,
                                isCompleted = false
                            ),
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
                    val title = etTodoTitle.text.toString()
                    if (title.isBlank()) {
                        Toast.makeText(
                            requireContext(),
                            "내용을 입력해 주세요.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        val request = TodoRequest(
                            date = getTodayDate(),
                            title = title
                        )
                        toDoViewModel.addTodoItem(request = request)
                    }
                }
            }

            TODO.EDIT -> {
                btnChange.setOnClickListener {
                    if(item == null) return@setOnClickListener
                    val title = etTodoTitle.text.toString()
                    if (title.isBlank()) {
                        Toast.makeText(
                            requireContext(),
                            "내용을 입력해 주세요!",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                       toDoViewModel.updateItem(
                           id = item.id,
                           date = getTodayDate(),
                           title = title
                       )
                    val title = etTitle.text.toString()
                    if (title.isBlank()) Toast.makeText(
                        requireContext(),
                        "내용을 입력해 주세요!",
                        Toast.LENGTH_SHORT
                    ).show()
                    else {
                        networkService.updateTodoItem(
                            token = "Bearer $userToken",
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
                                Log.d("server", t.message!!)
                            }
                        })
                    }
                }
            }
        }

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