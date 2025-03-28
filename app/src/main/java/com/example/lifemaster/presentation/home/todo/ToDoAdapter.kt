package com.example.lifemaster.presentation.home.todo

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.lifemaster.databinding.ItemTodoBinding
import com.example.lifemaster.model.PomodoroTimer
import com.example.lifemaster.model.TodoItem
import com.example.lifemaster.network.RetrofitInstance
import com.example.lifemaster.presentation.home.pomodoro.PomodoroActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ToDoAdapter(
    private val context: Context,
    private val toDoViewModel: ToDoViewModel,
    private val fragmentManager: FragmentManager
) :
    ListAdapter<TodoItem, ToDoAdapter.ToDoViewHolder>(differ) {
    inner class ToDoViewHolder(private val binding: ItemTodoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TodoItem) {
            bindViews(item)
            bindEvents(item)
        }

        private fun bindEvents(item: TodoItem) = with(binding) {
            itemView.setOnLongClickListener {
                showDialog(item)
                true
            }
            chIsCompleted.setOnCheckedChangeListener { _, _ ->
                toggleTodoStatus(item)
            }
            ivEdit.setOnClickListener {
                val dialog = ToDoDialog(TODO.EDIT, item)
                dialog.isCancelable = false
                dialog.show(fragmentManager, ToDoDialog.TAG)
            }
            ivGoToPomodoro.setOnClickListener {
                val intent = Intent(context, PomodoroActivity::class.java).apply {
                    putExtra("item", item)
                }
                context.startActivity(intent)
            }
        }

        private fun toggleTodoStatus(item: TodoItem) {
            RetrofitInstance.networkService.toggleTodoItem(item.id)
                .enqueue(object : Callback<TodoItem> {
                    override fun onResponse(
                        call: Call<TodoItem>,
                        response: Response<TodoItem>
                    ) {
                        if (response.isSuccessful) {
                            val todoItem = response.body() ?: return
                            if (todoItem.isCompleted) {
                                Toast.makeText(
                                    context,
                                    "할일이 완료되었습니다!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(
                                    context,
                                    "할일이 해제되었습니다!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {

                        }
                    }

                    override fun onFailure(call: Call<TodoItem>, t: Throwable) {
                        TODO("Not yet implemented")
                    }
                })
        }

        private fun bindViews(item: TodoItem) = with(binding) {
            tvTitle.text = item.title
            chIsCompleted.isChecked = currentList[adapterPosition].isCompleted
            if(item.timer == PomodoroTimer.TIMER_25) ivTimer25.visibility = View.VISIBLE else if(item.timer == PomodoroTimer.TIMER_50) ivTimer50.visibility = View.VISIBLE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToDoViewHolder {
        return ToDoViewHolder(
            ItemTodoBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ToDoViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    private fun showDialog(deleteItem: TodoItem) {
        AlertDialog.Builder(context)
            .setTitle("할일 목록 삭제")
            .setMessage("할일 목록을 삭제하시겠습니까?")
            .setPositiveButton("예") { _, _ ->
                RetrofitInstance.networkService.deleteTodoItem(deleteItem.id)
                    .enqueue(object : Callback<Any> {
                        override fun onResponse(call: Call<Any>, response: Response<Any>) {
                            if (response.isSuccessful) {
                                Toast.makeText(context, "할일이 삭제되었습니다!", Toast.LENGTH_SHORT).show()
                                toDoViewModel.deleteTodoItems(deleteItem)
                            } else {
                                Log.d("server success", "else")
                            }
                        }

                        override fun onFailure(call: Call<Any>, t: Throwable) {
                            Log.d("server error", "" + t.message)
                        }
                    })
            }
            .setNegativeButton("아니요", null)
            .show()
    }

    companion object {
        val differ = object : DiffUtil.ItemCallback<TodoItem>() {
            override fun areContentsTheSame(oldItem: TodoItem, newItem: TodoItem): Boolean {
                return oldItem == newItem
            }

            override fun areItemsTheSame(oldItem: TodoItem, newItem: TodoItem): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }
}