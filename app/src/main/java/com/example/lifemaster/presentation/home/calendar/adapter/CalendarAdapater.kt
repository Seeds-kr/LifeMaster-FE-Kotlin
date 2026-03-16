package com.example.lifemaster.presentation.home.calendar.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.lifemaster.R
import com.example.lifemaster.presentation.home.calendar.model.CalendarDay

class CalendarAdapter(
    private val days: List<CalendarDay>,
    private val onClick: (CalendarDay) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {

    inner class CalendarViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.tv_day)
        val bgSelected: View = view.findViewById(R.id.bg_selected)
        val stars: List<ImageView> = listOf(
            view.findViewById(R.id.star1),
            view.findViewById(R.id.star2),
            view.findViewById(R.id.star3),
            view.findViewById(R.id.star4),
            view.findViewById(R.id.star5),
            view.findViewById(R.id.star6),
            view.findViewById(R.id.star7),
            view.findViewById(R.id.star8),
            view.findViewById(R.id.star9),
            view.findViewById(R.id.star10),
            view.findViewById(R.id.star11),
            view.findViewById(R.id.star12),
        )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_day, parent, false)
        return CalendarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        val item = days[position]

        holder.itemView.isClickable = true
        holder.textView.text = if (item.day > 0) item.day.toString() else ""

        // 기본 스타일
        when {
            !item.isCurrentMonth || item.day <= 0 -> {
                holder.textView.setTextColor(Color.parseColor("#CCCCCC"))
            }
            item.isToday -> {
                holder.textView.setTextColor(Color.parseColor("#000000"))
            }
            else -> {
                holder.textView.setTextColor(Color.parseColor("#000000"))
            }
        }

        // 선택 배경은 아직 ViewModel에서 별도 선택 상태를 관리하지 않으므로 숨김 유지
        holder.bgSelected.visibility = View.GONE

        // 기능 별(별 아이콘) 초기화
        holder.stars.forEach { star ->
            star.visibility = View.GONE
        }

        // features 리스트를 기반으로 별 표시 (최대 12개)
        item.features.take(holder.stars.size).forEachIndexed { index, feature ->
            val starView = holder.stars[index]
            starView.visibility = View.VISIBLE
            ImageViewCompat.setImageTintList(
                starView,
                ColorStateList.valueOf(Color.parseColor(feature.colorHex))
            )
        }

        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount(): Int = days.size
}
