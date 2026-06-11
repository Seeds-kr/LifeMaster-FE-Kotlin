package com.example.lifemaster.presentation.home.pomodoro.view

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.lifemaster.R
import com.example.lifemaster.presentation.home.alarm.model.DataResource
import com.example.lifemaster.presentation.home.pomodoro.viewmodel.PomodoroViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDate

@AndroidEntryPoint
class PomodoroReportFragment : Fragment(R.layout.fragment_pomodoro_report) {

    private val pomodoroViewModel: PomodoroViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val today = LocalDate.now().toString()

        setupFocusLevelListeners(view, today)

        pomodoroViewModel.getPomodoroStats(today)
        pomodoroViewModel.getPomodoroRecentFocus(today)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    pomodoroViewModel.pomodoroStats.collect { dataResource ->
                        when(dataResource) {
                            is DataResource.Success -> {
                                val stats = dataResource.data

                                view.findViewById<TextView>(R.id.tv_pomodoro_report_title).text =
                                    "오늘은\n총 ${formatMinutes(stats.todayTotalFocusMinutes)} 집중했어요"

                                view.findViewById<TextView>(R.id.tv_pomodoro_report_focus_time).text =
                                    formatMinutes(stats.todayTotalFocusMinutes)

                                view.findViewById<TextView>(R.id.tv_pomodoro_report_focus_time_diff).text =
                                    formatDiffMinutes(stats.focusMinutesDiff)

                                view.findViewById<TextView>(R.id.tv_pomodoro_report_complete_count).text =
                                    "${stats.completedCount}회"

                                view.findViewById<TextView>(R.id.tv_pomodoro_report_complete_count_diff).text =
                                    formatDiffCount(stats.completedCountDiff)

                                view.findViewById<TextView>(R.id.tv_pomodoro_report_weekly_focus_time).text =
                                    formatMinutes(stats.weeklyTotalFocusMinutes)

                                view.findViewById<TextView>(R.id.tv_pomodoro_report_average_focus_time).text =
                                    formatMinutes(stats.averageFocusMinutes)

                                view.findViewById<TextView>(R.id.tv_pomodoro_report_average_focus_time_diff).text =
                                    formatDiffMinutes(stats.averageFocusMinutesDiff)

                                view.findViewById<TextView>(R.id.tv_pomodoro_report_analysis_title).text =
                                    "평소보다"
                            }

                            is DataResource.Error -> {
                                Toast.makeText(
                                    context,
                                    getString(R.string.server_error_message),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            else -> {}
                        }
                    }
                }

                launch {
                    pomodoroViewModel.recentFocusItems.collect { dataResource ->
                        when (dataResource) {
                            is DataResource.Success -> {
                                val todayItem = dataResource.data.find { it.date == today }
                                when (todayItem?.focusLevel?.trim()?.uppercase()) {
                                    "LOW" -> selectFocusLevel(view.findViewById(R.id.tv_focus_level_low))
                                    "NORMAL" -> selectFocusLevel(view.findViewById(R.id.tv_focus_level_normal))
                                    "GOOD" -> selectFocusLevel(view.findViewById(R.id.tv_focus_level_good))
                                    "VERY_GOOD" -> selectFocusLevel(view.findViewById(R.id.tv_focus_level_very_good))
                                    else -> clearFocusLevelSelection()
                                }
                            }
                            is DataResource.Error -> {
                                clearFocusLevelSelection()
                            }
                            else -> {}
                        }
                    }
                }

                launch {
                    pomodoroViewModel.saveFocusLevelResult.collect { dataResource ->
                        when(dataResource) {
                            is DataResource.Success -> {
                                pomodoroViewModel.getPomodoroRecentFocus(today)
                            }

                            else -> {}
                        }
                    }
                }
            }
        }
    }

    private fun setupFocusLevelListeners(view: View, today: String) {
        val low = view.findViewById<TextView>(R.id.tv_focus_level_low)
        val normal = view.findViewById<TextView>(R.id.tv_focus_level_normal)
        val good = view.findViewById<TextView>(R.id.tv_focus_level_good)
        val veryGood = view.findViewById<TextView>(R.id.tv_focus_level_very_good)

        low.setOnClickListener {
            selectFocusLevel(low)
            pomodoroViewModel.savePomodoroFocusLevel(today, "LOW")
        }

        normal.setOnClickListener {
            selectFocusLevel(normal)
            pomodoroViewModel.savePomodoroFocusLevel(today, "NORMAL")
        }

        good.setOnClickListener {
            selectFocusLevel(good)
            pomodoroViewModel.savePomodoroFocusLevel(today, "GOOD")
        }

        veryGood.setOnClickListener {
            selectFocusLevel(veryGood)
            pomodoroViewModel.savePomodoroFocusLevel(today, "VERY_GOOD")
        }
    }

    private fun selectFocusLevel(selectedView: TextView) {
        clearFocusLevelSelection()

        selectedView.alpha = 1f
        selectedView.scaleX = 1.05f
        selectedView.scaleY = 1.05f
    }

    private fun formatMinutes(minutes: Int): String {
        val hour = minutes / 60
        val min = minutes % 60

        return when {
            hour > 0 && min > 0 -> "${hour}시간 ${min}분"
            hour > 0 -> "${hour}시간"
            else -> "${min}분"
        }
    }

    private fun formatDiffMinutes(minutes: Int): String {
        return if(minutes >= 0) "+${minutes}분" else "${minutes}분"
    }

    private fun formatDiffCount(count: Int): String {
        return if(count >= 0) "+${count}회" else "${count}회"
    }

    private fun clearFocusLevelSelection() {
        val low = requireView().findViewById<TextView>(R.id.tv_focus_level_low)
        val normal = requireView().findViewById<TextView>(R.id.tv_focus_level_normal)
        val good = requireView().findViewById<TextView>(R.id.tv_focus_level_good)
        val veryGood = requireView().findViewById<TextView>(R.id.tv_focus_level_very_good)

        listOf(low, normal, good, veryGood).forEach {
            it.alpha = 0.35f
            it.scaleX = 1f
            it.scaleY = 1f
        }
    }
}