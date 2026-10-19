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
 * Extra buttons in the media notification (and lock screen / Android Auto):
 * - Shuffle: off ↔ on; when Smart Shuffle is on it shows a sparkle and turns shuffle off.
 * - Favorites: a round "+" adds the playing song, a (green) tick removes it again.
 * Must be called on the main thread (it reads the player).
 */
@UnstableApi
class NotificationButtons(
    private val scope: CoroutineScope,
    private val sessionProvider: () -> MediaSession?,
    private val database: DatabaseRepositorySource,
    private val isSmartShuffleOn: () -> Boolean,
    private val turnSmartShuffleOff: () -> Unit,
    private val publish: (Bundle) -> Unit
) {
    private var favorite = false

    /** What the buttons last showed, so unchanged states aren't re-sent (each send re-posts the notification). */
    private var shown: Triple<Boolean, Boolean, Boolean>? = null

    fun handleCommand(action: String) {
        when (action) {
            CMD_TOGGLE -> toggleFavorite()
            CMD_REFRESH -> refresh()
            CMD_SHUFFLE -> toggleShuffle()
        }
    }

    /** Re-reads whether the playing song is a favorite (song changed, or the app changed it). */
    fun refresh() {
        val id = currentId()
        if (id == null) {
            favorite = false
            show()
            return
        }
        scope.launch {
            val isFavorite = (database.isFavourite(id).first() as? Resource.Success)?.data == true
            withContext(Dispatchers.Main) {
                if (currentId() == id) {
                    favorite = isFavorite
                    show()
                }
            }
        }
    }

    /** Call when shuffle or Smart Shuffle changed elsewhere (player screen, other controllers). */
    fun onShuffleChanged() = show()

    private fun toggleShuffle() {
        val player = sessionProvider()?.player ?: return
        if (isSmartShuffleOn()) {
            turnSmartShuffleOff()
            player.shuffleModeEnabled = false
        } else {
            player.shuffleModeEnabled = !player.shuffleModeEnabled
        }
        show()
    }

    private fun toggleFavorite() {
        val item = sessionProvider()?.player?.currentMediaItem ?: return
        val id = currentId() ?: return
        val add = !favorite
        favorite = add
        show() // update the button right away
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

    private fun show() {
        val session = sessionProvider() ?: return
        val smart = isSmartShuffleOn()
        val shuffleOn = smart || session.player.shuffleModeEnabled
        val state = Triple(smart, shuffleOn, favorite)
        if (state == shown) return
        shown = state
        val shuffle = CommandButton.Builder(
            when {
                smart -> CommandButton.ICON_SHUFFLE_STAR
                shuffleOn -> CommandButton.ICON_SHUFFLE_ON
                else -> CommandButton.ICON_SHUFFLE_OFF
            }
        )
            .setDisplayName(
                when {
                    smart -> "Smart Shuffle on (tap to turn off)"
                    shuffleOn -> "Shuffle on (tap to turn off)"
                    else -> "Shuffle"
                }
            )
            .setSessionCommand(SessionCommand(CMD_SHUFFLE, Bundle.EMPTY))
            // The system media notification only takes extra buttons from the overflow slot
            .setSlots(CommandButton.SLOT_BACK_SECONDARY, CommandButton.SLOT_OVERFLOW)
            .build()
        val favoriteButton = CommandButton.Builder(
            if (favorite) CommandButton.ICON_CHECK_CIRCLE_FILLED else CommandButton.ICON_PLUS_CIRCLE_UNFILLED
        )
            .setCustomIconResId(if (favorite) R.drawable.ic_notification_fav_added else R.drawable.ic_notification_fav_add)
            .setDisplayName(if (favorite) "Remove from Favorites" else "Add to Favorites")
            .setSessionCommand(SessionCommand(CMD_TOGGLE, Bundle.EMPTY))
            .setSlots(CommandButton.SLOT_FORWARD_SECONDARY, CommandButton.SLOT_OVERFLOW)
            .build()
        // Shuffle first, Favorites second: systems that show two extra buttons put them on either side
        session.setMediaButtonPreferences(listOf(shuffle, favoriteButton))
    }

    companion object {
        const val CMD_TOGGLE = "com.ramzmania.tubefy.FAVORITE_TOGGLE"

        /** Sent by the app after it changed the playing song's favorite state. */
        const val CMD_REFRESH = "com.ramzmania.tubefy.FAVORITE_REFRESH"

        const val CMD_SHUFFLE = "com.ramzmania.tubefy.SHUFFLE_TOGGLE"

        /** Session extra: when the notification last changed Favorites (the player re-checks its heart). */
        const val EXTRA_FAVORITE_CHANGED_AT = "com.ramzmania.tubefy.FAVORITE_CHANGED_AT"

        val COMMANDS = listOf(CMD_TOGGLE, CMD_REFRESH, CMD_SHUFFLE).map { SessionCommand(it, Bundle.EMPTY) }
    }
}
