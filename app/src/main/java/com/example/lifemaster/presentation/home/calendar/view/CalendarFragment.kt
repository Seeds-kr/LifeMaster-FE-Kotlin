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
import java.util.Calendar
import java.util.GregorianCalendar

class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!

    private val vm: CalendarViewModel by activityViewModels()

    private val current: Calendar = GregorianCalendar().apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }

    private var monthChanged: ((Int, Int) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        savedInstanceState?.let {
            current.set(Calendar.YEAR, it.getInt("y", current.get(Calendar.YEAR)))
            current.set(Calendar.MONTH, it.getInt("m", current.get(Calendar.MONTH)))
        }

        vm.mode.observe(viewLifecycleOwner) { mode ->
            when (mode) {
                CalendarMode.MONTH -> showMonthView()
                CalendarMode.WEEK  -> showWeekView()
                CalendarMode.DAY   -> showDayView()
                null -> {}
            }
        }

        when (vm.mode.value ?: CalendarMode.MONTH) {
            CalendarMode.MONTH -> showMonthView()
            CalendarMode.WEEK  -> showWeekView()
            CalendarMode.DAY   -> showDayView()
        }
    }

    fun moveMonth(delta: Int) {
        current.add(Calendar.MONTH, delta)
        when (vm.mode.value ?: CalendarMode.MONTH) {
            CalendarMode.MONTH -> showMonthView()
            CalendarMode.WEEK  -> showWeekView()
            CalendarMode.DAY   -> showDayView()
        }
        monthChanged?.invoke(getCurrentYear(), getCurrentMonth1())
    }

    fun getCurrentYear(): Int = current.get(Calendar.YEAR)
    fun getCurrentMonth1(): Int = current.get(Calendar.MONTH) + 1

    fun setOnMonthChangedListener(l: ((Int, Int) -> Unit)?) {
        monthChanged = l
    }

    private fun showMonthView() = with(binding) {
        calendarContainer.visibility = View.VISIBLE
        monthRecyclerView.visibility = View.VISIBLE
        weekRecyclerView.visibility = View.GONE
        layoutWeekHeader.visibility = View.VISIBLE

        val days = generateMonthDays(current)
        monthRecyclerView.layoutManager = GridLayoutManager(requireContext(), 7)
        monthRecyclerView.adapter = CalendarAdapter(days) { day ->
            if (day.isCurrentMonth && day.day > 0) {
                vm.selectDate(
                    LocalDate.of(
                        current.get(Calendar.YEAR),
                        current.get(Calendar.MONTH) + 1,
                        day.day
                    )
                )
                Toast.makeText(requireContext(), "${day.day}일 선택", Toast.LENGTH_SHORT).show()
            }
        }

        monthChanged?.invoke(getCurrentYear(), getCurrentMonth1())
    }

    private fun showWeekView() = with(binding) {
        calendarContainer.visibility = View.VISIBLE
        monthRecyclerView.visibility = View.GONE
        weekRecyclerView.visibility = View.VISIBLE
        layoutWeekHeader.visibility = View.VISIBLE

        val days = generateWeekDays(current)
        weekRecyclerView.layoutManager = GridLayoutManager(requireContext(), 7)
        weekRecyclerView.adapter = CalendarAdapter(days) { day ->
            if (day.day > 0) {
                vm.selectDate(
                    LocalDate.of(
                        current.get(Calendar.YEAR),
                        current.get(Calendar.MONTH) + 1,
                        day.day
                    )
                )
            }
        }

        monthChanged?.invoke(getCurrentYear(), getCurrentMonth1())
    }

    private fun showDayView() = with(binding) {
        calendarContainer.visibility = View.GONE
        monthRecyclerView.visibility = View.GONE
        weekRecyclerView.visibility = View.GONE
        layoutWeekHeader.visibility = View.GONE
        monthChanged?.invoke(getCurrentYear(), getCurrentMonth1())
    }

    private fun generateMonthDays(base: Calendar): List<CalendarDay> {
        val cal = (base.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) }

        val firstDayOfWeekIdx = (cal.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY + 7) % 7
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        val todayCal = GregorianCalendar()
        val isTodayInThisMonth =
            todayCal.get(Calendar.YEAR) == cal.get(Calendar.YEAR) &&
                    todayCal.get(Calendar.MONTH) == cal.get(Calendar.MONTH)

        val prevCal = (cal.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
        val prevMonthDays = prevCal.getActualMaximum(Calendar.DAY_OF_MONTH)

        val result = mutableListOf<CalendarDay>()
        for (i in (prevMonthDays - firstDayOfWeekIdx + 1)..prevMonthDays) {
            if (firstDayOfWeekIdx > 0) {
                result.add(CalendarDay(i, isCurrentMonth = false, isToday = false))
            }
        }

        for (day in 1..daysInMonth) {
            val isToday = isTodayInThisMonth && (day == todayCal.get(Calendar.DAY_OF_MONTH))
            result.add(CalendarDay(day, isCurrentMonth = true, isToday = isToday))
        }

        val totalCells = ((result.size + 6) / 7) * 7
        var nextDay = 1
        while (result.size < totalCells) {
            result.add(CalendarDay(nextDay++, isCurrentMonth = false, isToday = false))
        }

        return result
    }

    private fun generateWeekDays(base: Calendar): List<CalendarDay> {
        val weekCal = (base.clone() as Calendar)
        val firstDow = weekCal.firstDayOfWeek
        weekCal.set(Calendar.DAY_OF_WEEK, firstDow)

        val today = GregorianCalendar()
        val todayY = today.get(Calendar.YEAR)
        val todayM = today.get(Calendar.MONTH)
        val todayD = today.get(Calendar.DAY_OF_MONTH)

        return (0..6).map {
            val y = weekCal.get(Calendar.YEAR)
            val m = weekCal.get(Calendar.MONTH)
            val d = weekCal.get(Calendar.DAY_OF_MONTH)
            val isThisMonth = (y == base.get(Calendar.YEAR) && m == base.get(Calendar.MONTH))
            val isToday = (y == todayY && m == todayM && d == todayD)
            val item = CalendarDay(d, isCurrentMonth = isThisMonth, isToday = isToday)
            weekCal.add(Calendar.DAY_OF_MONTH, 1)
            item
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("y", current.get(Calendar.YEAR))
        outState.putInt("m", current.get(Calendar.MONTH))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}