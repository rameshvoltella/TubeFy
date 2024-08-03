package com.ramzmania.tubefy.core.dataformatter.stripper

import android.util.Log
import com.ramzmania.tubefy.core.dataformatter.dto.TubeFyCoreTypeData
import com.ramzmania.tubefy.data.dto.youtubestripper.ApiResponse

class YouTubePageStripDataFormatter {

    fun getFormattedData(youtubeJsonScrapping: ApiResponse):ArrayList<TubeFyCoreTypeData>?{
//        youtubeJsonScrapping.contents
//        for ()
        var sortedVideoList:ArrayList<TubeFyCoreTypeData>?=ArrayList()
        for(contents in youtubeJsonScrapping.contents.sectionListRenderer.contents)
        {
            var videoName=""
            var videoId=""
            var videoImage=""
            Log.d("checker","-----------------1")
            if(contents.itemSectionRenderer!=null)
            {
                Log.d("checker","-----------------22222")

                for(contentsBaseRenderer in contents.itemSectionRenderer.contentsBaseRenderer)
                {
                    Log.d("checker","-----------------33333")

                    if(contentsBaseRenderer!=null) {
                        Log.d("checker","-----------------4444444")

                        if (contentsBaseRenderer.reelShelfRenderer != null) {
                            if(contentsBaseRenderer.reelShelfRenderer.shortsList!=null)
                            {
                                Log.d("checker","-----------------5555555")

                                for(shortData in contentsBaseRenderer.reelShelfRenderer.shortsList)
                                {
                                    if(shortData.trackingParams!=null) {
                                        Log.d("checker","-----------------66666666")

                                        shortData.trackingParams.reelsOnTap.innerTubeCommand.reelWatchEndpoint.videoId
                                        videoName = shortData.trackingParams.accessibilityText
                                        if(shortData.trackingParams.reelsOnTap?.innerTubeCommand?.reelWatchEndpoint != null)
                                        {
                                            videoId=shortData.trackingParams.reelsOnTap.innerTubeCommand.reelWatchEndpoint.videoId
                                            Log.d("checker","-----------------777777"+videoId)

                                        }
                                        if(shortData.trackingParams.reelsThumpNail?.thumpList != null)
                                        {
                                            for(thumpList in shortData.trackingParams.reelsThumpNail?.thumpList)
                                            {
                                                videoImage= thumpList.reelShortsThumpNailUrl
                                            }
                                        }
                                    }
                                }
                                sortedVideoList?.add(TubeFyCoreTypeData(videoId,videoName,videoImage))
                            }

                        }
                        else if (contentsBaseRenderer.videoWithContextRenderer != null) {

                            videoId=contentsBaseRenderer.videoWithContextRenderer.videoId
                            if(contentsBaseRenderer?.videoWithContextRenderer?.headline?.accessibility?.accessibilityData?.label!=null) {
                                videoName =
                                    contentsBaseRenderer.videoWithContextRenderer.headline.accessibility.accessibilityData.label
                            }
                            if(contentsBaseRenderer.videoWithContextRenderer.thumbnail!=null&&contentsBaseRenderer.videoWithContextRenderer.thumbnail.thumbnails!=null)
                            {
                               for(thumbnails in contentsBaseRenderer.videoWithContextRenderer.thumbnail.thumbnails)
                               {
                                   videoImage=thumbnails.reelShortsThumpNailUrl
                               }
                            }
                            sortedVideoList?.add(TubeFyCoreTypeData(videoId,videoName,videoImage))



                        }
                    }
                }
            }

        }
//        Log.d("tada","????"+sortedVideoList?.size)
        return sortedVideoList
    }
}