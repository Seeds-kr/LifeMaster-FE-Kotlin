package com.example.lifemaster.presentation.home.calendar.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.lifemaster.R
import com.example.lifemaster.presentation.home.calendar.model.CalendarDay

class CalendarAdapter(
    private val days: List<CalendarDay>,
    private val onClick: (CalendarDay) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {

    inner class CalendarViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.tv_day)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_day, parent, false)
        return CalendarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        val item = days[position]

        if (item.day == 0) {
            holder.textView.text = ""
            holder.textView.setBackgroundColor(Color.TRANSPARENT)
            return
        }

        holder.textView.text = item.day.toString()

        when {
            !item.isCurrentMonth -> {
                holder.textView.setTextColor(Color.parseColor("#CCCCCC"))
                holder.textView.background = null
            }
            else -> {
                holder.textView.setTextColor(Color.parseColor("#000000"))
                holder.textView.background = null
            }
        }

        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount(): Int = days.size
}