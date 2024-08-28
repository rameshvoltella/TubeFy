package com.ramzmania.tubefy.ui.components.screen.search

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField

import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramzmania.tubefy.R
import com.ramzmania.tubefy.data.Resource
import com.ramzmania.tubefy.data.dto.base.searchformat.TubeFyCoreTypeData
import com.ramzmania.tubefy.ui.components.screen.album.TrackItem
import com.ramzmania.tubefy.ui.components.screen.category.CategoryScreenMain
import com.ramzmania.tubefy.viewmodel.TubeFyViewModel
import org.schabi.newpipe.extractor.Page

@Composable
fun AudioSearchScreen(viewModel: TubeFyViewModel = hiltViewModel()) {
    val textFieldValueSaver = Saver<TextFieldValue, String>(
        save = { it.text },  // Save only the text part
        restore = { TextFieldValue(it) }  // Restore TextFieldValue from the text
    )

    // Use rememberSaveable with the custom Saver
    var textFieldValue by rememberSaveable(stateSaver = textFieldValueSaver) {
        mutableStateOf(TextFieldValue())
    }
//    var text by remember { mutableStateOf(TextFieldValue()) }
//    var text by rememberSaveable { mutableStateOf("") }
    val searchPlayListName by viewModel.youTubeSearchData.observeAsState()
    var isLoading by rememberSaveable { mutableStateOf(false) }  // Track loading state
    var videoListItems by rememberSaveable { mutableStateOf<List<TubeFyCoreTypeData?>>(emptyList()) }
    var page by rememberSaveable { mutableStateOf<Page?>(null) }
    val lazyListState = rememberLazyListState()
    var isChecked by rememberSaveable { mutableStateOf(false) }
    var isFreshSearch by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(key1 = searchPlayListName) {
        if (searchPlayListName is Resource.Success) {
//            val items = (streamUrlData as Resource.Success<StreamUrlData>).data
            // Prepend new data to the existing list
            if (isLoading || isFreshSearch) {
                Log.d("datata", "first")
                Log.d(
                    "datata",
                    ">>VADAAACAME" + searchPlayListName!!.data!!.youtubeSortedData.youtubeSortedList!!.size
                )
                if (isFreshSearch) {
                    lazyListState.scrollToItem(0) // Scroll to top without animation
                    isFreshSearch = false
                    videoListItems =
                        searchPlayListName!!.data!!.youtubeSortedData.youtubeSortedList!!
                } else {
                    if (videoListItems.size > 0) {
                        videoListItems =
                            videoListItems + searchPlayListName!!.data!!.youtubeSortedData.youtubeSortedList!!
                    } else {
                        videoListItems =
                            searchPlayListName!!.data!!.youtubeSortedData.youtubeSortedList!!

                    }
                }
                isLoading = false
                page = searchPlayListName!!.data!!.youtubeSortedData.newPipePage
            }

        } else if (searchPlayListName is Resource.DataError) {
            isLoading = false

        }

    }

    LaunchedEffect(lazyListState) {
        snapshotFlow { lazyListState.layoutInfo }
            .collect { layoutInfo ->
                if (layoutInfo.visibleItemsInfo.isNotEmpty()) {
                    val lastVisibleItem = layoutInfo.visibleItemsInfo.last()
                    val totalItems = layoutInfo.totalItemsCount
                    // Check if we've reached near the end of the list
                    if (lastVisibleItem.index >= totalItems - 1 && !isLoading) {
                        // Trigger loading more items
                        if (isChecked) {
                            if (videoListItems.size >= 10) {
                                isLoading = true
                                page?.let {
                                    if (Page.isValid(page)) {
                                        viewModel.searchNewPipeNextPage(
                                            page,
                                            if (isChecked) mutableListOf("all") else mutableListOf("music_songs"),
                                            textFieldValue.text
                                        ) // Implement this function in your ViewModel
                                    }
                                }

                            }
                        } else {
                            if (videoListItems.size >= 20) {
                                isLoading = true
                                page?.let {
                                    if (Page.isValid(page)) {
                                        viewModel.searchNewPipeNextPage(
                                            page,
                                            if (isChecked) mutableListOf("all") else mutableListOf("music_songs"),
                                            textFieldValue.text
                                        ) // Implement this function in your ViewModel
                                    }
                                }

                            }
                        }
                    }
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Top TextField
        val keyboardController = LocalSoftwareKeyboardController.current
        TextField(
            value = textFieldValue,
            placeholder = { Text("Search songs") },
            onValueChange = { newValue -> textFieldValue = newValue },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = {
                // Handle text input
                isFreshSearch = true
                keyboardController?.hide() // Hide the keyboard
                viewModel.searchNewPipePage(
                    textFieldValue.text,
                    if (isChecked) mutableListOf("all") else mutableListOf("music_songs")
                )


            }),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            textStyle = androidx.compose.material3.MaterialTheme.typography.bodyLarge.copy(
                color = Color.Red
            )
                 ,   trailingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.close), // Replace with your icon resource
                    contentDescription = "Search Icon",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
                            // Handle icon click if needed
                            textFieldValue = textFieldValue.copy(text = "") // Clear the text
                        }
                )
            }
//                .border(1.dp, MaterialTheme.colors.onSurface)
        )
        Row(modifier = Modifier.padding(20.dp)) {
            Switch(
                checked = isChecked,
                onCheckedChange = { checked ->
                    run {
                        isChecked = checked
                        if (textFieldValue.text.isNotEmpty()) {
                            isFreshSearch = true
                            viewModel.searchNewPipePage(
                                textFieldValue.text,
                                if (isChecked) mutableListOf("all") else mutableListOf("music_songs")
                            )
                        }
                    }
                },
                modifier = Modifier.padding(horizontal = 5.dp)
            )

            // Display the current state
            Text(
                text = if (isChecked) "Audio from youtube video is enabled" else "Turn on to search from youtube videos",
                fontWeight = FontWeight.Thin,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(horizontal = 5.dp),
                textAlign = TextAlign.Center,
                fontSize = 16.sp
            )

        }
        if (textFieldValue.text.isEmpty()) {
            CategoryScreenMain()
        }
        if (isFreshSearch) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        // LazyColumn for the list
        LazyColumn(
            state = lazyListState,
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(videoListItems.size) { index ->
                videoListItems[index]?.let { item ->
                    TrackItem(item)
                }
            }
            item {
                Spacer(modifier = Modifier.height(90.dp))
            }
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}




