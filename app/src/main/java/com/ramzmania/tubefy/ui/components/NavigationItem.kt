package com.ramzmania.tubefy.ui.components

import androidx.navigation.NavController
import com.ramzmania.tubefy.R

sealed class NavigationItem(var route: String, var icon: Int, var title: String) {
    data object Home : NavigationItem("home", R.drawable.ic_home, "Home")
    data object Search : NavigationItem("search", R.drawable.ic_search, "Search")
    data object Books : NavigationItem("library", R.drawable.library, "Your Library")
    data object YouTube : NavigationItem("youtube", R.drawable.ic_search, "YouTube")

    /** Settings → Downloads: files downloaded from the YouTube video page. */
    data object VideoDownloads : NavigationItem("videoDownloads", R.drawable.ic_search, "Downloads")

    /** A YouTube channel's page. Values must be URL-encoded. */
    data object Channel : NavigationItem("channel?url={url}&name={name}", R.drawable.ic_search, "Channel") {
        fun createRoute(url: String, name: String) = "channel?url=$url&name=$name"
    }

    /** A YouTube video's page (opened from the YouTube tab). Values must be URL-encoded. */
    data object Video : NavigationItem("video/{videoId}?title={title}&thumb={thumb}&channel={channel}", R.drawable.ic_search, "Video") {
        fun createRoute(videoId: String, title: String, thumb: String, channel: String) =
            "video/$videoId?title=$title&thumb=$thumb&channel=$channel"
    }
    data object Profile : NavigationItem("settings", R.drawable.info, "Settings")
//    data object PlayList : NavigationItem("playlist", R.drawable.ic_profile, "Playlist")
   data object PlayList : NavigationItem("playlist/{playlistId}/{playlistName}/{playlistImage}", R.drawable.ic_search, "Playlist") {
        fun createRoute(playlistId: String, playlistName: String,playlistImage: String) = "playlist/$playlistId/$playlistName/$playlistImage"
    }
    data object AudioPlayer : NavigationItem("audioPlayer/{videoId}/{encodedVideoThumpUrl}/{playerHeader}/{playerBottomHeader}/{playerBottomSub}/{isBulk}?mode={mode}", R.drawable.ic_home, "AudioPlayer") {
        const val MODE_VIDEO = "video"

        /**
         * [offline] plays the queued Downloads from local files (the last segment becomes "offline").
         * [video] opens the player on its Video tab (the YouTube tab's videos).
         */
        fun createRoute(videoId: String, encodedVideoThumpUrl: String, playerHeader: String, playerBottomHeader: String, playerBottomSub: String,isBulk:Boolean=false, offline: Boolean = false, video: Boolean = false) =
            "audioPlayer/$videoId/$encodedVideoThumpUrl/$playerHeader/$playerBottomHeader/$playerBottomSub/${if (offline) "offline" else isBulk}" +
                if (video) "?mode=$MODE_VIDEO" else ""
    }

    data object CategoryPlayList : NavigationItem("categoryPlayList/{browserId}/{playerListId}/{categoryName}", R.drawable.ic_search, "CategoryPlayList") {
        fun createRoute(browserId: String, playerListId: String,categoryName:String) = "categoryPlayList/$browserId/$playerListId/$categoryName"
    }

    data object MyPlayList : NavigationItem("myPlayList/{playerListImage}/{playerListName}", R.drawable.library, "CategoryPlayList") {
        fun createRoute(playerListImage: String, playerListName: String) = "myPlayList/$playerListImage/$playerListName"
    }
}

/** Switches bottom navigation tab, keeping each tab's back stack and state. */
fun NavController.navigateToTab(route: String) {
    navigate(route) {
        graph.startDestinationRoute?.let { startRoute ->
            popUpTo(startRoute) {
                saveState = true
            }
        }
        launchSingleTop = true
        restoreState = true
    }
}
