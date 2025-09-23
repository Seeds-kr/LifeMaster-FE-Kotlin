package com.example.lifemaster.presentation.community.model

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

fun PostSummaryDto.toCommunityItem(idStr: String): CommunityItem =
    CommunityItem(
        id = idStr,
        title = this.title.orEmpty(),
        content = "",
        author = this.nickName.orEmpty(),
        authorImage = null,
        likes = 0,
        views = this.viewCount ?: 0,
        createdAt = parseIsoToMillisFlexible(this.createdAt) ?: 0L,
        fileUri = null,
        type = "FREE",
        commentCount = this.commentCount ?: 0,
        comments = emptyList()
    )

fun parseIsoToMillisFlexible(iso: String?): Long? {
    if (iso.isNullOrBlank()) return null
    val patterns = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSSX",
        "yyyy-MM-dd'T'HH:mm:ssX",
        "yyyy-MM-dd'T'HH:mm:ss.SSS",
        "yyyy-MM-dd HH:mm:ss",
        "yyyy.MM.dd HH:mm",
        "yyyy.MM.dd",
        "yyyy-MM-dd"
    )
    for (p in patterns) {
        try {
            val sdf = SimpleDateFormat(p, Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            return sdf.parse(iso)?.time
        } catch (_: Throwable) {}
    }
    return null
}