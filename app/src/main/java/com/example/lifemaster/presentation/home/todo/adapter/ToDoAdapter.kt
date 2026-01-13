package com.example.lifemaster.presentation.home.todo.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.daimajia.swipe.SwipeLayout
import com.example.lifemaster.databinding.ItemTodoBinding
import com.example.lifemaster.presentation.home.todo.model.TodoModel

class ToDoAdapter (
    private val context: Context,
    private val onEditClicked: (TodoModel) -> Unit,
    private val onDeleteClicked: (Int) -> Unit,
    private val onToggleClicked: (Int) -> Unit,
    private val onViewClicked: (TodoModel) -> Unit
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
            cbTodoToggle.isChecked = item.isCompleted
            root.showMode = SwipeLayout.ShowMode.PullOut
            ivItemTodoTimer25.isVisible = (item.timer25Number != 0)
            tvItemTodoTimer25Count.isVisible = (item.timer25Number != 0)
            ivItemTodoTimer50.isVisible = (item.timer50Number != 0)
            tvItemTodoTimer50Count.isVisible = (item.timer50Number != 0)
            tvItemTodoTimer25Count.text = item.timer25Number.toString()
            tvItemTodoTimer50Count.text = item.timer50Number.toString()
        }

        private fun bindEvents(item: TodoModel) = with(binding) {
            cbTodoToggle.setOnCheckedChangeListener { _, _ ->
                onToggleClicked(item.id)
            }
            flTodoEdit.setOnClickListener {
                onEditClicked(item)
            }
            flTodoDelete.setOnClickListener {
                onDeleteClicked(item.id)
            }
            root.setOnClickListener {
                onViewClicked(item)
            }
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