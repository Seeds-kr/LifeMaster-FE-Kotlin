package com.example.lifemaster.presentation.home.calendar.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.lifemaster.databinding.FragmentCalendarBinding
import com.example.lifemaster.network.TokenProvider
import com.example.lifemaster.presentation.home.calendar.adapter.CalendarAdapter
import com.example.lifemaster.presentation.home.calendar.model.CalendarDay
import com.example.lifemaster.presentation.home.calendar.model.StarType
import com.example.lifemaster.presentation.home.calendar.model.CalendarRepository
import com.example.lifemaster.presentation.home.calendar.viewmodel.CalendarDataViewModel
import com.example.lifemaster.presentation.home.calendar.viewmodel.CalendarMode
import com.example.lifemaster.presentation.home.calendar.viewmodel.CalendarViewModel
import java.time.LocalDate
import java.util.Calendar
import java.util.GregorianCalendar

class CalendarFragment : Fragment() {

    companion object {
        const val ARG_TARGET_MEMBER_ID = "arg_target_member_id"
        const val ARG_CALENDAR_READ_ONLY = "arg_calendar_read_only"
    }

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!

    private val vm: CalendarViewModel by activityViewModels()

    private val dataVm: CalendarDataViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val repo = CalendarRepository(
                    authProvider = { TokenProvider.getAccessToken(requireContext()) }
                )
                @Suppress("UNCHECKED_CAST")
                return CalendarDataViewModel(repo) as T
            }
        }
    }

    private val targetMemberId: Long? by lazy {
        val v = arguments?.getLong(ARG_TARGET_MEMBER_ID, -1L) ?: -1L
        v.takeIf { it > 0L }
    }

    private val current: Calendar = GregorianCalendar().apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }

    private var monthAdapter: CalendarAdapter? = null
    private var weekAdapter: CalendarAdapter? = null
    private var weekDateMap: Map<Int, String> = emptyMap()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        savedInstanceState?.let {
            current.set(Calendar.YEAR, it.getInt("y", current.get(Calendar.YEAR)))
            current.set(Calendar.MONTH, it.getInt("m", current.get(Calendar.MONTH)))
        }

        // 월, 주, 일에 따라 화면 재구성
        vm.mode.observe(viewLifecycleOwner) { mode ->
            when (mode) {
                CalendarMode.MONTH -> showMonthView()
                CalendarMode.WEEK  -> showWeekView()
                CalendarMode.DAY   -> showDayView()
                null -> {}
            }
        }

        // 자아성찰 등 기능별 표시가 바뀌면 달력을 다시 그림
        vm.introspectionDates.observe(viewLifecycleOwner) {
            when (vm.mode.value ?: CalendarMode.MONTH) {
                CalendarMode.MONTH -> showMonthView()
                CalendarMode.WEEK  -> showWeekView()
                CalendarMode.DAY   -> showDayView()
            }
        }

        dataVm.monthEvents.observe(viewLifecycleOwner) { map ->
            when (vm.mode.value ?: CalendarMode.MONTH) {
                CalendarMode.MONTH -> {
                    val yyyy = getCurrentYear()
                    val mm = getCurrentMonth1()
                    val prefix = "%04d%02d".format(yyyy, mm)

                    val featuresByDay: Map<Int, List<StarType>> = map
                        .filterKeys { it.startsWith(prefix) }
                        .mapKeys { it.key.substring(6, 8).toInt() }
                        .mapValues { (_, events) -> events.mapNotNull(::classify).distinct() }

                    monthAdapter?.setDayFeatures(featuresByDay)
                }

                CalendarMode.WEEK -> {
                    if (weekDateMap.isNotEmpty()) {
                        val featuresByDay: Map<Int, List<StarType>> =
                            weekDateMap.mapValues { (_, yyyymmdd) ->
                                map[yyyymmdd].orEmpty().mapNotNull(::classify).distinct()
                            }
                        weekAdapter?.setDayFeatures(featuresByDay)
                    }
                }

                CalendarMode.DAY -> {}
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
    }

    fun getCurrentYear(): Int = current.get(Calendar.YEAR)
    fun getCurrentMonth1(): Int = current.get(Calendar.MONTH) + 1

    private fun showMonthView() = with(binding) {
        calendarContainer.visibility = View.VISIBLE
        monthRecyclerView.visibility = View.VISIBLE
        weekRecyclerView.visibility = View.GONE
        layoutWeekHeader.visibility = View.VISIBLE

        val days = generateMonthDays(current)
        monthRecyclerView.layoutManager = GridLayoutManager(requireContext(), 7)

        monthAdapter = CalendarAdapter(days) { day ->
            if (day.isCurrentMonth && day.day > 0) {
                val selected = LocalDate.of(
                    current.get(Calendar.YEAR),
                    current.get(Calendar.MONTH) + 1,
                    day.day
                )
                vm.selectDate(selected)
                monthAdapter?.setSelectedDay(day.day)
            }
        }
        monthRecyclerView.adapter = monthAdapter

        vm.selectedDate.value?.let { sel ->
            if (sel.year == current.get(Calendar.YEAR) && sel.monthValue == current.get(Calendar.MONTH) + 1) {
                monthAdapter?.setSelectedDay(sel.dayOfMonth)
            } else {
                monthAdapter?.setSelectedDay(null)
            }
        }

        dataVm.loadMonth(getCurrentYear(), getCurrentMonth1(), targetMemberId)
    }

    private fun showWeekView() = with(binding) {
        calendarContainer.visibility = View.VISIBLE
        monthRecyclerView.visibility = View.GONE
        weekRecyclerView.visibility = View.VISIBLE
        layoutWeekHeader.visibility = View.VISIBLE

        val anchor: LocalDate = vm.selectedDate.value ?: LocalDate.now()
        val days = generateWeekDays(anchor, current)

        weekRecyclerView.layoutManager = GridLayoutManager(requireContext(), 7)
        weekAdapter = CalendarAdapter(days) { day ->
            if (day.day > 0) {
                val yyyymmdd = weekDateMap[day.day] ?: return@CalendarAdapter
                val y = yyyymmdd.substring(0, 4).toInt()
                val m = yyyymmdd.substring(4, 6).toInt()
                val d = yyyymmdd.substring(6, 8).toInt()
                vm.selectDate(LocalDate.of(y, m, d))
                weekAdapter?.setSelectedDay(d)
            }
        }
        weekAdapter?.setSelectedDay(anchor.dayOfMonth)
        weekRecyclerView.adapter = weekAdapter

        val monthsToLoad: List<Pair<Int, Int>> =
            weekDateMap.values
                .map { it.substring(0, 6) }
                .distinct()
                .map { ym -> ym.substring(0, 4).toInt() to ym.substring(4, 6).toInt() }

        dataVm.loadMonths(monthsToLoad, targetMemberId)
    }

    private fun showDayView() = with(binding) {
        calendarContainer.visibility = View.GONE
        monthRecyclerView.visibility = View.GONE
        weekRecyclerView.visibility = View.GONE
        layoutWeekHeader.visibility = View.GONE
    }

    // 월 단위 달력 셀 생성 (이전/다음 달 포함)
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

    // 선택 날짜 기준 주간 셀 생성 + 실제 날짜 매핑
    private fun generateWeekDays(
        anchorDate: LocalDate,
        baseMonth: Calendar
    ): List<CalendarDay> {
        val startOfWeek = Calendar.SUNDAY
        val cal = GregorianCalendar().apply {
            set(Calendar.YEAR, anchorDate.year)
            set(Calendar.MONTH, anchorDate.monthValue - 1)
            set(Calendar.DAY_OF_MONTH, anchorDate.dayOfMonth)
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }

        val dow = cal.get(Calendar.DAY_OF_WEEK)
        val shift = ((dow - startOfWeek + 7) % 7)
        cal.add(Calendar.DAY_OF_MONTH, -shift)

        val today = GregorianCalendar().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }

        val result = mutableListOf<CalendarDay>()
        val mapForWeek = linkedMapOf<Int, String>()

        repeat(7) {
            val y = cal.get(Calendar.YEAR)
            val m = cal.get(Calendar.MONTH)
            val d = cal.get(Calendar.DAY_OF_MONTH)

            val isThisMonth = (y == baseMonth.get(Calendar.YEAR) && m == baseMonth.get(Calendar.MONTH))
            val isToday = (y == today.get(Calendar.YEAR) && m == today.get(Calendar.MONTH) && d == today.get(Calendar.DAY_OF_MONTH))

            mapForWeek[d] = "%04d%02d%02d".format(y, m + 1, d)
            result.add(CalendarDay(day = d, isCurrentMonth = isThisMonth, isToday = isToday))
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }

        weekDateMap = mapForWeek
        return result
    }

    private fun classify(text: String): StarType? {
        val t = text.uppercase()
        return when {
            t.contains("ALARM") || t.contains("알람") || t.contains("기상") -> StarType.ALARM
            t.contains("SLEEP") || t.contains("수면") -> StarType.SLEEP
            t.contains("DETOX") || t.contains("디톡스") -> StarType.DETOX
            t.contains("INTROSPECTION") || t.contains("자아성찰") || t.contains("성찰")
                    || t.contains("DIARY") || t.contains("일기") -> StarType.INTROSPECTION
            t.contains("CHALLENGE") || t.contains("챌린지") -> StarType.CHALLENGE
            t.contains("TODO") || t.contains("할일") || t.contains("할 일") -> StarType.TODO
            else -> null
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