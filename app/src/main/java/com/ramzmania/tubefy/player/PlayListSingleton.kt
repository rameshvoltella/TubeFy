package com.ramzmania.tubefy.player

import com.ramzmania.tubefy.data.dto.searchformat.TubeFyCoreTypeData

object PlayListSingleton {
        private var myDataList: YoutubePlayerPlaylistListModel?= null
    fun addData(playListData: List<TubeFyCoreTypeData?>) {
        myDataList= YoutubePlayerPlaylistListModel(playListData)
    }

    fun getDataList(): YoutubePlayerPlaylistListModel? {
        return myDataList
    }

    }