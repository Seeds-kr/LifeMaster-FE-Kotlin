package com.example.lifemaster.presentation.community.model

data class PollListItem(
    val pollId: Long,
    val title: String,
    val status: String,
    val endDate: String? = null
)

data class PollDetailsDto(
    val title: String,
    val isExpired: Boolean,
    val totalVotes: Int? = null,
    val options: List<PollOption>,
    val myVotedOptionId: Int? = null
)

data class PollOption(
    val content: String,
    val votes: Int? = null,
    val votePercentage: Int? = null,
    val optionId: Int? = null
)

data class PollResultDto(
    val percentage: Int,
    val votes: Int
)

data class VoteRequest(
    val optionId: Int,
    val userId: String
)