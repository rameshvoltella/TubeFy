package com.ramzmania.tubefy.ui.components.screen
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
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
                    CellType.PLAYLIST_ONLY -> VerticalContentList(contentData = it.contentData)
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
                    .data(it)
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
//                style = MaterialTheme.typography.h6
            )
        }
    }
}
