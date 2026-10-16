package com.ramzmania.tubefy.ui.components.screen.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ramzmania.tubefy.R
import com.ramzmania.tubefy.core.YoutubeCoreConstant.decodeThumpUrl
import com.ramzmania.tubefy.data.Resource
import com.ramzmania.tubefy.data.dto.base.searchformat.TubeFyCoreTypeData
import com.ramzmania.tubefy.ui.components.ChipRow
import com.ramzmania.tubefy.ui.components.screen.album.formatDuration
import com.ramzmania.tubefy.ui.components.screen.album.openTrack
import com.ramzmania.tubefy.ui.components.screen.category.CategoryScreenMain
import com.ramzmania.tubefy.ui.components.screen.library.PlayListDialogViewer
import com.ramzmania.tubefy.ui.theme.DiscoverAccent
import com.ramzmania.tubefy.ui.theme.DiscoverBackground
import com.ramzmania.tubefy.ui.theme.DiscoverBorder
import com.ramzmania.tubefy.ui.theme.DiscoverSurface
import com.ramzmania.tubefy.ui.theme.DiscoverTextSecondary
import com.ramzmania.tubefy.utils.LocalNavController
import com.ramzmania.tubefy.viewmodel.TubeFyViewModel
import org.schabi.newpipe.extractor.Page

/**
 * Result type filters. Each maps to a NewPipe YouTube content filter, depending on whether
 * the user searches YouTube videos (toggle on) or YouTube Music songs (toggle off).
 */
private enum class SearchFilter(val label: String, val youtubeFilter: String, val musicFilter: String) {
    All("All", "all", "music_songs"),
    Songs("Songs", "music_songs", "music_songs"),
    Videos("Videos", "videos", "music_videos"),
    Playlists("Playlists", "playlists", "music_playlists");

    fun contentFilter(youtubeVideos: Boolean) =
        mutableListOf(if (youtubeVideos) youtubeFilter else musicFilter)
}

@Composable
fun AudioSearchScreen(viewModel: TubeFyViewModel = hiltViewModel()) {
    val textFieldValueSaver = Saver<TextFieldValue, String>(
        save = { it.text },
        restore = { TextFieldValue(it) }
    )
    var textFieldValue by rememberSaveable(stateSaver = textFieldValueSaver) {
        mutableStateOf(TextFieldValue())
    }
    val searchPlayListName by viewModel.youTubeSearchData.observeAsState()
    var isLoading by rememberSaveable { mutableStateOf(false) }
    var videoListItems by rememberSaveable { mutableStateOf<List<TubeFyCoreTypeData?>>(emptyList()) }
    var page by rememberSaveable { mutableStateOf<Page?>(null) }
    val lazyListState = rememberLazyListState()
    var isChecked by rememberSaveable { mutableStateOf(false) }
    var filter by rememberSaveable { mutableStateOf(SearchFilter.All) }
    var isFreshSearch by rememberSaveable { mutableStateOf(false) }
    val showPlayListDialog = viewModel.showPlayListDialog.collectAsState()
    val currentSongItemSelected = viewModel.selectedTrack.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current

    fun search() {
        if (textFieldValue.text.isBlank()) return
        isFreshSearch = true
        keyboardController?.hide()
        viewModel.searchNewPipePage(textFieldValue.text, filter.contentFilter(isChecked))
    }

    LaunchedEffect(key1 = searchPlayListName) {
        if (searchPlayListName is Resource.Success) {
            if (isLoading || isFreshSearch) {
                if (isFreshSearch) {
                    lazyListState.scrollToItem(0)
                    isFreshSearch = false
                    videoListItems = searchPlayListName!!.data!!.youtubeSortedData.youtubeSortedList!!
                } else {
                    videoListItems = videoListItems + searchPlayListName!!.data!!.youtubeSortedData.youtubeSortedList!!
                }
                isLoading = false
                page = searchPlayListName!!.data!!.youtubeSortedData.newPipePage
            }
        } else if (searchPlayListName is Resource.DataError) {
            isLoading = false
            isFreshSearch = false
        }
    }

    LaunchedEffect(lazyListState) {
        snapshotFlow { lazyListState.layoutInfo }
            .collect { layoutInfo ->
                if (layoutInfo.visibleItemsInfo.isNotEmpty()) {
                    val lastVisibleItem = layoutInfo.visibleItemsInfo.last()
                    val totalItems = layoutInfo.totalItemsCount
                    // Load the next page once the end of the list is reached
                    val minimumForPaging = if (isChecked) 10 else 20
                    if (lastVisibleItem.index >= totalItems - 1 && !isLoading && videoListItems.size >= minimumForPaging) {
                        page?.let {
                            if (Page.isValid(it)) {
                                isLoading = true
                                viewModel.searchNewPipeNextPage(it, filter.contentFilter(isChecked), textFieldValue.text)
                            }
                        }
                    }
                }
            }
    }

    if (showPlayListDialog.value && currentSongItemSelected.value != null) {
        PlayListDialogViewer(
            viewModel = viewModel,
            onDismiss = { viewModel.showPlayListDialog(false) },
            currentSongItemSelected.value!!
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DiscoverBackground)
    ) {
        Text(
            text = "Search",
            color = Color.White,
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 4.dp)
        )
        OutlinedTextField(
            value = textFieldValue,
            onValueChange = { textFieldValue = it },
            placeholder = {
                Text(
                    "Search songs, artists, playlists...",
                    color = DiscoverTextSecondary,
                    fontSize = 17.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            leadingIcon = {
                Icon(Icons.Rounded.Search, contentDescription = null, tint = DiscoverTextSecondary, modifier = Modifier.size(24.dp))
            },
            trailingIcon = {
                if (textFieldValue.text.isNotEmpty()) {
                    IconButton(onClick = { textFieldValue = TextFieldValue() }) {
                        Icon(Icons.Rounded.Close, contentDescription = "Clear", tint = Color.White.copy(alpha = 0.8f))
                    }
                }
            },
            singleLine = true,
            textStyle = TextStyle(color = Color.White, fontSize = 18.sp),
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { search() }, onDone = { search() }),
            shape = RoundedCornerShape(28.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = DiscoverAccent,
                unfocusedBorderColor = DiscoverAccent.copy(alpha = 0.75f),
                focusedContainerColor = SearchFieldFill,
                unfocusedContainerColor = SearchFieldFill,
                cursorColor = DiscoverAccent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 12.dp)
                .height(60.dp)
        )

        // Search source toggle
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Switch(
                checked = isChecked,
                onCheckedChange = { checked ->
                    isChecked = checked
                    search()
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = DiscoverAccent,
                    checkedBorderColor = DiscoverAccent,
                    uncheckedThumbColor = Color.White.copy(alpha = 0.8f),
                    uncheckedTrackColor = DiscoverSurface,
                    uncheckedBorderColor = DiscoverBorder
                )
            )
            Column(modifier = Modifier.padding(start = 16.dp)) {
                Text(
                    text = "Search from YouTube videos",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = if (isChecked) "Find and play songs directly from YouTube" else "Turn on to include YouTube videos",
                    color = DiscoverTextSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        if (textFieldValue.text.isEmpty()) {
            CategoryScreenMain()
        } else {
            ChipRow(
                chips = SearchFilter.entries.map { it.label },
                selected = filter.label,
                onSelect = { label ->
                    filter = SearchFilter.entries.first { it.label == label }
                    search()
                }
            )

            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    state = lazyListState,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(videoListItems.size) { index ->
                        videoListItems[index]?.let { item ->
                            SearchResultItem(item, viewModel)
                        }
                    }
                    if (isLoading) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = DiscoverAccent)
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(170.dp)) }
                }
                if (isFreshSearch) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(DiscoverBackground.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = DiscoverAccent)
                    }
                }
            }
        }
    }
}

private val SearchFieldFill = Color(0xFF161314)

/** Search result card: 16:9 thumbnail with duration badge, title, "channel • views", and ⋮. */
@Composable
private fun SearchResultItem(item: TubeFyCoreTypeData, viewModel: TubeFyViewModel) {
    val navController = LocalNavController.current
    val isPlaylist = item.videoId.isEmpty()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(DiscoverSurface)
            .border(1.dp, DiscoverBorder, RoundedCornerShape(14.dp))
            .clickable { openTrack(navController, item) }
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(14.dp))
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(decodeThumpUrl(item.videoImage))
                    .crossfade(true)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .build(),
                contentDescription = item.videoTitle,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            val badge = when {
                isPlaylist -> "Playlist"
                item.durationSeconds > 0 -> formatDuration(item.durationSeconds)
                else -> null
            }
            badge?.let {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(alpha = 0.75f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    if (isPlaylist) {
                        Icon(
                            Icons.AutoMirrored.Rounded.QueueMusic,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .size(14.dp)
                        )
                    }
                    Text(text = it, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 14.dp, end = 4.dp)
        ) {
            Text(
                text = item.videoTitle,
                color = Color.White,
                fontSize = 15.sp,
                lineHeight = 19.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            resultSubtitle(item)?.let {
                Text(
                    text = it,
                    color = DiscoverTextSecondary,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
        Icon(
            Icons.Rounded.MoreVert,
            contentDescription = "More options",
            tint = Color.White.copy(alpha = 0.85f),
            modifier = Modifier
                .padding(end = 6.dp)
                .clip(CircleShape)
                .clickable { viewModel.showPlayListDialog(true, item) }
                .padding(8.dp)
                .size(20.dp)
        )
    }
}

/** "Sun TV • 120M views", skipping whichever parts are unknown. */
private fun resultSubtitle(item: TubeFyCoreTypeData): String? {
    val parts = listOfNotNull(
        item.artist.takeIf { it.isNotBlank() },
        item.viewCount.takeIf { it >= 0 }?.let { "${formatCount(it)} views" }
    )
    return parts.takeIf { it.isNotEmpty() }?.joinToString(" • ")
}

private fun formatCount(count: Long): String = when {
    count >= 1_000_000_000 -> trimDecimal(count / 1_000_000_000.0) + "B"
    count >= 1_000_000 -> trimDecimal(count / 1_000_000.0) + "M"
    count >= 1_000 -> trimDecimal(count / 1_000.0) + "K"
    else -> count.toString()
}

private fun trimDecimal(value: Double): String =
    if (value >= 10) value.toLong().toString() else "%.1f".format(java.util.Locale.US, value).removeSuffix(".0")
