package com.ramzmania.tubefy.data.dto.base.searchformat

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.schabi.newpipe.extractor.Page
@Parcelize

data class TubeFyCoreFormattedData(val youtubeSortedList:List<TubeFyCoreTypeData>?, val newPipePage: Page?=null, val youtubeV3NextToken:String?=null):
    Parcelable