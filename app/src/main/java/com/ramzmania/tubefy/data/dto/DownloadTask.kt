package com.ramzmania.tubefy.data.dto

import androidx.annotation.Keep

@Keep
data class DownloadTask(
    val id: String,
    val url: String,
    val filePath: String,
    val progress: Int = 0,
    val isCompleted: Boolean = false,
    val isFailed: Boolean = false
)
