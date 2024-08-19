package com.ramzmania.tubefy.core.dataformatter.newpipe

import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NewPipeDataFormatterFactory @Inject constructor() {
    fun createForInfoItem(): NewPipeDataFormatter<InfoItem> {
        return NewPipeDataFormatter<InfoItem>()
    }
    fun createForPlayListItem(): NewPipeDataFormatter<StreamInfoItem?> {
        return NewPipeDataFormatter<StreamInfoItem?>()
    }

    // You can add more methods for different types if needed
}
