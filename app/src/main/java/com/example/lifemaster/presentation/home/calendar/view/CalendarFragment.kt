package com.example.lifemaster.presentation.home.calendar.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.lifemaster.databinding.FragmentCalendarBinding
import com.example.lifemaster.presentation.home.calendar.adapter.CalendarAdapter
import com.example.lifemaster.presentation.home.calendar.model.CalendarDay
import com.example.lifemaster.presentation.home.calendar.viewmodel.CalendarMode
import com.example.lifemaster.presentation.home.calendar.viewmodel.CalendarViewModel
import java.time.LocalDate
import java.util.*

class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!

    private val vm: CalendarViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.mode.observe(viewLifecycleOwner) { mode ->
            when (mode) {
                CalendarMode.MONTH -> showMonthView()
                CalendarMode.WEEK  -> showWeekView()
                CalendarMode.DAY   -> showDayView()
            }
        }
    }

    private fun showMonthView() = with(binding) {
        calendarContainer.visibility = View.VISIBLE
        monthRecyclerView.visibility = View.VISIBLE
        weekRecyclerView.visibility = View.GONE
        layoutWeekHeader.visibility = View.VISIBLE

        val days = generateMonthDays()
        monthRecyclerView.layoutManager = GridLayoutManager(requireContext(), 7)
        monthRecyclerView.adapter = CalendarAdapter(days) { day ->
            if (day.isCurrentMonth && day.day > 0) {
                val cal = Calendar.getInstance()
                val year = cal.get(Calendar.YEAR)
                val monthZeroBased = cal.get(Calendar.MONTH)
                vm.selectDate(LocalDate.of(year, monthZeroBased + 1, day.day))
                Toast.makeText(requireContext(), "${day.day}일 선택", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showWeekView() = with(binding) {
        calendarContainer.visibility = View.VISIBLE
        monthRecyclerView.visibility = View.GONE
        weekRecyclerView.visibility = View.VISIBLE
        layoutWeekHeader.visibility = View.VISIBLE

        val days = generateWeekDays()
        weekRecyclerView.layoutManager = GridLayoutManager(requireContext(), 7)
        weekRecyclerView.adapter = CalendarAdapter(days) { day ->
            if (day.day > 0) {
                val today = Calendar.getInstance()
                vm.selectDate(LocalDate.of(today.get(Calendar.YEAR), today.get(Calendar.MONTH) + 1, day.day))
            }
        }
    }

    private fun showDayView() = with(binding) {
        calendarContainer.visibility = View.GONE
        monthRecyclerView.visibility = View.GONE
        weekRecyclerView.visibility = View.GONE
        layoutWeekHeader.visibility = View.GONE
    }

    private fun generateMonthDays(): List<CalendarDay> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)

        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val today = Calendar.getInstance()

        val result = mutableListOf<CalendarDay>()

        val prevCal = Calendar.getInstance().apply {
            time = cal.time
            add(Calendar.MONTH, -1)
        }
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
        val cal = Calendar.getInstance().apply { set(Calendar.DAY_OF_WEEK, firstDayOfWeek) }
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