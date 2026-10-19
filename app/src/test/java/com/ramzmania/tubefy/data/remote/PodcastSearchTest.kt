package com.ramzmania.tubefy.data.remote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PodcastSearchTest {

    private fun item(browseId: String, title: String, vararg subtitle: String) = """
        {"musicResponsiveListItemRenderer": {
          "thumbnail": {"musicThumbnailRenderer": {"thumbnail": {"thumbnails": [
            {"url": "https://img/small.jpg"}, {"url": "https://img/large.jpg"}]}}},
          "flexColumns": [
            {"musicResponsiveListItemFlexColumnRenderer": {"text": {"runs": [{"text": "$title"}]}}},
            {"musicResponsiveListItemFlexColumnRenderer": {"text": {"runs": [${subtitle.joinToString(",") { "{\"text\": \"$it\"}" }}]}}}
          ],
          "navigationEndpoint": {"browseEndpoint": {"browseId": "$browseId"}}
        }}
    """

    private fun response(vararg items: String) =
        """{"contents": {"tabbedSearchResultsRenderer": {"tabs": [{"tabRenderer": {"content": {"sectionListRenderer": {"contents": [
            {"musicShelfRenderer": {"contents": [${items.joinToString(",")}]}}]}}}}]}}}"""

    @Test
    fun `extracts podcast shows with playlist id, author and largest thumbnail`() {
        val shows = PodcastSearch.parseShows(
            response(item("MPSPPLabc123", "Malayalam Horror Stories", "Podcast", " • ", "KathaVerse"))
        )
        assertEquals(1, shows.size)
        val show = shows.single()
        assertEquals("PLabc123", show.playlistId)
        assertEquals("Malayalam Horror Stories", show.title)
        assertEquals("KathaVerse", show.author)
        assertEquals("https://img/large.jpg", show.thumbnail)
    }

    @Test
    fun `ignores non podcast results and duplicates`() {
        val shows = PodcastSearch.parseShows(
            response(
                item("UCartist", "Some artist"),
                item("MPSPPLone", "Show one", "Host"),
                item("MPSPPLone", "Show one again", "Host")
            )
        )
        assertEquals(listOf("PLone"), shows.map { it.playlistId })
    }

    @Test
    fun `empty or unrelated json gives no shows`() {
        assertTrue(PodcastSearch.parseShows("""{"contents": {}}""").isEmpty())
    }
}
