package com.example.lifemaster_xml.total.pomodoro

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.lifemaster_xml.R

class PomodoroAdapter(
    val todoItems: MutableList<String>,
    val context: Context
) : RecyclerView.Adapter<PomodoroAdapter.PomodoroViewHolder>() {

    private var selectedPosition = RecyclerView.NO_POSITION

    inner class PomodoroViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val content: TextView
        val linearLayout: LinearLayoutCompat

        init {
            content = itemView.findViewById(R.id.tv_todo_content)
            linearLayout = itemView.findViewById(R.id.linearLayout)
            itemView.setOnClickListener {
                val previousPosition = selectedPosition
                selectedPosition = adapterPosition
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PomodoroViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.item_pomodoro, parent, false)
        return PomodoroViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PomodoroViewHolder, position: Int) {
        holder.content.text = todoItems[position]
        holder.linearLayout.isSelected = (position == selectedPosition)
    }

    override fun getItemCount(): Int {
        return todoItems.size
    }
}