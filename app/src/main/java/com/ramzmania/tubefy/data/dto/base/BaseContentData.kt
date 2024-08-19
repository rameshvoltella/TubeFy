package com.ramzmania.tubefy.data.dto.base

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BaseContentData (val videoId: String?,
                            val playlistId: String?,
                            val thumbnail: String?,
                            val title: String?,val isScrapData:Boolean=false):Parcelable