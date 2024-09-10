package com.ramzmania.tubefy.ui.components.screen.home

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.navigation.NavController
import com.ramzmania.tubefy.R
import com.ramzmania.tubefy.core.extractors.yotubewebextractor.YoutubeScrapType
import com.ramzmania.tubefy.viewmodel.TubeFyViewModel
import com.ramzmania.tubefy.data.dto.home.HomePageResponse
import com.ramzmania.tubefy.data.Resource
import com.ramzmania.tubefy.data.dto.home.youtubei.YoutubeiHomeBaseResponse
import org.schabi.newpipe.extractor.Page


@Composable
fun HomeInitialScreen(viewModel: TubeFyViewModel = hiltViewModel(), navController: NavController?) {
//    var isDataLoaded by rememberSaveable { mutableStateOf(false) }
    var isDefaultDataLoaded by rememberSaveable { mutableStateOf(false) }
    var errorMode by rememberSaveable { mutableStateOf(false) }

//    var isInitialPaginationDataLoaded by rememberSaveable { mutableStateOf(false) }
//    var isInitialPaginationDataLoaded2 by rememberSaveable { mutableStateOf(false) }
    var loadPagination = viewModel.loadMoreHomeData.collectAsState()
    var loadMoreHomePageEnded = viewModel.loadMoreHomePageEnded.collectAsState()


    var visiterData by rememberSaveable {
        mutableStateOf("")
    }
    var paginationId by rememberSaveable {
        mutableStateOf("")
    }
    var paginationHex by rememberSaveable {
        mutableStateOf("")
    }

    var isScrapDataLoaded by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(key1 = loadPagination.value) {

        if (loadPagination.value) {
            if (visiterData.isNotEmpty() && paginationId.isNotEmpty() && paginationHex.isNotEmpty() && !loadMoreHomePageEnded.value) {

                viewModel.callYoutubeiHomePagination(paginationHex, paginationId, visiterData)
            }

        }

    }

    // Trigger loading of default home data
    LaunchedEffect(Unit) {

        /* if(!isScrapDataLoaded) {

             viewModel.startWebScrapping(
                 "https://music.youtube.com/",
                 YoutubeScrapType.YOUTUBE_MUSIC
             )
         }
         if(!isDefaultDataLoaded) {

             viewModel.loadDefaultHomeData()
         }*/
        if (!isDefaultDataLoaded) {

            viewModel.callYoutubeiHome()
        }
    }

    // Observe the data from the ViewModel
    val homeData by viewModel.youTubeMusicHomeDefaultData.observeAsState()
    val scrapData by viewModel.youTubeMusicHomeData.observeAsState()
    val homeTubeiData by viewModel.youTubeiMusicHomeData.observeAsState()
    val homeTubeiPaginationData by viewModel.youTubeiMusicHomePaginationData.observeAsState()


    // Mutable state for the final list to display
    var finalItems by rememberSaveable { mutableStateOf<List<HomePageResponse?>>(emptyList()) }


    // Update finalItems based on homeData
    LaunchedEffect(homeData) {
        if (!isDefaultDataLoaded) {
            if (homeData is Resource.Success) {
                val items = (homeData as Resource.Success<List<HomePageResponse?>>).data
                if (items != null && items.isNotEmpty()) {
                    // Prepend new data to the existing list
                    finalItems = items + finalItems
                    isDefaultDataLoaded = true

                }
            }
        }
    }

    LaunchedEffect(homeTubeiData) {
        if (!isDefaultDataLoaded) {
            if (homeTubeiData is Resource.Success) {
                viewModel.pullToRefreshHome(false)
                val data = (homeTubeiData as Resource.Success<YoutubeiHomeBaseResponse>).data
                if (data != null && data.homePageContentDataList?.isNotEmpty()!!) {
                    isDefaultDataLoaded = true
                    visiterData = data?.paginationContent?.visitorData!!
                    paginationId = data?.paginationContent?.paginationId!!
                    paginationHex = data?.paginationContent?.paginationHex!!
                    // Prepend new data to the existing list
                    finalItems= emptyList()
                    finalItems = data.homePageContentDataList + finalItems
//                    Log.d("datat", "<unda>" + finalItems[0]?.contentData?.get(0)?.title?.trim())
//                    viewModel.callYoutubeiHomePagination(data?.paginationContent?.paginationHex!!,data?.paginationContent?.paginationId!!,data?.paginationContent?.visitorData!!)


                }
                viewModel.setHomePageLoadMoreState(false)
                viewModel.homePagePaginationEnded(false)

            } else if (homeTubeiData is Resource.DataError) {
//                viewModel.l
                viewModel.homePagePaginationEnded(true)
                viewModel.pullToRefreshHome(false)

                if (!isScrapDataLoaded) {

                    viewModel.startWebScrapping(
                        "https://music.youtube.com/",
                        YoutubeScrapType.YOUTUBE_MUSIC
                    )
                }
                if (!isDefaultDataLoaded) {

                    viewModel.loadDefaultHomeData()
                }
            }
        }
    }

    LaunchedEffect(homeTubeiPaginationData) {
        if (!loadMoreHomePageEnded.value&&loadPagination.value) {
            if (homeTubeiPaginationData is Resource.Success) {
                val data =
                    (homeTubeiPaginationData as Resource.Success<YoutubeiHomeBaseResponse>).data
                if (data != null && data.homePageContentDataList?.isNotEmpty()!!) {
                    if (data?.paginationContent?.paginationId != null) {
                        paginationId = data?.paginationContent?.paginationId!!
                        paginationHex = data?.paginationContent?.paginationHex!!
                    } else {
                        paginationId = ""
                        paginationHex = "null"
                        viewModel.homePagePaginationEnded(true)
                    }
                    // Prepend new data to the existing list
                    finalItems = finalItems + data.homePageContentDataList

//                    viewModel.callYoutubeiHomePagination(data?.paginationContent?.paginationHex!!,data?.paginationContent?.paginationId!!,data?.paginationContent?.visitorData!!)

//                    isInitialPaginationDataLoaded = true
//                    viewModel.callYoutubeiHomePagination(data?.paginationContent?.paginationHex!!,data?.paginationContent?.paginationId!!,visiterData)


                }
                viewModel.setHomePageLoadMoreState(false)
            }
        }
    }

    // Update finalItems based on scrapData
    LaunchedEffect(scrapData) {
        if (!isScrapDataLoaded) {
            if (scrapData is Resource.Success) {
                val items = (scrapData as Resource.Success<List<HomePageResponse?>>).data
                if (items != null && items.isNotEmpty()) {
                    // Prepend new data to the existing list
                    finalItems = items + finalItems
                    isScrapDataLoaded = true
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.colorPrimary))
            .wrapContentSize(Alignment.Center)
    ) {
        /*  Text(
              text = "Home View",
              fontWeight = FontWeight.Bold,
              color = Color.White,
              modifier = Modifier.align(Alignment.CenterHorizontally),
              textAlign = TextAlign.Center,
              fontSize = 25.sp
          )

          Spacer(modifier = Modifier.height(16.dp))*/
        if (!isDefaultDataLoaded) {
            if(finalItems.isEmpty()) {
                if (errorMode) {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Error loading data",
                            color = Color.Red,
                            fontSize = 18.sp,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = colorResource(id = R.color.tubefyred))
                    }
                }
            }
        }

        // Show loading indicators or error messages
        val onRefresh: () -> Unit = {

//            Log.d("tadadad","REFRESH CALLED")

            isDefaultDataLoaded=false
            viewModel.callYoutubeiHome()

        }

        // Display the updated list
        if (finalItems.isNotEmpty()) {
            HomePageContentList(homePageResponses = finalItems, navController = navController,onRefresh=onRefresh)
        }
    }
}


@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
//    HomeInitialScreen()
}
