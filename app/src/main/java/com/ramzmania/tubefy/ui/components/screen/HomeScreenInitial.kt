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

    // Mutable state for the final list to display
    var finalItems by remember { mutableStateOf<List<HomePageResponse?>>(emptyList()) }

    // Update finalItems based on homeData
    LaunchedEffect(homeData) {
        if (homeData is Resource.Success) {
            val items = (homeData as Resource.Success<List<HomePageResponse?>>).data
            if (items != null && items.isNotEmpty()) {
                // Prepend new data to the existing list
                finalItems = items + finalItems

            }
        }
    }

    // Update finalItems based on scrapData
    LaunchedEffect(scrapData) {
        if (scrapData is Resource.Success) {
            val items = (scrapData as Resource.Success<List<HomePageResponse?>>).data
            if (items != null && items.isNotEmpty()) {
                // Prepend new data to the existing list
                finalItems = items + finalItems
            }
        }
    }

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

        // Show loading indicators or error messages
        if (homeData is Resource.Loading || scrapData is Resource.Loading) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else if (homeData is Resource.DataError || scrapData is Resource.DataError) {
            Text(
                text = "Error loading data",
                color = Color.Red,
                fontSize = 18.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        // Display the updated list
        if (finalItems.isNotEmpty()) {
            HomePageContentList(homePageResponses = finalItems)
        }
    }
}


@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeInitialScreen()
}
