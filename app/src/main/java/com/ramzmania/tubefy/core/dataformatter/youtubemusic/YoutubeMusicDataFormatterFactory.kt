package com.ramzmania.tubefy.core.dataformatter.youtubemusic

import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YoutubeMusicDataFormatterFactory @Inject constructor() {
    fun createForYoutubeMusicHomeDataFormatter(): YoutubeMusicHomeDataFormatter {
        return YoutubeMusicHomeDataFormatter()
    }
    fun createForYoutubeMusicCategoryDataFormatter(): YoutubeMusicCategoryDataFormatter {
        return YoutubeMusicCategoryDataFormatter()
    }

    // You can add more methods for different types if needed
}
