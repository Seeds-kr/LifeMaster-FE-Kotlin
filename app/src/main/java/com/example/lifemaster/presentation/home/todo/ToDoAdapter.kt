package com.example.lifemaster.presentation.home.todo

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.lifemaster.R
import com.example.lifemaster.model.TodoItem
import androidx.recyclerview.widget.ListAdapter
import com.example.lifemaster.databinding.ItemTodoBinding

class ToDoAdapter: ListAdapter<TodoItem, ToDoAdapter.ToDoViewHolder>(differ) {
    inner class ToDoViewHolder(private val binding: ItemTodoBinding): RecyclerView.ViewHolder(binding.root) {
//        val content: TextView
//        val goToPomodoro: ImageView
//        init {
//            content = itemView.findViewById(R.id.tv_todo_item)
//            goToPomodoro = itemView.findViewById(R.id.iv_go_to_pomodoro)
//
//            goToPomodoro.setOnClickListener {
////                // 1. home tab -> total view 전환
////                activity.findViewById<BottomNavigationView>(R.id.navigation).selectedItemId = R.id.action_total
////                // 2. total view 중 포모도로 프래그먼트 화면에 보이기
////                activity.supportFragmentManager.beginTransaction().replace(R.id.fragmentContainerView, PomodoroFragment(), "PomodoroFragment").commit()
//            }
//        }

        fun bind(item: TodoItem) = with(binding) {
            tvTitle.text = item.title
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToDoViewHolder {
        return ToDoViewHolder(ItemTodoBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ToDoViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    companion object {
        val differ = object: DiffUtil.ItemCallback<TodoItem>() {
            override fun areContentsTheSame(oldItem: TodoItem, newItem: TodoItem): Boolean {
                return oldItem == newItem
            }

            override fun areItemsTheSame(oldItem: TodoItem, newItem: TodoItem): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }
}