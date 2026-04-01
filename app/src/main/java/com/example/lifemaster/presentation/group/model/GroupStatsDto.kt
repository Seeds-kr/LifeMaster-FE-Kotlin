package com.example.lifemaster.presentation.group.model

data class GroupSleepStatsResponse(
    val userSleepDurations: List<Int>
)

data class GroupGoalProgressResponseItem(
    val goalValue: Int,
    val goalDuration: String,
    val goalCondition: String,
    val goalName: String,
    val userProgress: List<UserProgressItem>
)

data class UserProgressItem(
    val userEmail: String,
    val progress: Int? = null
)

// 최근 30일 달성 인원
data class GroupAchievementHeatmapItem(
    val date: String,
    val achievedUserCount: Int
)

// 그룹 내 랭킹
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
    val rank: Int,
    val memberId: Long? = null,
    val nickname: String,
    val profileImage: String? = null,
    val achieveCount: Int
)