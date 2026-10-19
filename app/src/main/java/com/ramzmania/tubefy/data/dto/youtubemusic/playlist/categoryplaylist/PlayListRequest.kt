package com.ramzmania.tubefy.data.dto.youtubemusic.playlist.categoryplaylist

import com.squareup.moshi.Json

data class Client(
    val clientName: String,
    val clientVersion: String
//    val originalUrl: String=""
)

data class ClientPagination(
    val clientName: String,
    val clientVersion: String,
    val visitorData: String
)

data class Context(
    val client: Client
)

data class ContextPagination(
    val client: ClientPagination
)

data class BrowseRequest(
    val context: Context,
    val browseId: String,
    /** Left out of the request when null (e.g. the podcasts page has no params). */
    val params: String? = null
)

data class BrowseHomeRequest(
    val context: Context
)

data class BrowseHomePaginationRequest(
    val context: ContextPagination
)
