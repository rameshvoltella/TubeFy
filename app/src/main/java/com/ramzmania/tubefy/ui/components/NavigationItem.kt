package com.ramzmania.tubefy.ui.components

import com.ramzmania.tubefy.R

sealed class NavigationItem(var route: String, var icon: Int, var title: String) {
    data object Home : NavigationItem("home", R.drawable.ic_home, "Home")
    data object Music : NavigationItem("search", R.drawable.ic_music, "Search")
    data object Books : NavigationItem("library", R.drawable.ic_book, "Your Library")
    data object Profile : NavigationItem("profile", R.drawable.ic_profile, "Profile")
//    data object PlayList : NavigationItem("playlist", R.drawable.ic_profile, "Playlist")
    object PlayList : NavigationItem("playlist/{playlistId}/{playlistName}", R.drawable.ic_profile, "Playlist") {
        fun createRoute(playlistId: String, playlistName: String) = "playlist/$playlistId/$playlistName"
    }
}