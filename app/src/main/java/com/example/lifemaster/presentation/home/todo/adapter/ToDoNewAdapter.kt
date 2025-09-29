package com.example.lifemaster.presentation.home.todo.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.lifemaster.databinding.ItemTodoNewBinding
import com.example.lifemaster.presentation.home.todo.model.TodoModel

class ToDoNewAdapter: ListAdapter<TodoModel, ToDoNewAdapter.ToDoNewViewHolder>(differ) {

    inner class ToDoNewViewHolder(val binding: ItemTodoNewBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(todoItem: TodoModel) = with(binding) {
            tvTodoContent.text = todoItem.title
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ToDoNewViewHolder {
        return ToDoNewViewHolder(ItemTodoNewBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(
        holder: ToDoNewViewHolder,
        position: Int
    ) {
        return holder.bind(currentList[position])
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