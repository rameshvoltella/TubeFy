package com.ramzmania.tubefy.ui.tv

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlaylistPlay
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ramzmania.tubefy.R
import com.ramzmania.tubefy.data.youtube.YouTubeItem
import com.ramzmania.tubefy.ui.components.screen.youtube.formatDuration
import com.ramzmania.tubefy.ui.components.screen.youtube.stats
import com.ramzmania.tubefy.ui.theme.DiscoverAccent
import com.ramzmania.tubefy.ui.theme.DiscoverSurface
import com.ramzmania.tubefy.ui.theme.DiscoverTextSecondary

internal val TvLiveRed = Color(0xFFE5202E)

/** A video card for the remote: grows a little and gets a red ring while focused; OK opens it. */
@Composable
internal fun TvVideoCard(item: YouTubeItem, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val scale by animateFloatAsState(if (focused) 1.06f else 1f, label = "cardScale")
    Column(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(14.dp))
            .background(if (focused) Color(0xFF241A1C) else Color.Transparent)
            .border(3.dp, if (focused) DiscoverAccent else Color.Transparent, RoundedCornerShape(14.dp))
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(10.dp))
                .background(DiscoverSurface)
        ) {
            AsyncImage(
                model = item.thumbnail.ifBlank { null } ?: R.drawable.placeholder,
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            val badge = when {
                item.isLive -> "LIVE"
                item.isPlaylist -> if (item.videoCount > 0) "${item.videoCount} videos" else "Playlist"
                item.durationSeconds > 0 -> formatDuration(item.durationSeconds)
                else -> null
            }
            badge?.let {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (item.isLive) TvLiveRed else Color.Black.copy(alpha = 0.8f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    if (item.isPlaylist) Icon(Icons.Rounded.PlaylistPlay, null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Text(it, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        Text(
            item.title,
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 19.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 8.dp, start = 2.dp, end = 2.dp)
        )
        val meta = listOfNotNull(item.channel.ifBlank { null }, stats(item)).joinToString(" • ")
        if (meta.isNotBlank()) Text(
            meta,
            color = DiscoverTextSecondary,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 2.dp, start = 2.dp, end = 2.dp)
        )
    }
}

/** A side-menu row: highlighted while focused, red text while it's the open section. */
@Composable
internal fun TvMenuItem(
    label: String,
    icon: ImageVector?,
    selected: Boolean,
    onFocus: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    if (focused) androidx.compose.runtime.LaunchedEffect(Unit) { onFocus() }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .background(
                when {
                    focused -> Color.White
                    selected -> DiscoverAccent.copy(alpha = 0.22f)
                    else -> Color.Transparent
                }
            )
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 11.dp)
    ) {
        val tint = when {
            focused -> Color.Black
            selected -> DiscoverAccent
            else -> Color.White.copy(alpha = 0.85f)
        }
        icon?.let { Icon(it, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp).padding(end = 0.dp)) }
        Text(
            label,
            color = tint,
            fontSize = 16.sp,
            fontWeight = if (selected || focused) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = if (icon != null) 14.dp else 0.dp)
        )
    }
}

/** A round button on the player bar: white when focused. */
@Composable
internal fun TvRoundButton(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    big: Boolean = false,
    label: String? = null
) {
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val scale by animateFloatAsState(if (focused) 1.12f else 1f, label = "buttonScale")
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .scale(scale)
                .size(if (big) 64.dp else 52.dp)
                .clip(RoundedCornerShape(50))
                .background(if (focused) Color.White else Color.White.copy(alpha = 0.16f))
                .clickable(interactionSource = interaction, indication = null, onClick = onClick)
        ) {
            Icon(icon, contentDescription = description, tint = if (focused) Color.Black else Color.White, modifier = Modifier.size(if (big) 36.dp else 28.dp))
        }
        label?.let {
            Text(it, color = if (focused) Color.White else DiscoverTextSecondary, fontSize = 12.sp, maxLines = 1, modifier = Modifier.padding(top = 6.dp))
        }
    }
}
