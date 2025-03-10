package com.example.lifemaster.presentation.home.todo

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.lifemaster.R
import com.example.lifemaster.databinding.DialogTodoBinding
import com.example.lifemaster.network.RetrofitInstance
import com.example.lifemaster.presentation.home.ToDoViewModel
import retrofit2.Call
import java.time.LocalDate
import retrofit2.Callback
import retrofit2.Response
import java.time.format.DateTimeFormatter

class ToDoDialog : DialogFragment(R.layout.dialog_todo) {

    lateinit var binding: DialogTodoBinding
    private val toDoViewModel: ToDoViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DialogTodoBinding.bind(view)
        setupListeners()
    }

    private fun setupListeners() = with(binding) {

        btnAdd.setOnClickListener {
            val title = etTitle.text.toString()
            if (title.isBlank()) Toast.makeText(requireContext(), "내용을 입력해 주세요!", Toast.LENGTH_SHORT).show()
            else {
                RetrofitInstance.networkService.registerTodoItem(
                    date = getTodayDate(),
                    title = title
                ).enqueue(object: Callback<Any> {
                    override fun onResponse(call: Call<Any>, response: Response<Any>) {
                        if (response.isSuccessful) {
                            Toast.makeText(context, "할일이 등록되었습니다.", Toast.LENGTH_SHORT).show()
                            dismiss()
                        } else {
                            Log.d("server success", "else")
                        }
                    }
                    override fun onFailure(call: Call<Any>, t: Throwable) {
                        Log.d("server error", ""+t.message)
                    }
                })
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