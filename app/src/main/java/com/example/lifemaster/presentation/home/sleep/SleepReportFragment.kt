package com.example.lifemaster.presentation.home.sleep

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.lifemaster.R
import com.example.lifemaster.databinding.FragmentSleepReportBinding
import com.example.lifemaster.network.RetrofitInstance
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
import androidx.core.graphics.toColorInt
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.example.lifemaster.presentation.Constants
import com.example.lifemaster.presentation.home.sleep.model.AlarmInfo
import com.example.lifemaster.presentation.home.sleep.model.Result
import com.example.lifemaster.presentation.home.sleep.model.SleepRequest
import java.time.Instant

class SleepReportFragment : Fragment(R.layout.fragment_sleep_report) {

    private lateinit var binding: FragmentSleepReportBinding
    private val sleepViewModel: SleepViewModel by activityViewModels {
        SleepViewModelFactory(RetrofitInstance.networkService)
    }

    private var userSleepDataPoints = mutableListOf<Entry>() // 1개의 line 을 구성하는 점들의 집합
    private var userMoodDataPoints = mutableListOf<Pair<Float, Drawable?>>()

    private var xLabels = mutableListOf<String>() // x축에 표시할 값(단위: 일)
    private var yValues = mutableListOf<Float>() // y축에 표시할 값(수면 점수)

    private var dailySleepDurations = mutableListOf<String>()
    private var dailyAlarmDurations = mutableListOf<Int>()
    private var dailySleepScores = mutableListOf<Float>()

    private var remoteUserSleepRecordList = listOf<SleepResponse>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSleepReportBinding.bind(view)
        fetchRemoteData()
        initObservers()
    }

    private fun fetchRemoteData() {
        sleepViewModel.getUserSleepInfo(userId = Constants.USER_ID)
    }

    private fun initObservers() = with(binding) {

        // 유저의 수면 기록 조회
        sleepViewModel.userSleepRecordList.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> {
                    initLoadingUI() // TODO: 로딩 UI 만들기(프로그래스바 등등..)
                }

                is Result.Success -> {
                    remoteUserSleepRecordList = result.data
                    initRemoteUI(remoteUserSleepRecordList)
                    initRemoteListeners(remoteUserSleepRecordList)
                }

                is Result.Error -> {
                    Toast.makeText(context, "서버 에러: ${result.throwable}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        sleepViewModel.isUserSleepRecordGenerated.observe(viewLifecycleOwner) { event ->
            event.getDataIfNotHandled()?.let { isGenerated ->
                if(isGenerated) {
                    sleepViewModel.getUserSleepInfo(Constants.USER_ID)
                }
            }
        }

        // 유저의 오늘의 기분 정보 업데이트
        sleepViewModel.userSleepUpdatedRecord.observe(viewLifecycleOwner) { result ->
            when(result) {
                is Result.Loading -> {}
                is Result.Success -> {
                    Toast.makeText(context, "오늘의 기분이 업데이트 되었습니다!", Toast.LENGTH_SHORT).show()
                    val updatedSleepRecord = result.data

                    // 1. 오늘의 기분 UI 업데이트
                    when (updatedSleepRecord.sleepMood) {
                        VERY_BAD -> changeSelectedMoodColor(ivSleepReportTodayMoodVeryBad)
                        BAD -> changeSelectedMoodColor(ivSleepReportTodayMoodBad)
                        GOOD -> changeSelectedMoodColor(ivSleepReportTodayMoodGood)
                        VERY_GOOD -> changeSelectedMoodColor(ivSleepReportTodayMoodVeryGood)
                    }

                    // 2. 차트 UI 업데이트
                    yValues[yValues.lastIndex] = updatedSleepRecord.sleepScore

                    userSleepDataPoints[userSleepDataPoints.lastIndex] = Entry(
                        yValues.lastIndex.toFloat(), updatedSleepRecord.sleepScore
                    )

                    dailySleepScores[dailySleepScores.lastIndex] = updatedSleepRecord.sleepScore

                    val updatedMoodIcon = when(updatedSleepRecord.sleepMood) {
                        VERY_BAD -> AppCompatResources.getDrawable(requireContext(), R.drawable.ic_mood_very_bad)
                        BAD -> AppCompatResources.getDrawable(requireContext(), R.drawable.ic_mood_bad)
                        GOOD -> AppCompatResources.getDrawable(requireContext(), R.drawable.ic_mood_good)
                        VERY_GOOD -> AppCompatResources.getDrawable(requireContext() ,R.drawable.ic_mood_very_good)
                        else -> AppCompatResources.getDrawable(requireContext(), R.drawable.ic_alert)
                    }

                    val updatedPair = yValues.lastIndex.toFloat() to updatedMoodIcon
                    userMoodDataPoints[userMoodDataPoints.lastIndex] = updatedPair

                    lineChartSleepReportGraph.invalidate()

                    // 3. 평균 비교 UI 업데이트
                    val todayUpdatedSleepScore = updatedSleepRecord.sleepScore.toInt()
                    tvSleepReportAnalysisSleepScoreValue.text = "${todayUpdatedSleepScore}점"

                    val filteredDataExcludingToday = remoteUserSleepRecordList.filter { it.sleepDate != LocalDate.now().toString() }
                    var sum = 0f

                    filteredDataExcludingToday.forEach { sleepData ->
                        val sleepScore = sleepData.sleepScore
                        sum += sleepScore
                    }

                    var average = (sum/filteredDataExcludingToday.size).toInt()

                    tvSleepReportAnalysisSleepScoreGapValue.text = "${kotlin.math.abs(todayUpdatedSleepScore - average)}"
                    if(todayUpdatedSleepScore > average) {
                        ivSleepReportAnalysisSleepScoreChangeIndicator.setImageResource(R.drawable.ic_arrow_up)
                    } else if(todayUpdatedSleepScore == average) {
                        ivSleepReportAnalysisSleepScoreChangeIndicator.setImageResource(R.drawable.ic_average)
                    } else {
                        ivSleepReportAnalysisSleepScoreChangeIndicator.setImageResource(R.drawable.ic_arrow_up)
                        ivSleepReportAnalysisSleepScoreChangeIndicator.rotation = 180f
                    }

                }
                is Result.Error -> {
                    Toast.makeText(context, "서버 에러: ${result.throwable}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun initLoadingUI() = with(binding) {
        // TODO: 로딩 UI 만들기
    }

    private fun initRemoteUI(remoteUserSleepRecordList: List<SleepResponse>) = with(binding) {

        val todaySleepRecord = remoteUserSleepRecordList.find { it.sleepDate == LocalDate.now().toString() }

        if(todaySleepRecord == null && sleepViewModel.isMeasured == true) {
            sleepViewModel.registerUserSleepInfo(
                sleepRequest = SleepRequest(
                    userId = Constants.USER_ID,
                    sleepDate = LocalDate.now().toString(),
                    sleepStart = Instant.ofEpochMilli(sleepViewModel.rawSleepTime ?: 0L).toString(),
                    sleepEnd =  Instant.ofEpochMilli(sleepViewModel.rawWakeTime ?: 0L).toString(),
                    sleepMood = "GOOD",
                    alarmInfo = AlarmInfo(
                        isWakeUpAlarmSet = false
                    )
                )
            )
        }

        // 금일 수면 기록 정보가 없는 경우
        if(!sleepViewModel.isMeasured) {
            tvSleepReportTitle.text = "오늘은\n측정 기록이 없어요"

            // TODO: 오늘의 기분은 그래프에 표시하기
            // TODO: 차트에 금일 기록 미측정 UI + 오늘의 기분 데이터만 표시하기

            tvSleepReportAnalysisTitle.text = "오늘은"

            tvSleepReportAnalysisSleepTimeTitle.text = "수면 기록이 없어요"
            tvSleepReportAnalysisSleepTimeValue.text = "미측정"
            cvSleepReportAnalysisSleepTimeCompare.isVisible = false

            tvSleepReportAnalysisSleepScoreValue.text = "점수 기록이 없어요"
            tvSleepReportAnalysisSleepScoreValue.text = "미측정"
            cvSleepReportAnalysisSleepScoreCompare.isVisible = false

            return@with // TODO: 이후 로직을 수행할 필요가 있는가?

        }

        // 서버에서 금일 데이터 존재 o, 기상 알람 설정 x
        if(todaySleepRecord?.alarmInfo?.isWakeUpAlarmSet == false) {
            // 알람을 설정하지 않은 경우
            tvSleepReportAnalysisAlarmDurationValue.text = "알람 미설정"
            tvSleepReportAnalysisWakeupDelayTimeValue.text = "기록 없음"
            cvSleepReportAnalysisWakeupDelayTimeCompare.isVisible = false
            // TODO: 일어나는데 걸린 시간 카드뷰 value 담기
        }

        // 서버에서 금일 데이터 존재 o, 기상 알람 설정 o

        // 금일 수면 타이틀 UI
        tvSleepReportTitle.text = "오늘은\n총 ${todaySleepRecord?.sleepDurationText} 잤어요"

        // 금일 기분 UI
        when (todaySleepRecord?.sleepMood) {
            VERY_BAD -> changeSelectedMoodColor(ivSleepReportTodayMoodVeryBad)
            BAD -> changeSelectedMoodColor(ivSleepReportTodayMoodBad)
            GOOD -> changeSelectedMoodColor(ivSleepReportTodayMoodGood)
            VERY_GOOD -> changeSelectedMoodColor(ivSleepReportTodayMoodVeryGood)
        }

        // 통계 UI
        remoteUserSleepRecordList.forEach { record ->
            val sleepDayOfMonth = record.sleepDate.split("-")[2] // "2025-08-28" → "28"
            xLabels.add(sleepDayOfMonth)
            yValues.add(record.sleepScore)
        }

        yValues.forEachIndexed { index, score ->
            userSleepDataPoints.add(
                Entry(index.toFloat(), score)
            )
        }

        // line 1개
        val lineDataSet = LineDataSet(userSleepDataPoints, "수면 점수 그래프").apply {
            color = "#BBAB94".toColorInt() // 선 색상
            lineWidth = 3f // 선 굵기
            setCircleColor("#927448".toColorInt()) // 점 색상
            circleRadius = 4f // 점 크기
            setDrawValues(false) // 값이 안보이게 하기
        }

        // 여러 개의 line 을 담는 전체 그래프 데이터
        val lineData = LineData(lineDataSet)

        with(lineChartSleepReportGraph) {
            // 데이터 연결
            data = lineData

            // 그래프 x축 설정
            xAxis.position = XAxis.XAxisPosition.BOTTOM // x축의 위치 지정
            xAxis.valueFormatter = IndexAxisValueFormatter(xLabels) // x축 레이블 표시
            xAxis.granularity = 1f // x축 레이블이 표시될 최소 간격 단위
            xAxis.textColor = "#C5C6C6".toColorInt() // x축 값 색상
            xAxis.textSize = 12f // x축 값 크기

            // 그래프 y축 설정
            axisLeft.textColor = "#C5C6C6".toColorInt() // y축 값 색상
            axisLeft.textSize = 12f // y축 값 크기

            // 기타 설정
            axisRight.isEnabled = false // 오른쪽 y축값 표시 비활성화
            animateX(1000) // 선이 그려지는 애니메이션을 1초동안 실행
            legend.isEnabled = false // LineDataSet 에서 지정한 두번째 파라미터가 표시되지 않음
            description.isEnabled = false // 맨 오른쪽 하단에 표시되는 그래프 설명 비활성화
            isDoubleTapToZoomEnabled = false // 더블 탭하여 확대되는 기능 비활성화
            setScaleEnabled(false) // 그래프 확대 기능 비활성화
            setVisibleXRangeMaximum(7f) // 화면에 한번에 보이는 데이터의 수 제한
            moveViewToX(userSleepDataPoints.size.toFloat()) // 최근 데이터로 이동
        }

        // 통계 점 클릭 시 나타나는 세부 정보
        dailySleepDurations = mutableListOf<String>() // 수면 시간
        dailyAlarmDurations = mutableListOf<Int>() // 일어나는 데 걸린 시간
        dailySleepScores = mutableListOf<Float>()

        remoteUserSleepRecordList.forEach { record ->
            dailySleepDurations.add(record.sleepDurationText)
            dailyAlarmDurations.add(record.alarmInfo.alarmSettings?.timeToWakeUp ?: 0)
            dailySleepScores.add(record.sleepScore)
        }

        val markerView = SleepReportMarkerView(
            requireContext(),
            R.layout.layout_sleep_report_marker_view,
            dailySleepDurations,
            dailySleepScores,
            dailyAlarmDurations,
        )
        lineChartSleepReportGraph.marker = markerView

        // x축 라벨 밑에 아이콘 표시하기 (Pair의 first = Entry의 x값(index, position))
        remoteUserSleepRecordList.forEachIndexed { index, record ->
            val drawable = when (record.sleepMood) {
                VERY_BAD -> AppCompatResources.getDrawable(requireContext(), R.drawable.ic_mood_very_bad)
                BAD -> AppCompatResources.getDrawable(requireContext(), R.drawable.ic_mood_bad)
                GOOD -> AppCompatResources.getDrawable(requireContext(), R.drawable.ic_mood_good)
                VERY_GOOD -> AppCompatResources.getDrawable(requireContext() ,R.drawable.ic_mood_very_good)
                else -> AppCompatResources.getDrawable(requireContext(), R.drawable.ic_alert)
            }
            val pair = index.toFloat() to drawable
            userMoodDataPoints.add(pair)
        }

        with(lineChartSleepReportGraph) {
            // 커스텀 XAxis Renderer에 추가할 아이콘 전달
            setXAxisRenderer(
                CustomXAxisRenderer(
                    lineChartSleepReportGraph.viewPortHandler,
                    lineChartSleepReportGraph.xAxis,
                    lineChartSleepReportGraph.getTransformer(YAxis.AxisDependency.LEFT),
                    userMoodDataPoints.toMap()
                )
            )
            // 여백 증가 (아이콘이 표시될 영역이 부족)
            setExtraOffsets(
                0f,
                0f,
                0f,
                20f
            )
        }

        // TODO: 평균 비교하기
    }

    // 선택된 오늘의 기분 UI 색상을 변경하는 메소드
    private fun changeSelectedMoodColor(imageView: ImageView) {
        imageView.setColorFilter(
            resources.getColor(
                R.color.sleep_mood_selected,
                context?.theme
            )
        )
    }

    private fun initRemoteListeners(remoteUserSleepRecordList: List<SleepResponse>) = with(binding) {

        // 화면 이동하기
        ivSleepReportGoToPlaylist.setOnClickListener {
            findNavController().navigate(R.id.action_sleepReportFragment_to_sleepPlaylistFragment)
        }

        val todaySleepRecord = remoteUserSleepRecordList.find { it.sleepDate == LocalDate.now().toString() }

        if(todaySleepRecord == null) {
            // TODO: 수면 기록이 측정되지 않은 경우 오늘의 기분 표시를 어떻게 할 것인가?
            return@with
        }

        val todayMoods = listOf(
            ivSleepReportTodayMoodVeryBad,
            ivSleepReportTodayMoodBad,
            ivSleepReportTodayMoodGood,
            ivSleepReportTodayMoodVeryGood
        )

        for(todayMood in todayMoods) {
            todayMood.setOnClickListener {

                // UI 변경
                todayMoods.forEach { it.clearColorFilter() }
                todayMood.setColorFilter(
                    resources.getColor(
                        R.color.sleep_mood_selected,
                        context?.theme
                    )
                )

                // 서버 연결
                when(todayMood) {
                    ivSleepReportTodayMoodVeryBad -> {
                        sleepViewModel.updateUserSleepInfo(
                            SleepRequest(
                                userId = Constants.USER_ID,
                                sleepId = todaySleepRecord.sleepId,
                                sleepDate = todaySleepRecord.sleepDate,
                                sleepStart = todaySleepRecord.sleepStart,
                                sleepEnd = todaySleepRecord.sleepEnd,
                                sleepMood = VERY_BAD,
                                alarmInfo = AlarmInfo(
                                    isWakeUpAlarmSet = todaySleepRecord.alarmInfo.isWakeUpAlarmSet,
                                    alarmSettings = todaySleepRecord.alarmInfo.alarmSettings
                                )
                            )
                        )

                        // 통계 UI 변경
                        changeTodayMoodGraphUI(R.drawable.ic_mood_very_bad)
                    }
                    ivSleepReportTodayMoodBad -> {
                        sleepViewModel.updateUserSleepInfo(
                            SleepRequest(
                                userId = Constants.USER_ID,
                                sleepId = todaySleepRecord.sleepId,
                                sleepDate = todaySleepRecord.sleepDate,
                                sleepStart = todaySleepRecord.sleepStart,
                                sleepEnd = todaySleepRecord.sleepEnd,
                                sleepMood = BAD,
                                alarmInfo = AlarmInfo(
                                    isWakeUpAlarmSet = todaySleepRecord.alarmInfo.isWakeUpAlarmSet,
                                    alarmSettings = todaySleepRecord.alarmInfo.alarmSettings
                                )
                            )
                        )
                        changeTodayMoodGraphUI(R.drawable.ic_mood_bad)
                    }
                    ivSleepReportTodayMoodGood -> {
                        sleepViewModel.updateUserSleepInfo(
                            SleepRequest(
                                userId = Constants.USER_ID,
                                sleepId = todaySleepRecord.sleepId,
                                sleepDate = todaySleepRecord.sleepDate,
                                sleepStart = todaySleepRecord.sleepStart,
                                sleepEnd = todaySleepRecord.sleepEnd,
                                sleepMood = GOOD,
                                alarmInfo = AlarmInfo(
                                    isWakeUpAlarmSet = todaySleepRecord.alarmInfo.isWakeUpAlarmSet,
                                    alarmSettings = todaySleepRecord.alarmInfo.alarmSettings
                                )
                            )
                        )
                        changeTodayMoodGraphUI(R.drawable.ic_mood_good)
                    }
                    ivSleepReportTodayMoodVeryGood -> {
                        sleepViewModel.updateUserSleepInfo(
                            SleepRequest(
                                userId = Constants.USER_ID,
                                sleepId = todaySleepRecord.sleepId,
                                sleepDate = todaySleepRecord.sleepDate,
                                sleepStart = todaySleepRecord.sleepStart,
                                sleepEnd = todaySleepRecord.sleepEnd,
                                sleepMood = VERY_GOOD,
                                alarmInfo = AlarmInfo(
                                    isWakeUpAlarmSet = todaySleepRecord.alarmInfo.isWakeUpAlarmSet,
                                    alarmSettings = todaySleepRecord.alarmInfo.alarmSettings
                                )
                            )
                        )
                        changeTodayMoodGraphUI(R.drawable.ic_mood_very_good)
                    }
                }
            }
        }
    }

    private fun FragmentSleepReportBinding.changeTodayMoodGraphUI(moodIcon: Int) {
        userMoodDataPoints[userMoodDataPoints.lastIndex] =
            userMoodDataPoints.lastIndex.toFloat() to AppCompatResources.getDrawable(
                requireContext(),
                moodIcon
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

    companion object {
        private const val VERY_BAD = "VERY_BAD"
        private const val BAD = "BAD"
        private const val GOOD = "GOOD"
        private const val VERY_GOOD = "VERY_GOOD"
        private const val NO_DATA = "null"
    }
}