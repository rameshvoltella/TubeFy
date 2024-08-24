package com.ramzmania.tubefy.core.dataformatter.newpipe

import com.ramzmania.tubefy.data.database.DatabaseRepository
import com.ramzmania.tubefy.database.PlaylistDao
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NewPipeDataFormatterFactory @Inject constructor(private val playlistDao: PlaylistDao) {
    fun createForInfoItem(): NewPipeDataFormatter<InfoItem> {
        return NewPipeDataFormatter<InfoItem>(playlistDao)
    }
    fun createForPlayListItem(): NewPipeDataFormatter<StreamInfoItem?> {
        return NewPipeDataFormatter<StreamInfoItem?>(playlistDao)
    }

    // You can add more methods for different types if needed
}
