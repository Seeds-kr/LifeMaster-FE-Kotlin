package com.example.lifemaster.presentation.home.sleep

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import com.example.lifemaster.R
import com.example.lifemaster.databinding.FragmentSleepReportBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import java.util.Calendar
import java.util.Date

class SleepReportFragment : Fragment(R.layout.fragment_sleep_report) {

    private lateinit var binding: FragmentSleepReportBinding
    private var lastUsedApp: String? = null // 자기전 마지막으로 사용한 앱
    private var firstUsedApp: String? = null // 기상후 처음으로 사용한 앱
    private var lastUsageTimeBeforeSleep: Long = 0L // 마지막 사용 시간 = 핸드폰 화면을 끈 시간
    private var firstUsageTimeAfterWake: Long? = null // 핸드폰을 처음 킨 시간 (잠금 해제x)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSleepReportBinding.bind(view)
        initViews()
        initListeners()
    }

    private fun initListeners() = with(binding) {
        val todayMoods = listOf(
            ivSleepReportTodayMoodVeryBad,
            ivSleepReportTodayMoodBad,
            ivSleepReportTodayMoodGood,
            ivSleepReportTodayMoodVeryGood
        )
        for (todayMood in todayMoods) {
            todayMood.setOnClickListener {
                todayMoods.forEach { it.clearColorFilter() }
                todayMood.setColorFilter(resources.getColor(R.color.sleep_selected_mood, context?.theme))
            }
        }
    }

    private fun initViews() = with(binding) {
        // 1개의 line 을 구성하는 점들의 집합
        val dataPoints = listOf(
            Entry(10f, 10f),
            Entry(11f, 6f),
            Entry(12f, 7f),
            Entry(13f, 9f),
            Entry(14f, 8.5f),
            Entry(15f, 5f)
        )

        // line 1개
        val lineDataSet = LineDataSet(dataPoints, "수면 꺾은선 그래프")
        lineDataSet.color = Color.parseColor("#BBAB94") // 선 색상
        lineDataSet.lineWidth = 3f // 선 굵기
        lineDataSet.setCircleColor(Color.parseColor("#927448")) // 점 색상
        lineDataSet.circleRadius = 4f // 점 크기
        lineDataSet.setDrawValues(false) // 값이 안보이게 하기
//        lineDataSet.valueTextColor = Color.parseColor("#927448") // 값 색상
//        lineDataSet.setDrawFilled(true) // 선 아래 영역 채우기

        // 여러 개의 line 을 담는 전체 그래프 데이터
        val lineData = LineData(lineDataSet)
        lineChartSleepReportGraph.data = lineData

        // 그래프 x축 설정
        lineChartSleepReportGraph.xAxis.position = XAxis.XAxisPosition.BOTTOM // x축의 위치 지정
        lineChartSleepReportGraph.xAxis.granularity = 1f // x축 값 사이의 최소
        lineChartSleepReportGraph.xAxis.textColor = Color.parseColor("#C5C6C6") // x축 값 색상
        lineChartSleepReportGraph.xAxis.textSize = 12f // x축 값 크기

        // 그래프 y축 설정
        lineChartSleepReportGraph.axisLeft.textColor = Color.parseColor("#C5C6C6") // y축 값 색상
        lineChartSleepReportGraph.axisLeft.textSize = 12f // y축 값 크기

        // 기타 설정
        lineChartSleepReportGraph.axisRight.isEnabled = false // 오른쪽 y축값 표시 비활성화
//      lineChartSleepReportGraph.animateX(1000) // 선이 그려지는 애니메이션을 1초동안 실행
        lineChartSleepReportGraph.legend.isEnabled = false // LineDataSet 에서 지정한 두번째 파라미터가 표시되지 않음
        lineChartSleepReportGraph.description.isEnabled = false // 맨 오른쪽 하단에 표시되는 그래프 설명 비활성화
//      lineChartSleepReportGraph.description.text = "수면 점수 그래프" // 맨 오른쪽 하단에 표시되는 그래프 설명

        // 점 클릭 시 나타나는 통계 세부 정보
        val markerView = SleepReportMarkerView(requireContext(), R.layout.layout_sleep_report_marker_view)
        lineChartSleepReportGraph.marker = markerView

        // 사용자가 잠든 시간 추적하기
        val usageStatsManager = requireContext().getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val sleepCalendar = Calendar.getInstance().apply {
            // 오늘 날짜
            set(Calendar.HOUR_OF_DAY, 4)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val sleepTrackingEndTime = sleepCalendar.timeInMillis // 새벽 4시
        sleepCalendar.add(Calendar.HOUR_OF_DAY, -8)
        val sleepTrackingStartTime = sleepCalendar.timeInMillis // 오후 8시

        val sleepEvent = UsageEvents.Event()

        val sleepUsageEvents = usageStatsManager.queryEvents(sleepTrackingStartTime, sleepTrackingEndTime) // 전날 오후 8시 ~ 오늘 새벽 4시까지의 핸드폰 이용 내역 조회
        while (sleepUsageEvents.hasNextEvent()) {
            // while 문을 통해 해당 시간대의 마지막 핸드폰 사용 시간 추적 + 업데이트
            sleepUsageEvents.getNextEvent(sleepEvent) // 다음 이벤트를 변수에 저장
            if(sleepEvent.eventType == UsageEvents.Event.ACTIVITY_STOPPED && sleepEvent.packageName != "com.sec.android.app.launcher") lastUsedApp = sleepEvent.packageName // 전날 오후 8시 ~ 오늘 새벽 4시 중 마지막으로 사용한 앱 추적
            else if(sleepEvent.eventType == UsageEvents.Event.SCREEN_NON_INTERACTIVE) lastUsageTimeBeforeSleep = sleepEvent.timeStamp // 전날 오후 8시 ~ 오늘 새벽 4시 중 마지막으로 화면을 끈 시각 추적
        }

        Log.e("NIGHT", "마지막 사용 앱: $lastUsedApp, 화면 끈 시각: ${Date(lastUsageTimeBeforeSleep)}")

        // 사용자가 일어난 시간 추적하기 (알람 연동x)
        val wakeUpCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 5)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val wakeTrackingStartTime = wakeUpCalendar.timeInMillis
        wakeUpCalendar.add(Calendar.HOUR_OF_DAY, 5)
        val wakeTrackingEndTime = wakeUpCalendar.timeInMillis

        val wakeEvent = UsageEvents.Event()
        val wakeUsageEvents = usageStatsManager.queryEvents(wakeTrackingStartTime, wakeTrackingEndTime) // 금일 오전 5시 ~ 금일 오전 10시 사이의 이벤트 조회

        while(wakeUsageEvents.hasNextEvent()) {
            wakeUsageEvents.getNextEvent(wakeEvent)
            if(wakeEvent.eventType == UsageEvents.Event.SCREEN_INTERACTIVE) {
                if(firstUsageTimeAfterWake == null) {
                    firstUsageTimeAfterWake = wakeEvent.timeStamp  // 기상 후 처음 핸드폰을 킨 시간 추적
                    Log.e("MORNING", "SCREEN_INTERACTIVE: ${Date(firstUsageTimeAfterWake ?: 0L)}")
                }
            }
            else if(wakeEvent.eventType == UsageEvents.Event.ACTIVITY_RESUMED && wakeEvent.packageName != "com.sec.android.app.launcher") {
                firstUsedApp = wakeEvent.packageName // 기상 후 처음 사용한 앱 추적
                Log.e("MORNING", "ACTIVITY_RESUMED: $firstUsedApp")
                break // 더 이상 이벤트를 추적할 필요 없음
            }
        }
    }

}