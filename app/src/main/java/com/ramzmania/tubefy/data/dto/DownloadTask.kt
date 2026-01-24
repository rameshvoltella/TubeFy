package com.ramzmania.tubefy.data.dto

data class DownloadTask(
    val id: String,
    val url: String,
    val filePath: String,
    val progress: Int = 0,
    val isCompleted: Boolean = false,
    val isFailed: Boolean = false
)
