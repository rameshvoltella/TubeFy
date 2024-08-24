package com.ramzmania.tubefy.ui.components.screen.category

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.ramzmania.tubefy.R
import com.ramzmania.tubefy.core.YoutubeCoreConstant
import com.ramzmania.tubefy.core.YoutubeCoreConstant.decodeTitle
import com.ramzmania.tubefy.data.dto.youtubemusic.category.MusicCategoryPlayList
import com.ramzmania.tubefy.ui.components.NavigationItem
import com.ramzmania.tubefy.utils.LocalNavController
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun CategoryScreenMain(viewModel: TubeFyViewModel = hiltViewModel()) {
    val categoryData by viewModel.youTubeMusicCategoryData.observeAsState()
    var isDefaultDataLoaded by rememberSaveable { mutableStateOf(false) }
    var categoryItemsList by remember { mutableStateOf<List<PlayListCategory?>>(emptyList()) }
    val navController = LocalNavController.current

    LaunchedEffect(Unit) {
        if (!isDefaultDataLoaded) {
            viewModel.startWebScrapping(
                "https://music.youtube.com/moods_and_genres",
                YoutubeScrapType.YOUTUBE_MUSIC_CATEGORY
            )
        }

    }

    LaunchedEffect(key1 = categoryData) {
        if (categoryData is Resource.Success) {
            isDefaultDataLoaded = true
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
                CategoryItemCard(categoryItem) { selectedItem ->
                    navController!!.navigate(
                        NavigationItem.CategoryPlayList.createRoute(
                            selectedItem.playListBrowserId!!,
                            selectedItem.playListCategoryId!!,
                            URLEncoder.encode(decodeTitle(selectedItem.playListName!!), StandardCharsets.UTF_8.toString())

                        )
                    ) {
                        navController.graph.route?.let { route ->
                            popUpTo(route) {
                                saveState = true
                            }
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
            // Add a Spacer to create additional space at the bottom of the grid
            item {
                Spacer(modifier = Modifier.height(190.dp))
            }
        }
    }else{
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = colorResource(id = R.color.tubefyred))
        }
    }
}

@Composable
fun CategoryItemCard(categoryItem: PlayListCategory?, onClick: (PlayListCategory) -> Unit) {
    Card(
        modifier = Modifier
//                .padding(8.dp)
            .fillMaxWidth()
            .clickable { onClick(categoryItem!!) },
        elevation = CardDefaults.cardElevation(4.dp), // Use CardDefaults.cardElevation for elevation
        colors = CardDefaults.cardColors(containerColor = Color.DarkGray), // Set background color
        shape = RoundedCornerShape(8.dp) // Adjust the corner radius for a rounded effect

    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
        ) {
            // This is the strip at the beginning of the content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp) // Height of the strip
                    .background(Color(categoryItem!!.stripColorCode)) // Change the color as needed
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(categoryItem!!.stripColorCode)),
                verticalArrangement = Arrangement.Center, // Center content vertically
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = decodeTitle(categoryItem?.playListName.orEmpty()),
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    maxLines = 2,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(horizontal = 8.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewCategoryScreenMain() {
    // Sample data to preview the UI
    val sampleCategoryItemsList = listOf(
        PlayListCategory(
            playListName = "Pop",
            playListBrowserId = "1",
            playListCategoryId = "pop",
            stripColorCode = 4278232339

        ),
        PlayListCategory(
            playListName = "Rock \\\u0026 hhhefdasdfasdfdsfvzdfvdad",
            playListBrowserId = "2",
            playListCategoryId = "rockakjdnvkmsadnvkm.ns"
        )
    )

    // Composable function to preview
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 150.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(sampleCategoryItemsList) { categoryItem ->
            CategoryItemCard(categoryItem) { selectedItem ->
                // Mock navigation or other logic here
            }
        }
        // Add a Spacer to create additional space at the bottom of the grid
        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewCategoryItemCard() {
    // Sample data for the preview
    val sampleCategoryItem = PlayListCategory(
        playListName = "Jazz",
        playListBrowserId = "3",
        playListCategoryId = "jazznsdlkjnajsnvljs"
    )

    // Preview the CategoryItemCard with sample data
    CategoryItemCard(sampleCategoryItem) {
        // Handle onClick if needed
    }
}
