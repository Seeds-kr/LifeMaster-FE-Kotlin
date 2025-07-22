package com.example.lifemaster.presentation.home.sleep

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.lifemaster.R
import com.example.lifemaster.databinding.FragmentSleepReportBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

class SleepReportFragment : Fragment(R.layout.fragment_sleep_report) {

    private lateinit var binding: FragmentSleepReportBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSleepReportBinding.bind(view)

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

        // 여러 개의 line 을 담는 전체 그래프 데이터
        val lineData = LineData(lineDataSet)
        binding.lineChartSleepReportGraph.data = lineData

        // 그래프 x축 설정
        binding.lineChartSleepReportGraph.xAxis.position = XAxis.XAxisPosition.BOTTOM // x축의 위치 지정
        binding.lineChartSleepReportGraph.xAxis.granularity = 1f // x축 값 사이의 최소
        binding.lineChartSleepReportGraph.xAxis.textColor = Color.parseColor("#C5C6C6") // x축 값 색상
        binding.lineChartSleepReportGraph.xAxis.textSize = 12f // x축 값 크기

        // 그래프 y축 설정
        binding.lineChartSleepReportGraph.axisLeft.textColor = Color.parseColor("#C5C6C6") // y축 값 색상
        binding.lineChartSleepReportGraph.axisLeft.textSize = 12f // y축 값 크기

        // 기타 설정
        binding.lineChartSleepReportGraph.axisRight.isEnabled = false // 오른쪽 y축값 표시 비활성화
        binding.lineChartSleepReportGraph.animateX(1000) // 선이 그려지는 애니메이션을 1초동안 실행
        binding.lineChartSleepReportGraph.legend.isEnabled = false // LineDataSet 에서 지정한 두번째 파라미터가 표시되지 않음
        binding.lineChartSleepReportGraph.description.isEnabled = false // 맨 오른쪽 하단에 표시되는 그래프 설명 비활성화
    }

}