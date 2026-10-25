package com.ramzmania.tubefy.ui.components.screen.player

import com.ramzmania.tubefy.core.YoutubeCoreConstant

/**
 * The album or playlist page the queue was played from, for the player's "Go to album / playlist".
 * [route] opens the page again; [argName] = [argValue] recognises it in the back stack.
 */
class PlayedFrom(
    val isAlbum: Boolean,
    val route: String,
    val argName: String,
    val argValue: String,
    songIds: Collection<String>
) {
    private val songIds = songIds.map(::normalize).toSet()

    operator fun contains(videoId: String) = normalize(videoId) in songIds

    private fun normalize(id: String) = YoutubeCoreConstant.extractYoutubeVideoId(id) ?: id

    companion object {
        var current: PlayedFrom? = null
    }
}
