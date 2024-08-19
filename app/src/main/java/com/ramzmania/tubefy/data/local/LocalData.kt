package com.ramzmania.tubefy.data.local

import com.ramzmania.tubefy.core.dataformatter.FormattingResult
import com.ramzmania.tubefy.core.dataformatter.defaultplaylist.DefaultHomePlayListDataFormatter
import com.ramzmania.tubefy.data.dto.base.searchformat.TubeFyCoreUniversalData
import com.ramzmania.tubefy.core.dataformatter.webscrapper.YoutubeWebDataFormatter
import com.ramzmania.tubefy.core.dataformatter.youtubemusic.YoutubeMusicDataFormatterFactory
import com.ramzmania.tubefy.data.Resource
import com.ramzmania.tubefy.data.dto.base.playlist.PlayListCategory
import com.ramzmania.tubefy.data.dto.home.HomePageResponse
import com.ramzmania.tubefy.data.dto.home.defaultmodel.LocalHomeData
import com.ramzmania.tubefy.data.dto.home.defaultmodel.DefaultBaseModel
import com.ramzmania.tubefy.data.dto.home.defaultmodel.PlaylistCategories
import com.ramzmania.tubefy.data.dto.youtubemusic.category.YtMusicCategoryBase
import com.ramzmania.tubefy.data.dto.youtubemusic.category.YtMusicCategoryContent
import com.ramzmania.tubefy.data.dto.youtubestripper.ApiResponse
import com.ramzmania.tubefy.data.dto.youtubestripper.MusicHomeResponse2
import com.ramzmania.tubefy.errors.YOUTUBE_V3_SEARCH_ERROR
import com.ramzmania.tubefy.utils.AssetJsonReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class LocalData @Inject
constructor(
    private val youtubeWebDataFormatter: YoutubeWebDataFormatter, private val youtubeMusicDataFormatterFactory: YoutubeMusicDataFormatterFactory, private val defaultHomePlayListDataFormatter: DefaultHomePlayListDataFormatter, val assetJsonReader: AssetJsonReader
) : LocalDataSource {
    override suspend fun manipulateYoutubeSearchStripData(youtubeJsonScrapping: ApiResponse): Resource<TubeFyCoreUniversalData> {
        return withContext(Dispatchers.IO)
        {
            val result=youtubeWebDataFormatter.run(youtubeJsonScrapping)
             when(result)
            {
                is FormattingResult.SUCCESS->{  Resource.Success(result.data)
                }
                is FormattingResult.FAILURE->{Resource.DataError(YOUTUBE_V3_SEARCH_ERROR)}

            }

        }

//        return if (sortedVideoDataList != null) {
//            Resource.Success(BasicResponse(sortedVideoDataList!!))
//
//        } else {
//            Resource.DataError(401)
//        }

    }

    override suspend fun manipulateYoutubeMusicHomeStripData(musicHomeResponse: MusicHomeResponse2): Resource<List<HomePageResponse?>> {
        return withContext(Dispatchers.IO)
        {
            val result=youtubeMusicDataFormatterFactory.createForYoutubeMusicHomeDataFormatter().run(musicHomeResponse)
            when(result)
            {
                is FormattingResult.SUCCESS->{  Resource.Success(result.data)
                }
                is FormattingResult.FAILURE->{Resource.DataError(YOUTUBE_V3_SEARCH_ERROR)}

            }

        }
    }

    override suspend fun loadDefaultHomePageData(): Resource<List<HomePageResponse?>> {
        return withContext(Dispatchers.IO)
        {

            val globalPlayList=assetJsonReader.loadModelFromAsset<PlaylistCategories>("default/default_playlist_formate.json")
            val composerList=ArrayList<LocalHomeData?>()
            composerList.add(assetJsonReader.loadModelFromAsset<LocalHomeData>("default/top_composers.json"))
            composerList.add(assetJsonReader.loadModelFromAsset<LocalHomeData>("default/top_composer_kerala.json"))
            composerList.add(assetJsonReader.loadModelFromAsset<LocalHomeData>("default/top_composer_south.json"))
            composerList.add(assetJsonReader.loadModelFromAsset<LocalHomeData>("default/top_composers_india.json"))
            composerList.add(assetJsonReader.loadModelFromAsset<LocalHomeData>("default/top_singers_india.json"))
            composerList.add(assetJsonReader.loadModelFromAsset<LocalHomeData>("default/top_style.json"))

            val result=defaultHomePlayListDataFormatter.run(DefaultBaseModel(globalPlayList,composerList))
            when(result)
            {
                is FormattingResult.SUCCESS->{  Resource.Success(result.data)
                }
                is FormattingResult.FAILURE->{Resource.DataError(YOUTUBE_V3_SEARCH_ERROR)}

            }

        }
    }

    override suspend fun manipulateYoutubeMusicCategorySearchStripData(youtubeJsonScrapping: YtMusicCategoryBase): Resource<List<PlayListCategory?>> {
        return withContext(Dispatchers.IO)
        {
            val result=youtubeMusicDataFormatterFactory.createForYoutubeMusicCategoryDataFormatter().run(youtubeJsonScrapping)
            when(result)
            {
                is FormattingResult.SUCCESS->{  Resource.Success(result.data)
                }
                is FormattingResult.FAILURE->{Resource.DataError(YOUTUBE_V3_SEARCH_ERROR)}

            }

        }    }


}