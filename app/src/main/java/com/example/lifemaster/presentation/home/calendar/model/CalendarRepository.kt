package com.example.lifemaster.presentation.home.calendar.model

import com.example.lifemaster.network.RetrofitInstance

class CalendarRepository(
    private val authProvider: () -> String?
) {
    private val api = RetrofitInstance.networkService

    private fun bearer(): String? =
        authProvider()
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?.let { if (it.startsWith("Bearer ")) it else "Bearer $it" }

    /** 내 달력: 월별 조회 */
    suspend fun getMonth(year: Int, month1: Int): List<CalendarEntry> {
        val yyyymm = "%04d%02d".format(year, month1)
        return api.getCalendarByMonth(yyyymm, bearer())
    }

    /** 공유 달력: 특정 멤버 월별 조회 */
    suspend fun getMemberMonth(memberId: Long, year: Int, month1: Int): List<CalendarEntry> {
        val yyyymm = "%04d%02d".format(year, month1)
        return api.getCalendarByMemberMonth(memberId, yyyymm, bearer())
    }
}