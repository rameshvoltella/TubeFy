package com.ramzmania.tubefy.player

import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata

fun createMediaItems(mediaUris: List<String>, videoThumpUrls: List<String>, videoTitles: List<String>,videoIdList: List<String>): List<MediaItem> {
    return mediaUris.indices.map { index ->
        MediaItem.Builder()
            .setMediaId(videoIdList[index])
            .setUri(Uri.parse(mediaUris[index]))
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(videoTitles.getOrElse(index) { "Unknown Title" })
                    .setArtist("Artist :${videoTitles.getOrElse(index) { "Unknown Artist" }}")
                    .setIsBrowsable(false)
                    .setIsPlayable(true)
                    .setArtworkUri(Uri.parse(videoThumpUrls.getOrElse(index) { "" }))
                    .setMediaType(MediaMetadata.MEDIA_TYPE_MUSIC)
                    .build()
            )
            .build()
    }
}