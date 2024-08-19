package com.ramzmania.tubefy.ui.components

import com.ramzmania.tubefy.R

sealed class NavigationItem(var route: String, var icon: Int, var title: String) {
    data object Home : NavigationItem("home", R.drawable.ic_home, "Home")
    data object Search : NavigationItem("search", R.drawable.ic_search, "Search")
    data object Books : NavigationItem("library", R.drawable.ic_book, "Your Library")
    data object Profile : NavigationItem("profile", R.drawable.ic_profile, "Profile")
//    data object PlayList : NavigationItem("playlist", R.drawable.ic_profile, "Playlist")
   data object PlayList : NavigationItem("playlist/{playlistId}/{playlistName}/{playlistImage}", R.drawable.ic_profile, "Playlist") {
        fun createRoute(playlistId: String, playlistName: String,playlistImage: String) = "playlist/$playlistId/$playlistName/$playlistImage"
    }
    data object AudioPlayer : NavigationItem("audioPlayer/{videoId}/{encodedVideoThumpUrl}/{playerHeader}/{playerBottomHeader}/{playerBottomSub}/{isBulk}", R.drawable.ic_profile, "AudioPlayer") {
        fun createRoute(videoId: String, encodedVideoThumpUrl: String, playerHeader: String, playerBottomHeader: String, playerBottomSub: String,isBulk:Boolean=false) = "audioPlayer/$videoId/$encodedVideoThumpUrl/$playerHeader/$playerBottomHeader/$playerBottomSub/$isBulk"
    }

    data object CategoryPlayList : NavigationItem("categoryPlayList/{browserId}/{playerListId}/{categoryName}", R.drawable.ic_profile, "CategoryPlayList") {
        fun createRoute(browserId: String, playerListId: String,categoryName:String) = "categoryPlayList/$browserId/$playerListId/$categoryName"
    }
}