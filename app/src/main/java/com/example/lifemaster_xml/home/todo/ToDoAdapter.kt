package com.example.lifemaster_xml.home.todo

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.lifemaster_xml.R

class ToDoAdapter(
    val todoItems: MutableList<String>,
    val context: Context
) : RecyclerView.Adapter<ToDoAdapter.ToDoViewHolder>() {
    inner class ToDoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val content: TextView
        init {
            content = itemView.findViewById(R.id.tv_todo_item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToDoViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.item_todo, parent, false) // [!] 무조건 false 를 전달해야한다.
        return ToDoViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ToDoViewHolder, position: Int) {
        holder.content.text = todoItems[position]
    }

    override fun getItemCount(): Int {
        return todoItems.size
    }
}