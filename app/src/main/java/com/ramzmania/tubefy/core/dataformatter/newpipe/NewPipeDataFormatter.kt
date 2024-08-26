package com.ramzmania.tubefy.core.dataformatter.newpipe

import android.util.Log
import com.ramzmania.tubefy.core.dataformatter.FormattingResult
import com.ramzmania.tubefy.core.dataformatter.UniversalYoutubeDataFormatter
import com.ramzmania.tubefy.core.dataformatter.YoutubeApiType
import com.ramzmania.tubefy.data.database.DatabaseRepository
import com.ramzmania.tubefy.data.dto.base.searchformat.NewPipeSortingData
import com.ramzmania.tubefy.data.dto.base.searchformat.TubeFyCoreFormattedData
import com.ramzmania.tubefy.data.dto.base.searchformat.TubeFyCoreTypeData
import com.ramzmania.tubefy.data.dto.base.searchformat.TubeFyCoreUniversalData
import com.ramzmania.tubefy.database.PlaylistDao
import com.ramzmania.tubefy.database.QuePlaylist
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NewPipeDataFormatter<T> @Inject constructor(private val playlistDao: PlaylistDao) :
    UniversalYoutubeDataFormatter<NewPipeSortingData<T>, FormattingResult<TubeFyCoreUniversalData, Exception>>() {
    //        @Inject
//        lateinit var playlistDao: PlaylistDao
    override suspend fun runFormatting(input: NewPipeSortingData<T>): FormattingResult<TubeFyCoreUniversalData, Exception> {
        return withContext(Dispatchers.IO) {
            try {

                val sortedVideoList: ArrayList<TubeFyCoreTypeData> = ArrayList()
                for (newPipeSearchData in input.result) {
//                    Log.d("daz",""+newPipeSearchData)
                    when (newPipeSearchData) {
                        is StreamInfoItem -> {
                            if (newPipeSearchData.url.contains("playList?list")) {
                                Log.d("Chekzzzop", "yoooooplist>" + newPipeSearchData.url)
                                sortedVideoList.add(
                                    TubeFyCoreTypeData(
                                        videoTitle = newPipeSearchData.name,
                                        videoImage = newPipeSearchData.thumbnails[0].url,
                                        plaListUrl = newPipeSearchData.url
                                    )
                                )
                            } else {
                                Log.d("Chekzzzop", "yooooovideo" + newPipeSearchData.url)

                                sortedVideoList.add(
                                    TubeFyCoreTypeData(
                                        videoTitle = newPipeSearchData.name,
                                        videoImage = newPipeSearchData.thumbnails[0].url,
                                        videoId = newPipeSearchData.url
                                    )
                                )
                                if (input?.contentFilter != null&&!input?.contentFilter.contains("all")) {
                                    Log.d("tadada", "klklklkl1111")
                                    playlistDao.addQueSingleSongPlaylists(
                                        QuePlaylist(
                                            videoId = newPipeSearchData.url,
                                            videoName = newPipeSearchData.name,
                                            videoThumbnail = newPipeSearchData.thumbnails[0].url
                                        )
                                    )
                                }
                            }
                        }

                        is InfoItem -> {
                            if (newPipeSearchData.url.contains(
                                    "playlist?list",
                                    ignoreCase = true
                                )
                            ) {
                                Log.d("Chekzzzop", "2yooooopPlalist>" + newPipeSearchData.url)

                                sortedVideoList.add(
                                    TubeFyCoreTypeData(
                                        videoTitle = newPipeSearchData.name,
                                        videoImage = newPipeSearchData.thumbnails[0].url,
                                        plaListUrl = newPipeSearchData.url
                                    )
                                )
                            } else {
                                Log.d("Chekzzzop", "2yooooopVideo>" + newPipeSearchData.url)

                                sortedVideoList.add(
                                    TubeFyCoreTypeData(
                                        videoTitle = newPipeSearchData.name,
                                        videoImage = newPipeSearchData.thumbnails[0].url,
                                        videoId = newPipeSearchData.url
                                    )
                                )

                                Log.d("tadada", "yaaaaaa")
                                if (input?.contentFilter != null&&!input?.contentFilter.contains("all")) {
                                    Log.d("tadada", "klklklkl222222")
                                    playlistDao.addQueSingleSongPlaylists(
                                        QuePlaylist(
                                            videoId = newPipeSearchData.url,
                                            videoName = newPipeSearchData.name,
                                            videoThumbnail = newPipeSearchData.thumbnails[0].url
                                        )
                                    )
                                }

                            }
                        }

                        // Handle other types if necessary
                        else -> {
                            // Handle cases where T is not InfoItem
                        }
                    }
                }
                FormattingResult.SUCCESS(
                    TubeFyCoreUniversalData(
                        TubeFyCoreFormattedData(sortedVideoList, input.nextPage),
                        YoutubeApiType.NEW_PIPE_API
                    )
                )

            } catch (ex: Exception) {
                ex.printStackTrace()
                FormattingResult.FAILURE(Exception("Unable to get Youtube URL"))
            }
        }

    }
}