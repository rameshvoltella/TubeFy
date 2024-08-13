package com.ramzmania.tubefy.ui.components.screen.search

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text

import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
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
    var isLoading by remember { mutableStateOf(true) }  // Track loading state
    var finalItems by remember { mutableStateOf<List<TubeFyCoreTypeData?>>(emptyList()) }
    var page by remember { mutableStateOf<Page?>(null) }
    LaunchedEffect(key1 = searchPlayListName) {
        if (searchPlayListName is Resource.Success) {
//            val items = (streamUrlData as Resource.Success<StreamUrlData>).data
            // Prepend new data to the existing list
            Log.d("datata", "first")
            Log.d(
                "datata",
                ">>VADAAACAME" + searchPlayListName!!.data!!.youtubeSortedData.youtubeSortedList!!.size
            )
            finalItems = searchPlayListName!!.data!!.youtubeSortedData.youtubeSortedList!!
            isLoading = false
            page = searchPlayListName!!.data!!.youtubeSortedData.newPipePage

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
        BasicTextField(
            value = text,
            onValueChange = { newText -> text = newText },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = {
                // Handle text input
                viewModel.searchNewPipePage(
                    text.text,
                    mutableListOf()
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
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(finalItems.size) { index ->
                finalItems[index]?.let { item ->
                    TrackItem(item)
                }
            }
        }
    }
}




