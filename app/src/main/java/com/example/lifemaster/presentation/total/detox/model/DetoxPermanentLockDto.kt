package com.example.lifemaster.presentation.total.detox.model

import com.google.gson.annotations.SerializedName

data class DetoxPermanentLock(
    @SerializedName("lockedApps")
    val lockedAppPackageNames: List<String>
)
