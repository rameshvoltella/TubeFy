package com.ramzmania.tubefy.ui.components.screen.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.ramzmania.tubefy.core.YoutubeCoreConstant
import com.ramzmania.tubefy.data.dto.base.playlist.PlayListCategory
import com.ramzmania.tubefy.ui.components.NavigationItem
import com.ramzmania.tubefy.ui.components.ChipRow
import com.ramzmania.tubefy.ui.components.navigateToTab
import com.ramzmania.tubefy.ui.components.screen.category.openCategoryPlaylistItem
import com.ramzmania.tubefy.ui.theme.DiscoverAccent
import com.ramzmania.tubefy.ui.theme.DiscoverBackground
import com.ramzmania.tubefy.ui.theme.DiscoverBorder
import com.ramzmania.tubefy.ui.theme.DiscoverOnAccent
import com.ramzmania.tubefy.ui.theme.DiscoverSurface
import com.ramzmania.tubefy.ui.theme.DiscoverTextSecondary
import com.ramzmania.tubefy.utils.LocalNavController
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

    val categoryData by viewModel.youTubeMusicCategoryData.observeAsState()
    val categories = (categoryData as? Resource.Success)?.data.orEmpty()
    val chips = remember(categories) { buildHomeChips(categories) }
    var selectedChip by rememberSaveable { mutableStateOf(FOR_YOU) }
    val selectedCategory = chips.firstOrNull { it.label == selectedChip }?.category

    LaunchedEffect(Unit) {
        viewModel.loadMusicCategories()
    }
    LaunchedEffect(selectedCategory) {
        selectedCategory?.let { viewModel.loadHomeCategory(it) }
    }
    // Back from a language / mood chip returns to "For you" instead of leaving the app
    BackHandler(enabled = selectedChip != FOR_YOU) {
        selectedChip = FOR_YOU
    }

    val onRefresh: () -> Unit = {
        isDefaultDataLoaded = false
        viewModel.callYoutubeiHome()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DiscoverBackground)
    ) {
        DiscoverHeader(
            onSearchClick = { navController?.navigateToTab(NavigationItem.Search.route) },
            onProfileClick = { navController?.navigateToTab(NavigationItem.Profile.route) }
        )
        ChipRow(
            chips = chips.map { it.label },
            selected = selectedChip,
            onSelect = { selectedChip = it }
        )

        Box(modifier = Modifier.weight(1f)) {
            if (selectedCategory == null) {
                when {
                    finalItems.isNotEmpty() ->
                        HomePageContentList(homePageResponses = finalItems, navController = navController, onRefresh = onRefresh)

                    errorMode -> CenteredMessage("Error loading data")

                    else -> CenteredLoader()
                }
            } else {
                HomeCategoryContent(viewModel = viewModel, category = selectedCategory)
            }
        }
    }
}

private const val FOR_YOU = "For you"

/** Language & mood chips shown after "For you", matched by name against YouTube Music's categories. */
private val HOME_CHIP_CATEGORIES = listOf(
    "Malayalam", "Tamil", "Hindi", "Telugu", "Kannada", "Punjabi",
    "Romance", "Chill", "Party", "Workout"
)

private data class HomeChip(val label: String, val category: PlayListCategory?)

private fun buildHomeChips(categories: List<PlayListCategory?>): List<HomeChip> {
    val byName = categories.filterNotNull().associateBy {
        YoutubeCoreConstant.decodeTitle(it.playListName.orEmpty()).trim().lowercase()
    }
    return listOf(HomeChip(FOR_YOU, null)) + HOME_CHIP_CATEGORIES.mapNotNull { name ->
        byName[name.lowercase()]?.let { HomeChip(name, it) }
    }
}

@Composable
private fun DiscoverHeader(onSearchClick: () -> Unit, onProfileClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 12.dp, top = 12.dp, bottom = 8.dp)
    ) {
        Text(
            text = "Discover",
            color = Color.White,
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onSearchClick) {
            Icon(Icons.Rounded.Search, contentDescription = "Search", tint = Color.White, modifier = Modifier.size(28.dp))
        }
        Spacer(modifier = Modifier.width(6.dp))
        Image(
            painter = painterResource(id = R.drawable.tubefy_icon),
            contentDescription = "Settings",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .border(1.dp, DiscoverBorder, CircleShape)
                .clickable(onClick = onProfileClick)
        )
    }
}

/** The playlists of a language / mood category, same data the Search tab's category grid opens. */
@Composable
private fun HomeCategoryContent(viewModel: TubeFyViewModel, category: PlayListCategory) {
    val navController = LocalNavController.current
    val content by viewModel.homeCategoryContent.collectAsState()
    when (val result = content[category.playListCategoryId]) {
        is Resource.Success -> {
            val sections = result.data.orEmpty().filterNotNull().filter { it.musicCategoryPlayList.isNotEmpty() }
            if (sections.isEmpty()) {
                CenteredMessage("Nothing to show here yet")
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(sections) { section ->
                        SectionHeader(section.plaListBaseName)
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(section.musicCategoryPlayList) { item ->
                                ArtworkCard(
                                    imageUrl = item.playListThump,
                                    title = item.playListName,
                                    onClick = { openCategoryPlaylistItem(navController, item) }
                                )
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }
        }

        is Resource.DataError -> Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Couldn't load ${category.playListName.orEmpty()}", color = Color.White, fontSize = 16.sp)
            TextButton(onClick = { viewModel.loadHomeCategory(category, forceRefresh = true) }) {
                Text("Retry", color = DiscoverAccent, fontSize = 16.sp)
            }
        }

        else -> CenteredLoader()
    }
}

@Composable
private fun CenteredLoader() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = DiscoverAccent)
    }
}

@Composable
private fun CenteredMessage(text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = text, color = DiscoverTextSecondary, fontSize = 16.sp)
    }
}


@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
//    HomeInitialScreen()
}
