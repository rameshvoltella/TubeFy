package com.ramzmania.tubefy.ui.components.screen.search

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextField

import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramzmania.tubefy.data.Resource
import com.ramzmania.tubefy.data.dto.searchformat.TubeFyCoreTypeData
import com.ramzmania.tubefy.ui.components.screen.album.TrackItem
import com.ramzmania.tubefy.viewmodel.TubeFyViewModel
import org.schabi.newpipe.extractor.Page

@Composable
fun AudioSearchScreen(viewModel: TubeFyViewModel = hiltViewModel()) {
    var text by remember { mutableStateOf(TextFieldValue()) }
    val searchPlayListName by viewModel.youTubeSearchData.observeAsState()
    var isLoading by remember { mutableStateOf(false) }  // Track loading state
    var finalItems by remember { mutableStateOf<List<TubeFyCoreTypeData?>>(emptyList()) }
    var page by remember { mutableStateOf<Page?>(null) }
    val lazyListState = rememberLazyListState()
    LaunchedEffect(key1 = searchPlayListName) {
        if (searchPlayListName is Resource.Success) {
//            val items = (streamUrlData as Resource.Success<StreamUrlData>).data
            // Prepend new data to the existing list
            Log.d("datata", "first")
            Log.d(
                "datata",
                ">>VADAAACAME" + searchPlayListName!!.data!!.youtubeSortedData.youtubeSortedList!!.size
            )
            if (finalItems.size > 0) {
                finalItems =
                    finalItems + searchPlayListName!!.data!!.youtubeSortedData.youtubeSortedList!!
            } else {
                finalItems = searchPlayListName!!.data!!.youtubeSortedData.youtubeSortedList!!
            }
            isLoading = false
            page = searchPlayListName!!.data!!.youtubeSortedData.newPipePage

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
                        if (finalItems.size >= 20) {
                            isLoading = true
                            if (page != null && Page.isValid(page)) {
                                Log.d("unda", "yessssss" + page?.url)
                                viewModel.searchNewPipeNextPage(page!!) // Implement this function in your ViewModel
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
        Text(
            text = "OLAKKA", color = Color.White,
        )
        TextField(
            value = text,
            placeholder = { Text("Search songs") },
            onValueChange = { newText -> text = newText },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = {
                // Handle text input
                viewModel.searchNewPipePage(
                    text.text,
                    mutableListOf("music_songs")
                )
            }),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            textStyle = androidx.compose.material3.MaterialTheme.typography.bodyLarge.copy(
                color = Color.Red
            )
//                .border(1.dp, MaterialTheme.colors.onSurface)
        )

        // LazyColumn for the list
        LazyColumn(
            state = lazyListState,
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(finalItems.size) { index ->
                finalItems[index]?.let { item ->
                    TrackItem(item)
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
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}




