package com.example.lifemaster.presentation.group.model

data class GroupSleepStatsResponse(
    val userSleepDurations: List<Int> = emptyList(),
    val groupAverageSleepDurations: List<Int> = emptyList()
)

data class GroupGoalProgressResponseItem(
    val goalValue: Int,
    val goalDuration: String,
    val goalCondition: String,
    val goalName: String,
    val userProgress: List<UserProgressItem> = emptyList()
)

data class UserProgressItem(
    val userEmail: String,
    val progress: Int? = null
)

data class GroupAchievementHeatmapItem(
    val date: String,
    val achievedUserCount: Int
)

data class GroupRankingResponse(
    val scope: String? = null,
    val myRank: Int? = null,
    val memberId: Long? = null,
    val myName: String? = null,
    val profileImage: String? = null,
    val achieveCount: Int? = null,
    val items: List<GroupRankingItem> = emptyList()
)

data class GroupRankingItem(
    val rank: Int = 0,
    val memberId: Long? = null,
    val nickname: String? = null,
    val profileImage: String? = null,
    val achieveCount: Int = 0
)

data class GroupRecentGoalStatisticsResponse(
    val goalId: Long,
    val goalType: String,
    val userValues: List<Float>,
    val groupAverageValues: List<Float>
)