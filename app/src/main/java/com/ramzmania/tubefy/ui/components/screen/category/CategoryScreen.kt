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
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
    val categoryItemsList = (categoryData as? Resource.Success)?.data.orEmpty()
    val navController = LocalNavController.current

    LaunchedEffect(Unit) {
        viewModel.loadMusicCategories()
    }

    if (categoryItemsList.isNotEmpty()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = "Browse by mood",
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp)
                )
            }
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
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    } else if (categoryData is Resource.DataError) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Couldn't load categories",
                color = Color.White,
                fontSize = 16.sp
            )
            TextButton(onClick = { viewModel.loadMusicCategories(forceRefresh = true) }) {
                Text(text = "Retry", color = colorResource(id = R.color.tubefyred), fontSize = 16.sp)
            }
        }
    } else {
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
    val item = categoryItem ?: return
    val name = decodeTitle(item.playListName.orEmpty())
    val style = categoryStyle(name)
    val shape = RoundedCornerShape(18.dp)

    Column(
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(84.dp)
            .clip(shape)
            .drawBehind {
                drawRect(Brush.linearGradient(listOf(style.start, style.end), start = Offset.Zero, end = Offset(size.width, size.height)))
                // Soft lighter wave across the top-right corner
                val wave = Path().apply {
                    moveTo(size.width * 0.45f, 0f)
                    cubicTo(
                        size.width * 0.7f, size.height * 0.35f,
                        size.width * 0.85f, size.height * 0.15f,
                        size.width, size.height * 0.55f
                    )
                    lineTo(size.width, 0f)
                    close()
                }
                drawPath(wave, Color.White.copy(alpha = 0.10f))
                // Slight darkening toward the bottom for depth
                drawRect(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.18f))))
            }
            .clickable { onClick(item) }
            .padding(horizontal = 18.dp)
    ) {
        Text(
            text = name,
            color = Color.White,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = style.subtitle,
            color = Color.White.copy(alpha = 0.82f),
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

private data class CategoryStyle(val start: Color, val end: Color, val subtitle: String)

private val CategoryPalette = listOf(
    Color(0xFFB06AF0) to Color(0xFF4B2BB8), // purple
    Color(0xFFF08A2C) to Color(0xFF8E3B2A), // orange
    Color(0xFFFF7A45) to Color(0xFFC2183F), // red-orange
    Color(0xFF2BB58A) to Color(0xFF0E6E6A), // green
    Color(0xFF3AA0F0) to Color(0xFF1858A8), // blue
    Color(0xFF4552D0) to Color(0xFF3A1A8C), // indigo
    Color(0xFFE95BB0) to Color(0xFF8E1FB8), // magenta
    Color(0xFFF0435E) to Color(0xFFA8203F), // pink-red
    Color(0xFFE0A93A) to Color(0xFF8A5A14), // gold
    Color(0xFF2AAFA8) to Color(0xFF0F5E6E), // teal
)

/** Colour + subtitle per category (moods & main languages as designed; others cycle the palette). */
private val KnownCategories = mapOf(
    "chill" to (0 to "Relax and unwind"),
    "commute" to (1 to "Music for the road"),
    "energize" to (2 to "Workout & get moving"),
    "feel good" to (3 to "Positive vibes only"),
    "focus" to (4 to "Stay in the zone"),
    "gaming" to (5 to "Play with rhythm"),
    "party" to (6 to "Turn up the energy"),
    "romance" to (7 to "Love is in the air"),
    "sad" to (5 to "Let it all out"),
    "sleep" to (4 to "Drift off peacefully"),
    "workout" to (2 to "Push your limits"),
    "malayalam" to (9 to "Best of Mollywood"),
    "tamil" to (1 to "Best of Kollywood"),
    "telugu" to (6 to "Best of Tollywood"),
    "hindi" to (8 to "Best of Bollywood"),
    "kannada" to (3 to "Best of Sandalwood"),
    "punjabi" to (2 to "Bhangra and beyond"),
)

private fun categoryStyle(name: String): CategoryStyle {
    val key = name.trim().lowercase().replace('-', ' ')
    val (paletteIndex, subtitle) = KnownCategories[key]
        ?: ((key.hashCode() and Int.MAX_VALUE) % CategoryPalette.size to "Explore playlists")
    val (start, end) = CategoryPalette[paletteIndex]
    return CategoryStyle(start, end, subtitle)
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
