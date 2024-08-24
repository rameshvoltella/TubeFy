package com.ramzmania.tubefy.data.dto.home.youtubei

import com.ramzmania.tubefy.data.dto.home.HomePageResponse

data class YoutubeiHomeBaseResponse (val paginationContent:PaginationContent?, val homePageContentDataList :List<HomePageResponse?>?)

data class PaginationContent(val paginationHex:String?,val paginationId:String?,val visitorData:String?)