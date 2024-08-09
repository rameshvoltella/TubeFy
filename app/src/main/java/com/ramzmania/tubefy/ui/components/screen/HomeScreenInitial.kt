package com.ramzmania.tubefy.ui.components.screen

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramzmania.tubefy.R
import com.ramzmania.tubefy.core.extractors.yotubewebextractor.YoutubeScrapType
import com.ramzmania.tubefy.viewmodel.TubeFyViewModel
import com.ramzmania.tubefy.data.dto.home.HomePageResponse
import com.ramzmania.tubefy.data.Resource
@Composable
fun HomeInitialScreen(viewModel: TubeFyViewModel = hiltViewModel()) {
    // Trigger loading of default home data
    LaunchedEffect(Unit) {
        viewModel.startWebScrapping("https://music.youtube.com/", YoutubeScrapType.YOUTUBE_MUSIC)
        viewModel.loadDefaultHomeData()
    }

    // Observe the data from the ViewModel
    val homeData by viewModel.youTubeMusicHomeDefaultData.observeAsState()
    val scrapData by viewModel.youTubeMusicHomeData.observeAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.colorPrimary))
            .wrapContentSize(Alignment.Center)
    ) {
        Text(
            text = "Home View",
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center,
            fontSize = 25.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        when (homeData) {
            is Resource.Loading -> {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
            is Resource.Success -> {
                val items = (homeData as Resource.Success<List<HomePageResponse?>>).data
                if (items != null && items.isNotEmpty()) {
                    // Show the list of items
                    HomePageContentList(homePageResponses = items)
                } else {
                    // Show error if the list is empty
                    Text(
                        text = "No data available",
                        color = Color.Red,
                        fontSize = 18.sp,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
            is Resource.DataError -> {
                Text(
                    text = "Error loading data",
                    color = Color.Red,
                    fontSize = 18.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
            else -> {}
        }

        when (scrapData) {
            is Resource.Loading -> {
//                CircularProgressIndicator(
//                    color = Color.White,
//                    modifier = Modifier.align(Alignment.CenterHorizontally)
//                )
            }
            is Resource.Success -> {
                val items = (homeData as Resource.Success<List<HomePageResponse?>>).data
                if (items != null && items.isNotEmpty()) {
                    // Show the list of items
                    HomePageContentList(homePageResponses = items)
                } else {
                    // Show error if the list is empty
                    Text(
                        text = "No data available",
                        color = Color.Red,
                        fontSize = 18.sp,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
            is Resource.DataError -> {
//                Text(
//                    text = "Error loading data",
//                    color = Color.Red,
//                    fontSize = 18.sp,
//                    modifier = Modifier.align(Alignment.CenterHorizontally)
//                )
            }
            else -> {}
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeInitialScreen()
}
