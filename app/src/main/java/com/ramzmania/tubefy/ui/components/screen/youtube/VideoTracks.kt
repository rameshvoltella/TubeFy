package com.ramzmania.tubefy.ui.components.screen.youtube

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.C
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.Tracks
import com.ramzmania.tubefy.ui.theme.DiscoverAccent
import com.ramzmania.tubefy.ui.theme.DiscoverTextSecondary
import java.util.Locale

/** One choice in a track sheet; [apply] turns the current parameters into the ones that select it. */
class TrackOption(
    val label: String,
    val selected: Boolean,
    val apply: (TrackSelectionParameters) -> TrackSelectionParameters
)

enum class TrackSheet(val title: String) { Quality("Quality"), Audio("Audio language"), Captions("Captions") }

/** Qualities, highest first, after "Auto" (adapts to the connection). */
fun qualityOptions(tracks: Tracks, params: TrackSelectionParameters): List<TrackOption> {
    val group = tracks.groups.firstOrNull { it.type == C.TRACK_TYPE_VIDEO } ?: return emptyList()
    if (group.length <= 1) return emptyList()
    val override = params.overrides[group.mediaTrackGroup]
    val playingHeight = (0 until group.length).firstOrNull { group.isTrackSelected(it) }?.let { group.getTrackFormat(it).height }
        ?.takeIf { it > 0 }
    val auto = TrackOption(
        label = if (override == null && playingHeight != null) "Auto (${playingHeight}p)" else "Auto",
        selected = override == null,
        apply = { it.buildUpon().clearOverridesOfType(C.TRACK_TYPE_VIDEO).build() }
    )
    val qualities = (0 until group.length)
        .filter { group.isTrackSupported(it) }
        .sortedByDescending { group.getTrackFormat(it).height }
        .map { index ->
            val format = group.getTrackFormat(index)
            TrackOption(
                label = "${format.height}p" + if (format.frameRate >= 49f) "60" else "",
                selected = override?.trackIndices?.contains(index) == true,
                apply = { it.buildUpon().setOverrideForType(TrackSelectionOverride(group.mediaTrackGroup, index)).build() }
            )
        }
    return listOf(auto) + qualities
}

/** The video's audio languages (only when it has more than one). */
fun audioOptions(tracks: Tracks): List<TrackOption> {
    val groups = tracks.groups.filter { it.type == C.TRACK_TYPE_AUDIO }
    if (groups.size <= 1) return emptyList()
    return groups.mapIndexed { i, group ->
        val format = group.getTrackFormat(0)
        TrackOption(
            label = format.label ?: format.language?.let { Locale.forLanguageTag(it).displayName } ?: "Track ${i + 1}",
            selected = group.isSelected,
            apply = { it.buildUpon().setOverrideForType(TrackSelectionOverride(group.mediaTrackGroup, 0)).build() }
        )
    }
}

/** "Off" plus every caption language. */
fun captionOptions(tracks: Tracks, params: TrackSelectionParameters): List<TrackOption> {
    val groups = tracks.groups.filter { it.type == C.TRACK_TYPE_TEXT }
    if (groups.isEmpty()) return emptyList()
    val textOff = C.TRACK_TYPE_TEXT in params.disabledTrackTypes || groups.none { it.isSelected }
    val off = TrackOption("Off", textOff) { it.buildUpon().clearOverridesOfType(C.TRACK_TYPE_TEXT).setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true).build() }
    return listOf(off) + groups.mapIndexed { i, group ->
        val format = group.getTrackFormat(0)
        TrackOption(
            label = format.label ?: format.language?.let { Locale.forLanguageTag(it).displayName } ?: "Captions ${i + 1}",
            selected = !textOff && group.isSelected,
            apply = {
                it.buildUpon()
                    .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
                    .setOverrideForType(TrackSelectionOverride(group.mediaTrackGroup, 0))
                    .build()
            }
        )
    }
}

/** The CC button: turns captions off, or on in the phone's language (else English, else the first). */
fun toggleCaptions(tracks: Tracks, params: TrackSelectionParameters): TrackSelectionParameters? {
    val options = captionOptions(tracks, params)
    if (options.isEmpty()) return null
    if (!options.first().selected) return options.first().apply(params)
    val groups = tracks.groups.filter { it.type == C.TRACK_TYPE_TEXT }
    val phone = Locale.getDefault().language
    val pick = groups.indexOfFirst { it.getTrackFormat(0).language?.startsWith(phone) == true }.takeIf { it >= 0 }
        ?: groups.indexOfFirst { it.getTrackFormat(0).language?.startsWith("en") == true }.takeIf { it >= 0 }
        ?: 0
    return options[pick + 1].apply(params)
}

/** The label shown in the menu for the current choice, e.g. "Auto (720p)", "Hindi", "Off". */
fun List<TrackOption>.currentLabel(): String? = firstOrNull { it.selected }?.label

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackSheetView(sheet: TrackSheet, options: List<TrackOption>, onPick: (TrackOption) -> Unit, onDismiss: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color(0xFF161314)
    ) {
        Column(modifier = Modifier.navigationBarsPadding().padding(bottom = 12.dp)) {
            Text(
                sheet.title,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 8.dp)
            )
            LazyColumn(modifier = Modifier.heightIn(max = 420.dp)) {
                items(options) { option ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPick(option) }
                            .padding(horizontal = 24.dp, vertical = 14.dp)
                    ) {
                        if (option.selected) {
                            Icon(Icons.Rounded.Check, contentDescription = "Selected", tint = DiscoverAccent, modifier = Modifier.size(22.dp))
                        } else {
                            Spacer(Modifier.width(22.dp))
                        }
                        Text(
                            option.label,
                            color = if (option.selected) Color.White else Color.White.copy(alpha = 0.85f),
                            fontSize = 16.sp,
                            fontWeight = if (option.selected) FontWeight.SemiBold else FontWeight.Normal,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }
            if (sheet == TrackSheet.Quality) Text(
                "Auto picks the best quality for your connection.",
                color = DiscoverTextSecondary,
                fontSize = 13.sp,
                modifier = Modifier.padding(start = 62.dp, end = 24.dp, top = 4.dp)
            )
        }
    }
}
