package com.ramzmania.tubefy.player

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import com.ramzmania.tubefy.R
import com.ramzmania.tubefy.core.YoutubeCoreConstant
import com.ramzmania.tubefy.data.Resource
import com.ramzmania.tubefy.data.database.DatabaseRepositorySource
import com.ramzmania.tubefy.database.FavoritePlaylist
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * The Favorites button in the media notification: a round "+" adds the playing song to
 * Favorites, a (green) tick shows it's already there and removes it when tapped.
 * Must be called on the main thread (it reads the player).
 */
@UnstableApi
class NotificationFavorite(
    private val scope: CoroutineScope,
    private val sessionProvider: () -> MediaSession?,
    private val database: DatabaseRepositorySource,
    private val publish: (Bundle) -> Unit
) {
    private var favorite = false

    fun handleCommand(action: String) {
        when (action) {
            CMD_TOGGLE -> toggle()
            CMD_REFRESH -> refresh()
        }
    }

    /** Re-reads whether the playing song is a favorite (song changed, or the app changed it). */
    fun refresh() {
        val id = currentId()
        if (id == null) {
            show(favorite = false)
            return
        }
        scope.launch {
            val isFavorite = (database.isFavourite(id).first() as? Resource.Success)?.data == true
            withContext(Dispatchers.Main) {
                if (currentId() == id) show(isFavorite)
            }
        }
    }

    private fun toggle() {
        val item = sessionProvider()?.player?.currentMediaItem ?: return
        val id = currentId() ?: return
        val add = !favorite
        show(add) // update the button right away
        scope.launch {
            if (add) {
                database.addToFavorites(
                    FavoritePlaylist(
                        videoId = id,
                        videoThump = item.mediaMetadata.artworkUri?.toString().orEmpty(),
                        videoName = item.mediaMetadata.title?.toString().orEmpty()
                    )
                ).first()
            } else {
                database.removeFromFavorites(id).first()
            }
            // Lets the player screen refresh its heart
            withContext(Dispatchers.Main) {
                publish(bundleOf(EXTRA_FAVORITE_CHANGED_AT to System.currentTimeMillis()))
            }
        }
    }

    private fun currentId(): String? {
        val mediaId = sessionProvider()?.player?.currentMediaItem?.mediaId?.takeIf { it.isNotBlank() } ?: return null
        return YoutubeCoreConstant.extractYoutubeVideoId(mediaId) ?: mediaId
    }

    private fun show(favorite: Boolean) {
        this.favorite = favorite
        val button = CommandButton.Builder(
            if (favorite) CommandButton.ICON_CHECK_CIRCLE_FILLED else CommandButton.ICON_PLUS_CIRCLE_UNFILLED
        )
            .setCustomIconResId(if (favorite) R.drawable.ic_notification_fav_added else R.drawable.ic_notification_fav_add)
            .setDisplayName(if (favorite) "Remove from Favorites" else "Add to Favorites")
            .setSessionCommand(SessionCommand(CMD_TOGGLE, Bundle.EMPTY))
            // The system media notification only takes extra buttons from the overflow slot
            // (forward-secondary alone is dropped while previous/next exist)
            .setSlots(CommandButton.SLOT_FORWARD_SECONDARY, CommandButton.SLOT_OVERFLOW)
            .build()
        sessionProvider()?.setMediaButtonPreferences(listOf(button))
    }

    companion object {
        const val CMD_TOGGLE = "com.ramzmania.tubefy.FAVORITE_TOGGLE"

        /** Sent by the app after it changed the playing song's favorite state. */
        const val CMD_REFRESH = "com.ramzmania.tubefy.FAVORITE_REFRESH"

        /** Session extra: when the notification last changed Favorites (the player re-checks its heart). */
        const val EXTRA_FAVORITE_CHANGED_AT = "com.ramzmania.tubefy.FAVORITE_CHANGED_AT"

        val COMMANDS = listOf(CMD_TOGGLE, CMD_REFRESH).map { SessionCommand(it, Bundle.EMPTY) }
    }
}
