package com.example.lifemaster.presentation.home.alarm.view.fragment

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.lifemaster.R
import com.example.lifemaster.databinding.FragmentAlarmRandomMissionTapBinding
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random


class AlarmRandomMissionTapFragment : Fragment(R.layout.fragment_alarm_random_mission_tap) {

    private lateinit var binding: FragmentAlarmRandomMissionTapBinding
    private lateinit var taps: List<MaterialCardView>
    private var answerTapPositions: MutableSet<Int> = hashSetOf()
    private var userTapPositions: MutableSet<Int> = hashSetOf()
    private var currentPage: Int = 1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAlarmRandomMissionTapBinding.bind(view)
        currentPage = arguments?.getInt("currentPageNum")!! // 파라미터 전달 받기
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // 뒤로가기 버튼 비활성화
            }
        })
        initViews()
        initListeners()
    }

    private fun initViews() = with(binding) {
        Log.e("TTEST", ""+currentPage)
        tvAlarmRandomMissionTapPage.text = "${currentPage}/3"
        taps = listOf(
            cvAlarmRandomMissionTap1,
            cvAlarmRandomMissionTap2,
            cvAlarmRandomMissionTap3,
            cvAlarmRandomMissionTap4,
            cvAlarmRandomMissionTap5,
            cvAlarmRandomMissionTap6,
            cvAlarmRandomMissionTap7,
            cvAlarmRandomMissionTap8,
            cvAlarmRandomMissionTap9,
            cvAlarmRandomMissionTap10,
            cvAlarmRandomMissionTap11,
            cvAlarmRandomMissionTap12,
            cvAlarmRandomMissionTap13,
            cvAlarmRandomMissionTap14,
            cvAlarmRandomMissionTap15,
            cvAlarmRandomMissionTap16,
            cvAlarmRandomMissionTap17,
            cvAlarmRandomMissionTap18,
            cvAlarmRandomMissionTap19,
            cvAlarmRandomMissionTap20,
            cvAlarmRandomMissionTap21,
            cvAlarmRandomMissionTap22,
            cvAlarmRandomMissionTap23,
            cvAlarmRandomMissionTap24,
            cvAlarmRandomMissionTap25
        )
        lifecycleScope.launch {
            for (tap in taps) { tap.isEnabled = false } // 사용자 터치 임시 비활성화
            // repeat 코드 실행 시간 거의 0ms에 가까움
            repeat(5) {
                val i = Random.nextInt(0, 25) // 0 ~ 24 (중복 허용)
                taps[i].apply {
                    isSelected = true
                    setCardBackgroundColor(
                        resources.getColor(
                            R.color.alarm_primary,
                            context?.theme
                        )
                    )
                }
                answerTapPositions.add(i)
            }
            delay(1000)
            tvAlarmRandomMissionTapCount.text = "2"
            delay(1000)
            tvAlarmRandomMissionTapCount.text = "1"
            delay(1000)
            tvAlarmRandomMissionTapCount.isVisible = false
            for (tap in taps) {
                tap.isSelected = false
                tap.setCardBackgroundColor(
                    resources.getColor(
                        R.color.light_gray_100,
                        context?.theme
                    )
                )
            }
            for (tap in taps) { tap.isEnabled = true } // 사용자 터치 재활성화
        }
    }

    private fun initListeners() = with(binding) {
        for (tap in taps) {
            tap.setOnClickListener {
                tap.isSelected = !tap.isSelected
                if (tap.isSelected) {
                    tap.setCardBackgroundColor(
                        resources.getColor(
                            R.color.alarm_primary,
                            context?.theme
                        )
                    )
                } else {
                    tap.setCardBackgroundColor(
                        resources.getColor(
                            R.color.light_gray_100,
                            context?.theme
                        )
                    )
                }
            }
        }
        cvAlarmRandomMissionNextPage.setOnClickListener {
            taps.forEachIndexed { position, tap ->
                if(tap.isSelected) userTapPositions.add(position) else userTapPositions.remove(position)
            }
            if(answerTapPositions.equals(userTapPositions)) {
                if(currentPage == 3) {
                    Toast.makeText(context, "수고하셨습니다!", Toast.LENGTH_SHORT).show()
                } else {
                    findNavController().navigate(
                        R.id.alarmRandomMissionTapFragment,
                        bundleOf("currentPageNum" to ++currentPage),
                        NavOptions.Builder()
                            .setLaunchSingleTop(true) // 최상단이 같은 프래그먼트인 경우 쌓지 않고 교체함
                            .build()
                    )
                }
            }
            else { Toast.makeText(context, "답이 틀렸습니다! 다시 입력해주세요!", Toast.LENGTH_SHORT).show() }
        }
    }

}
