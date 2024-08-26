package com.ramzmania.tubefy.data.dto.base.searchformat

import android.os.Parcelable
import com.ramzmania.tubefy.core.YoutubeCoreConstant
import com.ramzmania.tubefy.data.dto.base.PlaylistItem
import kotlinx.parcelize.Parcelize

@Parcelize

data class TubeFyCoreTypeData(val videoId:String="", val videoTitle:String, val videoImage:String,val videoBackupThump:String="https://i.ytimg.com/vi/${YoutubeCoreConstant.extractYoutubeVideoId(videoId)}/maxresdefault.jpg",val plaListUrl:String?=null):
    Parcelable, PlaylistItem