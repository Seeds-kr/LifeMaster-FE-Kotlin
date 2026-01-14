package com.example.lifemaster.presentation.home.todo.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.lifemaster.databinding.ItemSelectTodoBinding
import com.example.lifemaster.presentation.home.todo.model.TodoModel

class ToDoSelectAdapter(private val currentItem: TodoModel): ListAdapter<TodoModel, ToDoSelectAdapter.ToDoSelectViewHolder>(differ) {

    private var selectedPosition = -1 // 선택 전에는 -1

    fun getSelectedItem(): TodoModel? {
        return if(selectedPosition != -1) getItem(selectedPosition) else null
    }

    // ListAdapter에서 데이터가 성공적으로 로드되었을 때 호출되는 콜백
    override fun onCurrentListChanged(
        previousList: List<TodoModel?>,
        currentList: List<TodoModel?>
    ) {
        super.onCurrentListChanged(previousList, currentList)
        val index = currentList.indexOf(currentItem)
        selectedPosition = index
        notifyItemChanged(selectedPosition)
    }

    inner class ToDoSelectViewHolder(private val binding: ItemSelectTodoBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(todoItem: TodoModel, position: Int) = with(binding) {
            tvTodoContent.text = todoItem.title
            root.isSelected = (position == selectedPosition)
            ivItemSelectTodoTimer25.isVisible = todoItem.timer25Number != 0
            tvItemSelectTodoTimer25Count.isVisible = todoItem.timer25Number != 0
            tvItemSelectTodoTimer25Count.text = todoItem.timer25Number.toString()
            ivItemSelectTodoTimer50.isVisible = todoItem.timer50Number != 0
            tvItemSelectTodoTimer50Count.isVisible = todoItem.timer50Number != 0
            tvItemSelectTodoTimer50Count.text = todoItem.timer50Number.toString()
            root.setOnClickListener {
                val oldPosition = selectedPosition
                selectedPosition = adapterPosition
                notifyItemChanged(oldPosition)
                notifyItemChanged(selectedPosition)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ToDoSelectViewHolder {
        return ToDoSelectViewHolder(ItemSelectTodoBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(
        holder: ToDoSelectViewHolder,
        position: Int
    ) {
        return holder.bind(getItem(position), position)
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