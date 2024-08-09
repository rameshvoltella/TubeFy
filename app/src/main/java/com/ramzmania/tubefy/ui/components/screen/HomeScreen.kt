package com.ramzmania.tubefy.ui.components.screen
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ramzmania.tubefy.R
import com.ramzmania.tubefy.core.YoutubeCoreConstant
import com.ramzmania.tubefy.data.dto.base.BaseContentData
import com.ramzmania.tubefy.data.dto.home.CellType
import com.ramzmania.tubefy.data.dto.home.HomePageResponse

@Composable
fun HomePageContentList(homePageResponses: List<HomePageResponse?>) {

    LazyColumn {
        items(homePageResponses) { response ->

            response?.let {
                when (it.cellType) {
                    CellType.LIST -> VerticalContentList(contentData = it.contentData)
                    CellType.HORIZONTAL_LIST -> HorizontalContentList(contentData = it.contentData)
                    CellType.THREE_TYPE_CELL -> VerticalContentList(contentData = it.contentData)
                    CellType.SINGLE_CELL -> SingleContentCell(contentData = it.contentData)
                    CellType.PLAYLIST_ONLY -> HorizontalContentList(contentData = it.contentData)
                }
            }
        }
    }
}

@Composable
fun VerticalContentList(contentData: List<BaseContentData>?) {
    contentData?.let {
        Column {
            it.forEach { data ->
                ContentItem(data = data)
            }
        }
    }
}

@Composable
fun HorizontalContentList(contentData: List<BaseContentData>?) {
    contentData?.let {
        LazyRow {
            items(it) { data ->
                ContentItem(data = data)
            }
        }
    }
}

@Composable
fun SingleContentCell(contentData: List<BaseContentData>?) {
    contentData?.firstOrNull()?.let { data ->
        ContentItem(data = data)
    }
}
@Composable
fun ContentItem(data: BaseContentData) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
    ) {
        Log.d("incoming", "" + data.thumbnail)

        data.thumbnail?.let { thumbnailUrl ->
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(YoutubeCoreConstant.decodeThumpUrl(thumbnailUrl))
                    .crossfade(true)
                    .placeholder(R.drawable.tubefy_icon)
                    .error(R.drawable.tubefy_icon)
                    .build(),
                contentDescription = "Drawable Image",
                modifier = Modifier
                    .padding(20.dp)
                    .height(200.dp)
                    .width(200.dp),
                contentScale = ContentScale.Crop
            )
        } ?: AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(R.drawable.tubefy_icon)
                .build(),
            contentDescription = "Placeholder Image",
            modifier = Modifier
                .padding(20.dp)
                .height(200.dp)
                .width(200.dp),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun Content2Item(data: BaseContentData) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
    ) {
        Log.d("incomming",""+data.thumbnail)
        data.thumbnail?.let {
//            Image(
//                painter = rememberImagePainter(data = it),
//                contentDescription = data.title,
//                modifier = Modifier
//                    .height(150.dp)
//                    .fillMaxWidth()
//            )
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(YoutubeCoreConstant.decodeThumpUrl(it))
                    .crossfade(true)
                    .build(),
                contentDescription = "Drawable Image",
                modifier = Modifier
                    .padding(20.dp)
                    .height(200.dp)
                    .width(200.dp),
                contentScale = ContentScale.Crop
            )
        }
        data.title?.let {
            Text(
                text = it,
                modifier = Modifier.padding(top = 8.dp),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(id = R.color.purple_700),
//                style = MaterialTheme.typography.h6
            )
        }
    }
}
@Preview(showBackground = true)
@Composable
fun HomePageContentListPreview() {
    val videoSortedList = mutableListOf<HomePageResponse>()

    val mockContentData = listOf(
        BaseContentData(
            playlistId = "",
            videoId = "",
            title = "Song Title 1",
            thumbnail = "https://via.placeholder.com/150"
        ),
        BaseContentData(
            playlistId = "",
            videoId = "",
            title = "Song Title 1",
            thumbnail = "https://via.placeholder.com/150"
        ),
        BaseContentData(
            playlistId = "",
            videoId = "",
            title = "Song Title 1",
            thumbnail = "https://via.placeholder.com/150"
        )
    )
    videoSortedList.add(HomePageResponse(CellType.HORIZONTAL_LIST,mockContentData))
    videoSortedList.add(HomePageResponse(CellType.HORIZONTAL_LIST,mockContentData))

    HomePageContentList(homePageResponses = videoSortedList)
}