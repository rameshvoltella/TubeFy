/*
package com.ramzmania.tubefy.ui.components.screen.category

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramzmania.tubefy.core.extractors.yotubewebextractor.YoutubeScrapType
import com.ramzmania.tubefy.data.Resource
import com.ramzmania.tubefy.data.dto.base.playlist.PlayListCategory
import com.ramzmania.tubefy.viewmodel.TubeFyViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
@Composable
fun CategoryPlayListMain(viewModel: TubeFyViewModel = hiltViewModel()) {
    val categoryPlayListData by viewModel.youTubeCategoryPlayList.observeAsState()
    var isDefaultDataLoaded by rememberSaveable { mutableStateOf(false) }
    var categoryPlayListItemsList by remember { mutableStateOf<List<PlayListCategory?>>(emptyList()) }

    LaunchedEffect(Unit) {
        if(!isDefaultDataLoaded)
        {
            viewModel.startWebScrapping("https://music.youtube.com/moods_and_genres", YoutubeScrapType.YOUTUBE_MUSIC_CATEGORY)
        }

    }

    LaunchedEffect(key1 = categoryData) {
        if (categoryData is Resource.Success) {
            categoryItemsList = categoryData?.data!!
        }
    }

    if (categoryItemsList.isNotEmpty()) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 150.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categoryItemsList) { categoryItem ->
                CategoryItemCard(categoryItem)
            }
        }
    }
}

@Composable
fun CategoryItemCard(categoryItem: PlayListCategory?) {
    Card(
        modifier = Modifier
//                .padding(8.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp), // Use CardDefaults.cardElevation for elevation
        colors = CardDefaults.cardColors(containerColor = Color.DarkGray), // Set background color
        shape = RoundedCornerShape(8.dp) // Adjust the corner radius for a rounded effect
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            Text(
                text = categoryItem?.playListName.orEmpty(),
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(8.dp),
                textAlign = TextAlign.Center,
                fontSize = 25.sp
            )
        }
    }
}
*/
