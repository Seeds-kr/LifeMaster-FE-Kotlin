package com.example.lifemaster.presentation.home.todo

import com.example.lifemaster.R
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.lifemaster.databinding.ItemTodoBinding
import com.example.lifemaster.network.RetrofitInstance
import com.example.lifemaster.presentation.home.pomodoro.PomodoroActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.lifemaster.dp

class ToDoAdapter(
    private val context: Context,
    private val toDoViewModel: ToDoViewModel,
    private val fragmentManager: FragmentManager,
    private val userToken: String?
) :
    ListAdapter<TodoItem, ToDoAdapter.ToDoViewHolder>(differ) {
    inner class ToDoViewHolder(private val binding: ItemTodoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TodoItem) {
            bindViews(item)
            bindEvents(item)
        }

        private fun bindViews(item: TodoItem) = with(binding) {
            tvTitle.text = item.title
            chIsCompleted.isChecked = currentList[adapterPosition].isCompleted
            llTimerContainer25.removeAllViews()
            if(item.timer25Number > 0) {
                llTimerContainer25.visibility = View.VISIBLE
                repeat(item.timer25Number) {
                    val timer25ImageView = ImageView(root.context).apply {
                        setImageResource(R.drawable.ic_timer_25)
                        val size = 18
                        layoutParams = LinearLayout.LayoutParams(size.dp, size.dp).apply {
                            marginEnd = 4.dp
                        }
                    }
                    llTimerContainer25.addView(timer25ImageView)
                }
            } else {
                llTimerContainer25.visibility = View.GONE
            }

            // 50분 타이머 동적 추가
            llTimerContainer50.removeAllViews()
            if(item.timer50Number > 0) {
                llTimerContainer50.visibility = View.VISIBLE
                repeat(item.timer50Number) {
                    val timer50ImageView = ImageView(root.context).apply {
                        setImageResource(R.drawable.ic_timer_50)
                        val size = 18
                        layoutParams = LinearLayout.LayoutParams(size.dp, size.dp).apply {
                            marginEnd = 4.dp
                        }
                    }
                    llTimerContainer50.addView(timer50ImageView)
                }
            } else {
                llTimerContainer50.visibility = View.GONE
            }
        }

        private fun bindEvents(item: TodoItem) = with(binding) {
            itemView.setOnLongClickListener {
                val dialog = ToDoLongClickDialog(item, toDoViewModel, userToken)
                dialog.isCancelable = false
                dialog.show(fragmentManager, ToDoLongClickDialog.TAG) // childFragmentManager
                true
            }
            chIsCompleted.setOnCheckedChangeListener { _, _ ->
                toggleTodoStatus(item)
            }
            ivGoToPomodoro.setOnClickListener {
                val intent = Intent(context, PomodoroActivity::class.java).apply {
                    putExtra("item", item)
                }
                context.startActivity(intent)
            }
        }

        private fun toggleTodoStatus(item: TodoItem) {
            RetrofitInstance.networkService.toggleTodoItem(token = "Bearer $userToken", item.id)
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

    companion object {
        val differ = object : DiffUtil.ItemCallback<TodoItem>() {
            override fun areContentsTheSame(oldItem: TodoItem, newItem: TodoItem): Boolean {
                val result = oldItem == newItem
                Log.d("DiffUtil", "Comparing: $oldItem vs $newItem => $result")
                return result
            }

            override fun areItemsTheSame(oldItem: TodoItem, newItem: TodoItem): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }
}