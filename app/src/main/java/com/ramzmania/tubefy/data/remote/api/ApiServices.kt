package com.ramzmania.tubefy.data.remote.api

import com.ramzmania.tubefy.data.dto.youtubeV3.YoutubeSearchResponse
import com.ramzmania.tubefy.data.dto.youtubemusic.playlist.categoryplaylist.BrowseRequest
import com.ramzmania.tubefy.data.dto.youtubemusic.playlist.categoryplaylist.CategoryPlayListRoot
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url

interface ApiServices {

    @GET("search")
    suspend fun getVideo(
        @Query("part") part: String,
        @Query("q") q: String,
        @Query("pageToken") pageToken: String?,
        @Query("maxResults")maxResult:Int
    ) : Response<YoutubeSearchResponse>

    @POST
    suspend fun getCategoryPlaylistInfo(@Body request: BrowseRequest,@Url url:String): Response<CategoryPlayListRoot>

}