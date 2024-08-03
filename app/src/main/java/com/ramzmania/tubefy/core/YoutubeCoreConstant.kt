package com.ramzmania.tubefy.core

object YoutubeCoreConstant {
    const val YOUTUBE_V3_BASE_URL = "https://www.googleapis.com/youtube/v3/"
    const val YOUTUBE_V3API_KEY = "AIzaSyDD7uj_20UvVdn4DYZA31WBXi44QnPg3-g"
    const val YOUTUBE_V3_MAX_RESULT = 20


    const val START_TAG = "var ytInitialData"
    const val END_TAG = "</script>"
    const val YOUTUBE_WATCH_URL = "https://www.youtube.com/watch?v="
    const val DEFAULT_THROTTLE_TIMEOUT = 120L

    const val KEY_SERVICE_ID = "key_service_id"
    const val KEY_URL = "key_url"
    const val KEY_TITLE = "key_title"
    const val KEY_LINK_TYPE = "key_link_type"
    const val KEY_OPEN_SEARCH = "key_open_search"
    const val KEY_SEARCH_STRING = "key_search_string"

    const val KEY_THEME_CHANGE = "key_theme_change"
    const val KEY_MAIN_PAGE_CHANGE = "key_main_page_change"

    const val NO_SERVICE_ID = -1
    fun extractYoutubeVideoId(url: String): String? {
        return if (url.contains("http")) {
            val regex =
                "^(?:https?://)?(?:www\\.|music\\.)?(?:youtube\\.com/(?:[^/\\n\\s]+/.+/|(?:v|e(?:mbed)?)|\\?v=|\\&v=|embed/|\\?video_id=|shorts/|watch\\?v=|\\&video_id=)|youtu\\.be/)([a-zA-Z0-9_-]{11}).*".toRegex()
            val matchResult = regex.find(url)
            matchResult?.groups?.get(1)?.value
        } else {
            url
        }
    }
}