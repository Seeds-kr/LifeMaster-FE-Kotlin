package com.example.lifemaster.presentation.home.todo.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.daimajia.swipe.SwipeLayout
import com.example.lifemaster.databinding.ItemTodoBinding
import com.example.lifemaster.network.RetrofitInstance
import com.example.lifemaster.presentation.home.pomodoro.view.PomodoroActivity
import com.example.lifemaster.presentation.home.todo.model.TodoModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ToDoAdapter (
    private val context: Context,
    private val onDeleteClicked: (Int) -> Unit
) :
    ListAdapter<TodoModel, ToDoAdapter.ToDoViewHolder>(differ) {
    inner class ToDoViewHolder(private val binding: ItemTodoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TodoModel) {
            bindViews(item)
            bindEvents(item)
        }

        private fun bindViews(item: TodoModel) = with(binding) {
            tvTodoTitle.text = item.title
            checkboxTodoIsCompleted.isChecked = item.isCompleted
            root.showMode = SwipeLayout.ShowMode.PullOut
//            llTimerContainer25.removeAllViews()
//            if(item.timer25Number > 0) {
//                llTimerContainer25.visibility = View.VISIBLE
//                repeat(item.timer25Number) {
//                    val timer25ImageView = ImageView(root.context).apply {
//                        setImageResource(R.drawable.ic_timer_25)
//                        val size = 18
//                        layoutParams = LinearLayout.LayoutParams(size.dp, size.dp).apply {
//                            marginEnd = 4.dp
//                        }
//                    }
//                    llTimerContainer25.addView(timer25ImageView)
//                }
//            } else {
//                llTimerContainer25.visibility = View.GONE
//            }

            // 50분 타이머 동적 추가
//            llTimerContainer50.removeAllViews()
//            if(item.timer50Number > 0) {
//                llTimerContainer50.visibility = View.VISIBLE
//                repeat(item.timer50Number) {
//                    val timer50ImageView = ImageView(root.context).apply {
//                        setImageResource(R.drawable.ic_timer_50)
//                        val size = 18
//                        layoutParams = LinearLayout.LayoutParams(size.dp, size.dp).apply {
//                            marginEnd = 4.dp
//                        }
//                    }
//                    llTimerContainer50.addView(timer50ImageView)
//                }
//            } else {
//                llTimerContainer50.visibility = View.GONE
//            }
        }

        private fun bindEvents(item: TodoModel) = with(binding) {
//            chIsCompleted.setOnCheckedChangeListener { _, _ ->
//                toggleTodoStatuIs(item)
//            }
            flTodoEdit.setOnClickListener {

            }
            flTodoDelete.setOnClickListener {
                onDeleteClicked(item.id)
            }
            ivGoToPomodoro.setOnClickListener {
                val intent = Intent(context, PomodoroActivity::class.java).apply {
                    putExtra("item", item)
                }
                context.startActivity(intent)
            }
        }

        private fun toggleTodoStatus(item: TodoModel) {
            RetrofitInstance.networkService.toggleTodoItem(item.id)
                .enqueue(object : Callback<TodoModel> {
                    override fun onResponse(
                        call: Call<TodoModel>,
                        response: Response<TodoModel>
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

                    override fun onFailure(call: Call<TodoModel>, t: Throwable) {
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
        val differ = object : DiffUtil.ItemCallback<TodoModel>() {
            override fun areContentsTheSame(oldItem: TodoModel, newItem: TodoModel): Boolean {
                return oldItem == newItem
            }

            override fun areItemsTheSame(oldItem: TodoModel, newItem: TodoModel): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }
}