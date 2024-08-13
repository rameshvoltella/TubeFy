package com.ramzmania.tubefy.data.dto.searchformat

import com.ramzmania.tubefy.core.YoutubeCoreConstant

data class TubeFyCoreTypeData(val videoId:String, val videoTitle:String, val videoImage:String,val videoBackupThump:String="https://i.ytimg.com/vi/${YoutubeCoreConstant.extractYoutubeVideoId(videoId)}/maxresdefault.jpg")