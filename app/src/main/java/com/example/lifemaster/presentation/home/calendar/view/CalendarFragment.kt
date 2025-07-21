package com.example.lifemaster.presentation.home.calendar.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.lifemaster.R
import com.example.lifemaster.databinding.FragmentCalendarBinding
import com.example.lifemaster.presentation.home.calendar.adapter.CalendarAdapter
import com.example.lifemaster.presentation.home.calendar.model.CalendarDay
import java.util.*

class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        showMonthView()
        updateButtonUI(binding.btnMonth)
        updateSelectedDate("month")
    }

    private fun setupListeners() {
        binding.btnMonth.setOnClickListener {
            showMonthView()
            updateButtonUI(binding.btnMonth)
            updateSelectedDate("month")
        }
        binding.btnWeek.setOnClickListener {
            showWeekView()
            updateButtonUI(binding.btnWeek)
            updateSelectedDate("week")
        }
        binding.btnDay.setOnClickListener {
            showDayView()
            updateButtonUI(binding.btnDay)
            updateSelectedDate("day")
        }
    }

    private fun updateButtonUI(selectedButton: View) {
        val buttons = listOf(binding.btnMonth, binding.btnWeek, binding.btnDay)
        buttons.forEach { btn ->
            if (btn == selectedButton) {
                btn.setBackgroundResource(R.drawable.bg_round_and_mint)
                (btn as? androidx.appcompat.widget.AppCompatButton)
                    ?.setTextColor(resources.getColor(android.R.color.white, null))
            } else {
                btn.setBackgroundResource(R.drawable.bg_calendar_unselected)
                (btn as? androidx.appcompat.widget.AppCompatButton)
                    ?.setTextColor(resources.getColor(R.color.mint_60, null))
            }
        }
    }

    private fun updateSelectedDate(type: String) {
        val cal = Calendar.getInstance()
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val week = cal.get(Calendar.WEEK_OF_MONTH)
        binding.tvSelectedDate.text = when (type) {
            "month" -> "${month}월"
            "week" -> "${month}월 ${week}째주"
            "day" -> "${month}월 ${day}일"
            else -> "${month}월"
        }
    }

    private fun showMonthView() {
        binding.calendarContainer.visibility = View.VISIBLE
        binding.monthRecyclerView.visibility = View.VISIBLE
        binding.weekRecyclerView.visibility = View.GONE
        binding.layoutWeekHeader.visibility = View.VISIBLE

        val days = generateMonthDays()
        binding.monthRecyclerView.layoutManager = GridLayoutManager(requireContext(), 7)
        binding.monthRecyclerView.adapter = CalendarAdapter(days) {
            Toast.makeText(requireContext(), "${it.day}일 선택", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showWeekView() {
        binding.calendarContainer.visibility = View.VISIBLE
        binding.monthRecyclerView.visibility = View.GONE
        binding.weekRecyclerView.visibility = View.VISIBLE
        binding.layoutWeekHeader.visibility = View.VISIBLE

        val days = generateWeekDays()
        binding.weekRecyclerView.layoutManager = GridLayoutManager(requireContext(), 7)
        binding.weekRecyclerView.adapter = CalendarAdapter(days) {
            Toast.makeText(requireContext(), "${it.day}일 선택", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDayView() {
        binding.calendarContainer.visibility = View.GONE
        binding.monthRecyclerView.visibility = View.GONE
        binding.weekRecyclerView.visibility = View.GONE
        binding.layoutWeekHeader.visibility = View.GONE
    }

    private fun generateMonthDays(): List<CalendarDay> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)

        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val today = Calendar.getInstance()

        val result = mutableListOf<CalendarDay>()

        val prevCal = Calendar.getInstance()
        prevCal.time = cal.time
        prevCal.add(Calendar.MONTH, -1)
        val prevMonthDays = prevCal.getActualMaximum(Calendar.DAY_OF_MONTH)

        val emptyDays = (firstDayOfWeek - Calendar.SUNDAY + 7) % 7
        for (i in (prevMonthDays - emptyDays + 1)..prevMonthDays) {
            result.add(CalendarDay(i, isCurrentMonth = false))
        }

        for (day in 1..daysInMonth) {
            result.add(
                CalendarDay(
                    day,
                    isCurrentMonth = true,
                    isToday = (day == today.get(Calendar.DAY_OF_MONTH)
                            && cal.get(Calendar.MONTH) == today.get(Calendar.MONTH)
                            && cal.get(Calendar.YEAR) == today.get(Calendar.YEAR))
                )
            )
        }

        val totalCells = ((result.size + 6) / 7) * 7
        var nextDay = 1
        while (result.size < totalCells) {
            result.add(CalendarDay(nextDay++, isCurrentMonth = false))
        }

        return result
    }

    private fun generateWeekDays(): List<CalendarDay> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        val today = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        return (0..6).map {
            val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
            cal.add(Calendar.DAY_OF_MONTH, 1)
            CalendarDay(dayOfMonth, true, dayOfMonth == today)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}