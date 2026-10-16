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
import org.schabi.newpipe.extractor.Image
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
                                sortedVideoList.add(
                                    TubeFyCoreTypeData(
                                        videoTitle = newPipeSearchData.name,
                                        videoImage = bestThumbnailUrl(newPipeSearchData.thumbnails),
                                        plaListUrl = newPipeSearchData.url
                                    )
                                )
                            } else {

                                sortedVideoList.add(
                                    TubeFyCoreTypeData(
                                        videoTitle = newPipeSearchData.name,
                                        videoImage = bestThumbnailUrl(newPipeSearchData.thumbnails),
                                        videoId = newPipeSearchData.url,
                                        artist = newPipeSearchData.uploaderName.orEmpty().removeSuffix(" - Topic"),
                                        durationSeconds = newPipeSearchData.duration,
                                        viewCount = newPipeSearchData.viewCount
                                    )
                                )
                                if (input?.contentFilter != null && input.contentFilter?.contains("all") != true) {
                                    playlistDao.addQueSingleSongPlaylists(
                                        QuePlaylist(
                                            videoId = newPipeSearchData.url,
                                            videoName = newPipeSearchData.name,
                                            videoThumbnail = bestThumbnailUrl(newPipeSearchData.thumbnails)
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

                                sortedVideoList.add(
                                    TubeFyCoreTypeData(
                                        videoTitle = newPipeSearchData.name,
                                        videoImage = bestThumbnailUrl(newPipeSearchData.thumbnails),
                                        plaListUrl = newPipeSearchData.url
                                    )
                                )
                            } else {

                                sortedVideoList.add(
                                    TubeFyCoreTypeData(
                                        videoTitle = newPipeSearchData.name,
                                        videoImage = bestThumbnailUrl(newPipeSearchData.thumbnails),
                                        videoId = newPipeSearchData.url
                                    )
                                )

                                if (input?.contentFilter != null && input.contentFilter?.contains("all") != true) {
                                    playlistDao.addQueSingleSongPlaylists(
                                        QuePlaylist(
                                            videoId = newPipeSearchData.url,
                                            videoName = newPipeSearchData.name,
                                            videoThumbnail = bestThumbnailUrl(newPipeSearchData.thumbnails)
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

/**
 * NewPipe lists thumbnails smallest first, so take the largest one. YouTube Music artwork
 * (googleusercontent) tops out at 120px; its URL encodes the size, so ask for a bigger one.
 */
internal fun bestThumbnailUrl(thumbnails: List<Image>): String {
    val url = thumbnails.maxByOrNull { it.width.toLong() * it.height }?.url ?: return ""
    return if (url.contains("googleusercontent.com") || url.contains("ggpht.com")) {
        GOOGLE_IMAGE_SIZE.replace(url, "=w544-h544")
    } else {
        url
    }
}

private val GOOGLE_IMAGE_SIZE = Regex("=w\\d+-h\\d+")
