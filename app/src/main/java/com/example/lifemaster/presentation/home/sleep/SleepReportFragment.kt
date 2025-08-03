package com.example.lifemaster.presentation.home.sleep

import android.content.Context.MODE_PRIVATE
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.lifemaster.R
import com.example.lifemaster.databinding.FragmentSleepReportBinding
import com.example.lifemaster.presentation.home.sleep.viewmodel.SleepViewModel
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.text.SimpleDateFormat
import java.util.Locale

class SleepReportFragment : Fragment(R.layout.fragment_sleep_report) {

    private lateinit var binding: FragmentSleepReportBinding
    private val sleepViewModel: SleepViewModel by activityViewModels()
    private var userSleepDataPoints = mutableListOf<Entry>() // // 1개의 line 을 구성하는 점들의 집합
    private var xLabels = mutableListOf<String>() // x축에 표시할 값(일)
    private var yValues = mutableListOf<Float>() // y축에 표시할 값

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSleepReportBinding.bind(view)
        initViews()
        initListeners()
    }

    private fun initViews() = with(binding) {
        // 수면 타이틀 UI
        tvSleepReportTitle.text = "오늘은\n총 ${sleepViewModel.sleepDurationHour}시간 ${sleepViewModel.sleepDurationMinutes}분 잤어요"

        // 통계 UI
        val sharedPreference = requireContext().getSharedPreferences("user_sleep_info", MODE_PRIVATE)
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val regex = Regex("(\\d+)시간 (\\d+)분") // 정규 표현식
        val orderedSleepData = sharedPreference.all.map { Pair(it.key, it.value as String) }.sortedBy { dateFormatter.parse(it.first) }
        orderedSleepData.forEach {
            val date = it.first
            val dayOfMonth = date.split("-")[2]
            xLabels.add(dayOfMonth)

            val sleepDuration = it.second
            val matchResult = regex.find(sleepDuration) ?: return@forEach
            val (hour, minutes) = matchResult.destructured
            val tempYValue = "${hour}.${minutes}".toFloat()
            yValues.add(tempYValue)
        }

        Log.e("CHECK", "" + xLabels)
        Log.e("CHECK2", "" + yValues)

        yValues.forEachIndexed { index, value ->
            userSleepDataPoints.add(
                Entry(index.toFloat(), value)
            )
        }

        // line 1개
        val lineDataSet = LineDataSet(userSleepDataPoints, "수면 꺾은선 그래프")
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
        lineChartSleepReportGraph.xAxis.valueFormatter = IndexAxisValueFormatter(xLabels) // x축 레이블 표시
        lineChartSleepReportGraph.xAxis.granularity = 1f // x축 레이블이 표시될 최소 간격 단위
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
        lineChartSleepReportGraph.isDoubleTapToZoomEnabled = false // 더블 탭하여 확대되는 기능 비활성화
        lineChartSleepReportGraph.setScaleEnabled(false) // 그래프 확대 기능 비활성화

        // 통계 점 클릭 시 나타나는 통계 세부 정보
        val dailySleepDurations = mutableListOf<String>()
        orderedSleepData.forEach {
            dailySleepDurations.add(it.second)
        }
        val markerView = SleepReportMarkerView(
            requireContext(),
            R.layout.layout_sleep_report_marker_view,
            dailySleepDurations
        )
        lineChartSleepReportGraph.marker = markerView

        // 평소 수면 정보 비교 UI
        // step1. 금일 수면 정보 추출하기
        val todaySleepData = orderedSleepData.last() // 금일 수면 정보 ex. (2025-08-03, 7시간 45분)
        val result = regex.find(todaySleepData.second) ?: return@with
        val (hour, minutes) = result.destructured
        val todaySleepMinutes = hour.toInt()*60+minutes.toInt()
        tvSleepReportAnalysisSleepTimeValue.text = "${todaySleepMinutes}분" // UI 반영

        // step2. 금일 제외 수면 정보 추출 및 누적 합산하기
        var accumulatedPastSleepMinutes = 0 // 금일 제외 총합 수면 시간 (단위: 분)
        val pastSleepData = orderedSleepData.subList(0, orderedSleepData.size-1) // 금일 데이터 제외
        pastSleepData.forEach {
            val sleepTime = regex.find(it.second) ?: return@forEach
            val (hour, minutes) = sleepTime.destructured
            val totalSleepMinutes = hour.toInt()*60+minutes.toInt()
            accumulatedPastSleepMinutes += totalSleepMinutes
        }
        val averagePastSleepMinutes = accumulatedPastSleepMinutes/pastSleepData.size
        val sleepDifference = kotlin.math.abs(todaySleepMinutes-averagePastSleepMinutes) // 절댓값 계산
        tvSleepReportAnalysisSleepTimeGapValue.text = sleepDifference.toString() // UI 반영

        // step3. 금일 정보와 과거 정보 비교하기
        if(todaySleepMinutes > averagePastSleepMinutes) {
            tvSleepReportAnalysisSleepTimeTitle.text = "더 잤어요"
            ivSleepReportAnalysisSleepTimeChangeIndicator.setImageDrawable(AppCompatResources.getDrawable(requireContext(), R.drawable.ic_arrow_up))
        } else if(todaySleepMinutes == averagePastSleepMinutes) {
            tvSleepReportAnalysisSleepTimeTitle.text = "평소만큼 잤어요"
            ivSleepReportAnalysisSleepTimeChangeIndicator.setImageDrawable(AppCompatResources.getDrawable(requireContext(), R.drawable.ic_arrow_up)) // TODO: PM한테 물어보고 아이콘 변경하기
        } else {
            tvSleepReportAnalysisSleepTimeTitle.text = "잠이 부족했어요"
            ivSleepReportAnalysisSleepTimeChangeIndicator.setImageDrawable(AppCompatResources.getDrawable(requireContext(), R.drawable.ic_arrow_up))
            ivSleepReportAnalysisSleepTimeChangeIndicator.rotation = 180f // 180도 회전하여 기존 drawable 재활용
        }
    }

    private fun initListeners() = with(binding) {
        // 오늘의 기분 바꾸기
        val todayMoods = listOf(
            ivSleepReportTodayMoodVeryBad,
            ivSleepReportTodayMoodBad,
            ivSleepReportTodayMoodGood,
            ivSleepReportTodayMoodVeryGood
        )
        for (todayMood in todayMoods) {
            todayMood.setOnClickListener {
                todayMoods.forEach { it.clearColorFilter() }
                todayMood.setColorFilter(
                    resources.getColor(
                        R.color.sleep_selected_mood,
                        context?.theme
                    )
                )
            }
        }
    }

}