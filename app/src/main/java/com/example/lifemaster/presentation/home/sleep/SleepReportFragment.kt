package com.example.lifemaster.presentation.home.sleep

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.lifemaster.R
import com.example.lifemaster.databinding.FragmentSleepReportBinding
import com.example.lifemaster.network.RetrofitInstance
import com.example.lifemaster.presentation.home.sleep.model.Result
import com.example.lifemaster.presentation.home.sleep.model.SleepResponse
import com.example.lifemaster.presentation.home.sleep.viewmodel.SleepViewModel
import com.example.lifemaster.presentation.home.sleep.viewmodel.SleepViewModelFactory
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.util.Locale

class SleepReportFragment : Fragment(R.layout.fragment_sleep_report) {

    private lateinit var binding: FragmentSleepReportBinding
    private val sleepViewModel: SleepViewModel by activityViewModels {
        SleepViewModelFactory(RetrofitInstance.networkService)
    }

    private var userSleepDataPoints = mutableListOf<Entry>() // 1개의 line 을 구성하는 점들의 집합
    private var userMoodDataPoints = mutableListOf<Pair<Float, Drawable?>>()

    private var xLabels = mutableListOf<String>() // x축에 표시할 값(일)
    private var yValues = mutableListOf<Float>() // y축에 표시할 값

    private lateinit var userMoodPrefs: SharedPreferences
    private lateinit var userAlarmPrefs: SharedPreferences
    private lateinit var userSleepPrefs: SharedPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSleepReportBinding.bind(view)
        fetchRemoteData()
        initObservers()
        initListeners()
    }

    private fun fetchRemoteData() {
        sleepViewModel.getUserSleepInfo(userId = 13)
    }

    private fun initObservers() = with(binding) {

        // 유저의 수면 기록 조회
        sleepViewModel.userSleepRecordList.observe(viewLifecycleOwner) { result ->
            when(result) {
                is Result.Loading -> {
                    initLoadingUI()
                }
                is Result.Success -> {
                    initRemoteUI(result.data)
                }
                is Result.Error -> {
                    Toast.makeText(context, "서버 에러: ${result.throwable}", Toast.LENGTH_SHORT).show()
                    initLocalUI()
                }
            }
        }
    }

    private fun initLoadingUI() = with(binding) {
        // TODO: 로딩 UI 만들기
    }

    private fun initRemoteUI(remoteUserSleepRecordList: List<SleepResponse>) = with(binding) {

    }

    private fun initLocalUI() = with(binding) {
        // shared preference 초기화
        userMoodPrefs = requireContext().getSharedPreferences("user_mood_info", MODE_PRIVATE)
        if (!userMoodPrefs.contains(LocalDate.now().toString())) {
            userMoodPrefs.edit().putString(LocalDate.now().toString(), "null").apply()
        }

        userAlarmPrefs = requireContext().getSharedPreferences("user_alarm_info", MODE_PRIVATE)
        if (!userAlarmPrefs.contains(LocalDate.now().toString())) {
            userAlarmPrefs.edit().putString(LocalDate.now().toString(), "null").apply()
        }

        userSleepPrefs = requireContext().getSharedPreferences("user_sleep_info", MODE_PRIVATE)

        // 수면 타이틀 UI
        tvSleepReportTitle.text =
            "오늘은\n총 ${sleepViewModel.sleepDurationHour}시간 ${sleepViewModel.sleepDurationMinutes}분 잤어요"

        // 오늘의 기분 UI 업데이트
        val todayMood = userMoodPrefs.getString(LocalDate.now().toString(), "null")
        when (todayMood) {
            "very_bad" -> {
                ivSleepReportTodayMoodVeryBad.setColorFilter(
                    resources.getColor(
                        R.color.sleep_selected_mood,
                        context?.theme
                    )
                )
            }

            "bad" -> {
                ivSleepReportTodayMoodBad.setColorFilter(
                    resources.getColor(
                        R.color.sleep_selected_mood,
                        context?.theme
                    )
                )
            }

            "good" -> {
                ivSleepReportTodayMoodGood.setColorFilter(
                    resources.getColor(
                        R.color.sleep_selected_mood,
                        context?.theme
                    )
                )
            }

            "very_good" -> {
                ivSleepReportTodayMoodVeryGood.setColorFilter(
                    resources.getColor(
                        R.color.sleep_selected_mood,
                        context?.theme
                    )
                )
            }
        }

        // 통계 UI
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val regex = Regex("(\\d+)시간 (\\d+)분") // 정규 표현식

        val orderedSleepData = userSleepPrefs.all.map { Pair(it.key, it.value as String) }
            .sortedBy { dateFormatter.parse(it.first) }
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
        lineChartSleepReportGraph.xAxis.valueFormatter =
            IndexAxisValueFormatter(xLabels) // x축 레이블 표시
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
        lineChartSleepReportGraph.setVisibleXRangeMaximum(7f) // 화면에 한번에 보이는 데이터의 수 제한
        lineChartSleepReportGraph.moveViewToX(userSleepDataPoints.size.toFloat()) // 최근 데이터로 이동

        // 통계 점 클릭 시 나타나는 통계 세부 정보
        val dailySleepDurations = mutableListOf<String>()
        orderedSleepData.forEach {
            dailySleepDurations.add(it.second)
        }

        // 알람 데이터 추가
        val orderedAlarmData = userAlarmPrefs.all.map { Pair(it.key, it.value as String) }
            .sortedBy { dateFormatter.parse(it.first) }
        val dailyAlarmDurations = mutableListOf<String>()
        orderedAlarmData.forEach {
            dailyAlarmDurations.add(it.second)
        }

        val markerView = SleepReportMarkerView(
            requireContext(),
            R.layout.layout_sleep_report_marker_view,
            dailySleepDurations,
            dailyAlarmDurations
        )
        lineChartSleepReportGraph.marker = markerView

        /**
         * x축 라벨 밑에 아이콘 표시하기
         * Pair<Float, Drawable> → Map<Float, Drawable>
         * Map의 key인 Float는 Entry의 x값(index, position)을 의미 (라벨x)
         * vector drawable은 AppCompatResources.getDrawable로 가져와야 호환성이 보장됨
         * a to b = Pair(a,b)
         */
        val orderedUserMoodData = userMoodPrefs.all.map { Pair(it.key, it.value as String) }
            .sortedBy { dateFormatter.parse(it.first) }
        orderedUserMoodData.forEachIndexed { index, data ->
            val drawable = when (data.second) {
                "very_bad" -> AppCompatResources.getDrawable(
                    requireContext(),
                    R.drawable.ic_mood_very_bad
                )

                "bad" -> AppCompatResources.getDrawable(requireContext(), R.drawable.ic_mood_bad)
                "good" -> AppCompatResources.getDrawable(requireContext(), R.drawable.ic_mood_good)
                "very_good" -> AppCompatResources.getDrawable(
                    requireContext(),
                    R.drawable.ic_mood_very_good
                )

                else -> AppCompatResources.getDrawable(
                    requireContext(),
                    R.drawable.ic_alert
                ) // 임시 사용
            }
            val pair = index.toFloat() to drawable
            userMoodDataPoints.add(pair)
        }

        /**
         * 커스텀 XAxisRenderer 사용(매개변수 설명)
         * viewPortHandler: 차트의 뷰포트(보여지는 실제 영역)에 대한 정보를 관리하는 객체. 화면 상의 위치(좌표계)를 해석. 라벨을 어디에 그려야 할지 결정할 때 반드시 필요.
         * xAxis: X축에 대한 정보를 담는 객체. X축 라벨 포메팅, 텍스트 회전, 표시 여부 등을 결정.
         * getTransformer: 데이터 값(x, y)을 실제 화면에 그려지는 위치인 픽셀 좌표로 변환해주는 역할.
        - YAxis.AxisDependency.LEFT: Y축이 왼쪽/오른쪽 둘 다 있을 수 있기에, 어느 쪽 기준으로 좌표 변환할 것인지 명시.
        - 대부분 왼쪽 축을 기준으로 함
         * testIcons: 라벨 밑에 붙일 아이콘 콜렉션
         */
        lineChartSleepReportGraph.setXAxisRenderer(
            CustomXAxisRenderer(
                lineChartSleepReportGraph.viewPortHandler,
                lineChartSleepReportGraph.xAxis,
                lineChartSleepReportGraph.getTransformer(YAxis.AxisDependency.LEFT),
                userMoodDataPoints.toMap()
            )
        )

        lineChartSleepReportGraph.setExtraOffsets(
            0f,
            0f,
            0f,
            20f
        ) // 여백을 늘림 (이렇게 설정하지 않으면 아이콘이 표시될 영역이 부족해서 일부 짤림)

        // 평소 수면 정보 비교 UI
        // step1. 금일 수면 정보 추출하기
        val todaySleepData = orderedSleepData.last() // 금일 수면 정보 ex. (2025-08-03, 7시간 45분)
        val result = regex.find(todaySleepData.second) ?: return@with
        val (hour, minutes) = result.destructured
        val todaySleepMinutes = hour.toInt() * 60 + minutes.toInt()
        tvSleepReportAnalysisSleepTimeValue.text = "${todaySleepMinutes}분" // UI 반영

        // step2. 금일 제외 수면 정보 추출 및 누적 합산하기
        var accumulatedPastSleepMinutes = 0 // 금일 제외 총합 수면 시간 (단위: 분)
        val pastSleepData = orderedSleepData.subList(0, orderedSleepData.size - 1) // 금일 데이터 제외
        pastSleepData.forEach {
            val sleepTime = regex.find(it.second) ?: return@forEach
            val (hour, minutes) = sleepTime.destructured
            val totalSleepMinutes = hour.toInt() * 60 + minutes.toInt()
            accumulatedPastSleepMinutes += totalSleepMinutes
        }
        val averagePastSleepMinutes = accumulatedPastSleepMinutes / pastSleepData.size
        val sleepDifference = kotlin.math.abs(todaySleepMinutes - averagePastSleepMinutes) // 절댓값 계산
        tvSleepReportAnalysisSleepTimeGapValue.text = sleepDifference.toString() // UI 반영

        // step3. 금일 정보와 과거 정보 비교하기
        if (todaySleepMinutes > averagePastSleepMinutes) {
            tvSleepReportAnalysisSleepTimeTitle.text = "더 잤어요"
            ivSleepReportAnalysisSleepTimeChangeIndicator.setImageDrawable(
                AppCompatResources.getDrawable(
                    requireContext(),
                    R.drawable.ic_arrow_up
                )
            )
        } else if (todaySleepMinutes == averagePastSleepMinutes) {
            tvSleepReportAnalysisSleepTimeTitle.text = "평소만큼 잤어요"
            ivSleepReportAnalysisSleepTimeChangeIndicator.setImageDrawable(
                AppCompatResources.getDrawable(
                    requireContext(),
                    R.drawable.ic_average
                )
            ) // TODO: PM한테 물어보고 아이콘 변경하기
        } else {
            tvSleepReportAnalysisSleepTimeTitle.text = "잠이 부족했어요"
            ivSleepReportAnalysisSleepTimeChangeIndicator.setImageDrawable(
                AppCompatResources.getDrawable(
                    requireContext(),
                    R.drawable.ic_arrow_up
                )
            )
            ivSleepReportAnalysisSleepTimeChangeIndicator.rotation =
                180f // 180도 회전하여 기존 drawable 재활용
        }

        // 수면 점수 UI
        // TODO: 수면 점수 계산하기

        // 알람이 울린 시간 UI
        val alarmDuration = userAlarmPrefs.getString(LocalDate.now().toString(), "null") ?: ""
        tvSleepReportAnalysisAlarmDurationValue.text =
            if (alarmDuration == "null") "미측정" else alarmDuration

        // 일어나는데 걸린 시간(금일) UI
        tvSleepReportAnalysisWakeupDelayTimeValue.text =
            if (getMinuteDifference(alarmDuration) == -1) "미측정" else "${
                getMinuteDifference(alarmDuration)
            }분"

        // 일어나는데 걸린 시간(평균) UI
        var sum = 0
        userAlarmPrefs.all.filter { it.key != LocalDate.now().toString() }.map { it.value as String }.forEach {
            val minute = getMinuteDifference(it)
            if(minute != -1) sum += minute
        }
        val average = sum / (userAlarmPrefs.all.filter { it.value != "null" }.size - 1)
        var today = getMinuteDifference(
            userAlarmPrefs.getString(LocalDate.now().toString(), "null") ?: "null"
        )
        if(today == -1) today = 0 // 값이 없을 경우 변경
        tvSleepReportAnalysisWakeupDurationGapValue.text = "${kotlin.math.abs(today - average)}"

        if (today > average) ivSleepReportAnalysisWakeupDurationChangeIndicator.setImageDrawable(
            AppCompatResources.getDrawable(requireContext(), R.drawable.ic_arrow_up)
        )
        else if (today == average) ivSleepReportAnalysisWakeupDurationChangeIndicator.setImageDrawable(
            AppCompatResources.getDrawable(requireContext(), R.drawable.ic_average)
        ) else {
            ivSleepReportAnalysisWakeupDurationChangeIndicator.setImageDrawable(
                AppCompatResources.getDrawable(requireContext(), R.drawable.ic_arrow_up)
            )
            ivSleepReportAnalysisWakeupDurationChangeIndicator.rotation = 180f
        }
    }

    // 알람이 울린 시간에서 시간 차이(분) 계산하는 메소드
    private fun getMinuteDifference(timeRange: String): Int {
        if (timeRange == "null") return -1 else {
            val separatedTime = timeRange.split("~").map { it.trim() }
            val start = LocalTime.parse(separatedTime[0]) // Text '7:30' could not be parsed at index 0
            val end = LocalTime.parse(separatedTime[1])
            val difference = Duration.between(start, end).toMinutes().toInt()
            return difference
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
                // 오늘의 기분 shared preference에 저장하기
                when (todayMood) {
                    ivSleepReportTodayMoodVeryBad -> {
                        userMoodPrefs.edit().putString(LocalDate.now().toString(), "very_bad")
                            .apply() // 비동기 저장
                        userMoodDataPoints[userMoodDataPoints.lastIndex] =
                            userMoodDataPoints.lastIndex.toFloat() to AppCompatResources.getDrawable(
                                requireContext(),
                                R.drawable.ic_mood_very_bad
                            )
                        lineChartSleepReportGraph.setXAxisRenderer(
                            CustomXAxisRenderer(
                                lineChartSleepReportGraph.viewPortHandler,
                                lineChartSleepReportGraph.xAxis,
                                lineChartSleepReportGraph.getTransformer(YAxis.AxisDependency.LEFT),
                                userMoodDataPoints.toMap()
                            )
                        )
                        lineChartSleepReportGraph.invalidate()
                    }

                    ivSleepReportTodayMoodBad -> {
                        userMoodPrefs.edit().putString(LocalDate.now().toString(), "bad").apply()
                        userMoodDataPoints[userMoodDataPoints.lastIndex] =
                            userMoodDataPoints.lastIndex.toFloat() to AppCompatResources.getDrawable(
                                requireContext(),
                                R.drawable.ic_mood_bad
                            )
                        lineChartSleepReportGraph.setXAxisRenderer(
                            CustomXAxisRenderer(
                                lineChartSleepReportGraph.viewPortHandler,
                                lineChartSleepReportGraph.xAxis,
                                lineChartSleepReportGraph.getTransformer(YAxis.AxisDependency.LEFT),
                                userMoodDataPoints.toMap()
                            )
                        )
                        lineChartSleepReportGraph.invalidate()
                    }

                    ivSleepReportTodayMoodGood -> {
                        userMoodPrefs.edit().putString(LocalDate.now().toString(), "good").apply()
                        userMoodDataPoints[userMoodDataPoints.lastIndex] =
                            userMoodDataPoints.lastIndex.toFloat() to AppCompatResources.getDrawable(
                                requireContext(),
                                R.drawable.ic_mood_good
                            )
                        lineChartSleepReportGraph.setXAxisRenderer(
                            CustomXAxisRenderer(
                                lineChartSleepReportGraph.viewPortHandler,
                                lineChartSleepReportGraph.xAxis,
                                lineChartSleepReportGraph.getTransformer(YAxis.AxisDependency.LEFT),
                                userMoodDataPoints.toMap()
                            )
                        )
                        lineChartSleepReportGraph.invalidate()
                    }

                    ivSleepReportTodayMoodVeryGood -> {
                        userMoodPrefs.edit().putString(LocalDate.now().toString(), "very_good")
                            .apply()
                        userMoodDataPoints[userMoodDataPoints.lastIndex] =
                            userMoodDataPoints.lastIndex.toFloat() to AppCompatResources.getDrawable(
                                requireContext(),
                                R.drawable.ic_mood_very_good
                            )
                        lineChartSleepReportGraph.setXAxisRenderer(
                            CustomXAxisRenderer(
                                lineChartSleepReportGraph.viewPortHandler,
                                lineChartSleepReportGraph.xAxis,
                                lineChartSleepReportGraph.getTransformer(YAxis.AxisDependency.LEFT),
                                userMoodDataPoints.toMap()
                            )
                        )
                        lineChartSleepReportGraph.invalidate()
                    }
                }
            }
        }
    }
}