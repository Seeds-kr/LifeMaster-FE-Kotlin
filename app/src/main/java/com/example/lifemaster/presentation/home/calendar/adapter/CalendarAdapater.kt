package com.example.lifemaster.presentation.home.calendar.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.lifemaster.R
import com.example.lifemaster.presentation.home.calendar.model.CalendarDay
import com.example.lifemaster.presentation.home.calendar.model.StarType

class CalendarAdapter(
    private val days: List<CalendarDay>,
    private val onClick: (CalendarDay) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {

    private var dayFeatures: Map<Int, List<StarType>> = emptyMap()
    fun setDayFeatures(map: Map<Int, List<StarType>>) {
        dayFeatures = map
        notifyDataSetChanged()
    }

    private var selectedDay: Int? = null
    fun setSelectedDay(day: Int?) {
        selectedDay = day
        notifyDataSetChanged()
    }

    inner class CalendarViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDay: TextView = view.findViewById(R.id.tv_day)
        val bgSelected: View = view.findViewById(R.id.bg_selected)
        val bgToday: View = view.findViewById(R.id.bg_today)

        val stars: Map<Int, ImageView> = mapOf(
            1 to view.findViewById(R.id.star1),
            2 to view.findViewById(R.id.star2),
            3 to view.findViewById(R.id.star3),
            4 to view.findViewById(R.id.star4),
            5 to view.findViewById(R.id.star5),
            6 to view.findViewById(R.id.star6),
            7 to view.findViewById(R.id.star7),
            8 to view.findViewById(R.id.star8),
            9 to view.findViewById(R.id.star9),
            10 to view.findViewById(R.id.star10),
            11 to view.findViewById(R.id.star11),
            12 to view.findViewById(R.id.star12),
        )
    }

    private val patternByCount: Map<Int, List<Int>> = mapOf(
        1 to listOf(7),
        2 to listOf(3, 11),
        3 to listOf(1, 5, 9),
        4 to listOf(1, 3, 7, 11),
        5 to listOf(1, 3, 6, 8, 11),
        6 to listOf(1, 2, 4, 7, 10, 12)
    )

    private val typePriority = listOf(
        StarType.ALARM,
        StarType.INTROSPECTION,
        StarType.TODO,
        StarType.CHALLENGE,
        StarType.DETOX,
        StarType.SLEEP
    )
    private val priorityIndex = typePriority.withIndex().associate { it.value to it.index }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_day, parent, false)
        return CalendarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        val item = days[position]
        holder.itemView.isClickable = true
        holder.tvDay.text = item.day.toString()

        holder.tvDay.setTextColor(
            Color.parseColor(
                if (item.isCurrentMonth) "#000000" else "#CCCCCC"
            )
        )

        holder.bgSelected.visibility = View.GONE
        holder.bgToday.visibility = View.GONE
        if (item.isCurrentMonth) {
            if (selectedDay != null && selectedDay == item.day) holder.bgSelected.visibility = View.VISIBLE
            if (item.isToday) holder.bgToday.visibility = View.VISIBLE
        }

        holder.stars.values.forEach {
            it.visibility = View.GONE
            it.imageTintList = null
        }

        if (item.isCurrentMonth) {
            val rawTypes = dayFeatures[item.day].orEmpty()
                .distinct()
                .sortedBy { priorityIndex[it] ?: Int.MAX_VALUE }

            val types = rawTypes.take(6)
            val positions = patternByCount[types.size]

            if (types.isNotEmpty() && positions != null) {
                val n = minOf(types.size, positions.size)
                for (i in 0 until n) {
                    val pos = positions[i]
                    val star = holder.stars[pos] ?: continue
                    star.visibility = View.VISIBLE
                    star.imageTintList = ColorStateList.valueOf(Color.parseColor(types[i].colorHex))
                }
            }
        }

        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount(): Int = days.size
}