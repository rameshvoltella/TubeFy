package com.ramzmania.tubefy.data.remote.api

import com.ramzmania.tubefy.data.dto.home.HomePageResponse
import com.ramzmania.tubefy.data.dto.home.youtubei.YoutubeiMusicHomeApiResponse
import com.ramzmania.tubefy.data.dto.home.youtubei.next.ContinuationContents
import com.ramzmania.tubefy.data.dto.youtubeV3.YoutubeSearchResponse
import com.ramzmania.tubefy.data.dto.youtubemusic.playlist.categoryplaylist.BrowseHomePaginationRequest
import com.ramzmania.tubefy.data.dto.youtubemusic.playlist.categoryplaylist.BrowseHomeRequest
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
    @POST
    suspend fun getMusicHomeYoutubeiInfo(@Body request: BrowseHomeRequest, @Url url:String): Response<YoutubeiMusicHomeApiResponse>

    @POST
    suspend fun getMusicHomeYoutubeiPaginationInfo(@Body request: BrowseHomePaginationRequest, @Url url:String): Response<ContinuationContents>
}