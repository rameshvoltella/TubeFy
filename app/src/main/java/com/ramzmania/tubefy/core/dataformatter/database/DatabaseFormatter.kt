package com.ramzmania.tubefy.core.dataformatter.database

import com.ramzmania.tubefy.core.dataformatter.FormattingResult
import com.ramzmania.tubefy.core.dataformatter.UniversalYoutubeDataFormatter
import com.ramzmania.tubefy.data.dto.base.PlaylistItem
import com.ramzmania.tubefy.data.dto.base.searchformat.TubeFyCoreTypeData
import com.ramzmania.tubefy.database.ActivePlaylist
import com.ramzmania.tubefy.database.CustomPlaylist
import com.ramzmania.tubefy.database.FavoritePlaylist
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.KClass

@Singleton
class DatabaseFormatter<InputType, OutputType,ListType : PlaylistItem> @Inject constructor(private val listTypeClass: KClass<ListType>) :
    UniversalYoutubeDataFormatter<InputType, FormattingResult<OutputType, Exception>>() {

    override suspend fun runFormatting(input: InputType): FormattingResult<OutputType, Exception> {

        val listData: ArrayList<ListType> = ArrayList()

        when (input) {
            is List<*> -> {
                input.forEach { dataValue ->
                    when (dataValue) {

                        is TubeFyCoreTypeData -> {


                            if (listTypeClass == ActivePlaylist::class)
                            {
                                listData.add(
                                    ActivePlaylist(
                                        videoThumbnail = dataValue.videoImage,
                                        videoId = dataValue.videoId,
                                        videoName = dataValue.videoTitle
                                    )as ListType
                                )
                            }

                        }
                        is ActivePlaylist -> {
                            listData.add(
                                TubeFyCoreTypeData(
                                    videoImage = dataValue.videoThumbnail,
                                    videoId = dataValue.videoId,
                                    videoTitle = dataValue.videoName
                                )as ListType
                            )
                        }
                        is FavoritePlaylist->{

                        }

                        is CustomPlaylist->{

                        }
                        else -> {
                            // Handle other types if needed
                        }
                    }
                }
            }
            else -> {
                return FormattingResult.FAILURE(Exception("Unsupported input type"))
            }
        }

       /* // Convert ActivePlaylist list to the desired OutputType list
        val resultList: OutputType = convertToOutputType(activePlayListList)

        return if (resultList != null) {
            FormattingResult.SUCCESS(resultList)
        } else {
            FormattingResult.FAILURE(Exception("No data"))
        }*/

        if (listData.size > 0) {

            val resultList: OutputType = convertToOutputType(listData)

            return if (resultList != null) {
                FormattingResult.SUCCESS(resultList)
            } else {
                FormattingResult.FAILURE(Exception("No data"))
            }
        }


  /*      else if (activeTubeFyList.size > 0) {
            val resultList: OutputType = convertToTubeFyCoreTypeDataOutputType(activeTubeFyList)

            return if (resultList != null) {
                FormattingResult.SUCCESS(resultList)
            } else {

                FormattingResult.FAILURE(Exception("No data"))
            }
        }*/

        else {
            return FormattingResult.FAILURE(Exception("No data"))

        }

    }

    // Add this method to convert ActivePlaylist list to the desired OutputType list
    private fun convertToOutputType(activePlayListList: List<ListType>): OutputType {
        // Implement conversion logic based on OutputType
        // Example placeholder: return activePlayListList as OutputType
        return activePlayListList as OutputType
//        throw NotImplementedError("Conversion to OutputType not implemented")
    }

    private fun convertToTubeFyCoreTypeDataOutputType(activePlayListList: List<TubeFyCoreTypeData?>): OutputType {
        // Implement conversion logic based on OutputType
        // Example placeholder: return activePlayListList as OutputType
        return activePlayListList as OutputType
//        throw NotImplementedError("Conversion to OutputType not implemented")
    }


}
