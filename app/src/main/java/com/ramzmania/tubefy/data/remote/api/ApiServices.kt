package com.ramzmania.tubefy.data.remote.api

import com.ramzmania.tubefy.data.dto.youtubeV3.YoutubeSearchResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiServices {

    @GET("search")
    suspend fun getVideo(
        @Query("part") part: String,
        @Query("q") q: String,
        @Query("pageToken") pageToken: String?,
        @Query("maxResults")maxResult:Int
    ) : Response<YoutubeSearchResponse>

}