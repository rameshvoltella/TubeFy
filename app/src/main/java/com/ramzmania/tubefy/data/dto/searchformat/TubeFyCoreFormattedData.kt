package com.ramzmania.tubefy.data.dto.searchformat

import org.schabi.newpipe.extractor.Page

data class TubeFyCoreFormattedData(val youtubeSortedList:List<TubeFyCoreTypeData>?, val newPipePage: Page?=null, val youtubeV3NextToken:String?=null)