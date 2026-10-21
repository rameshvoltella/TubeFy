package com.ramzmania.tubefy.player

import android.content.Context
import android.hardware.display.DisplayManager
import android.view.Display
import androidx.media3.common.C
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.Tracks

/**
 * Video page default: start each video at its best quality, up to what the screen can show (more than
 * that looks the same and only costs data). The user can still switch to Auto or another quality.
 */
object VideoQuality {

    /** The screen's short side in pixels (1080 on a 1080p phone, 2160 on a 4K TV). */
    fun screenHeight(context: Context): Int = runCatching {
        val display = context.getSystemService(DisplayManager::class.java).getDisplay(Display.DEFAULT_DISPLAY)
        // The display mode gives the panel's real resolution (TVs often render the UI at 1080p on a 4K panel)
        val modes = display.supportedModes.orEmpty()
        modes.maxOfOrNull { minOf(it.physicalWidth, it.physicalHeight) } ?: minOf(display.mode.physicalWidth, display.mode.physicalHeight)
    }.getOrElse {
        val metrics = context.resources.displayMetrics
        minOf(metrics.widthPixels, metrics.heightPixels)
    }

    /**
     * Parameters that pin the best quality of the current video, or null when there's nothing to choose
     * (one quality only) or the user already picked one.
     */
    fun bestOverride(tracks: Tracks, params: TrackSelectionParameters, screenHeight: Int): TrackSelectionParameters? {
        val group = tracks.groups.firstOrNull { it.type == C.TRACK_TYPE_VIDEO } ?: return null
        if (group.length <= 1 || params.overrides.containsKey(group.mediaTrackGroup)) return null
        val index = bestIndex(
            heights = (0 until group.length).map { group.getTrackFormat(it).height },
            supported = (0 until group.length).map { group.isTrackSupported(it) },
            maxHeight = screenHeight
        ) ?: return null
        return params.buildUpon().setOverrideForType(TrackSelectionOverride(group.mediaTrackGroup, index)).build()
    }

    /** The tallest supported quality that fits [maxHeight] (or the smallest one if none fits). */
    internal fun bestIndex(heights: List<Int>, supported: List<Boolean>, maxHeight: Int): Int? {
        val candidates = heights.indices.filter { supported.getOrElse(it) { false } && heights[it] > 0 }
        if (candidates.isEmpty()) return null
        val fitting = candidates.filter { maxHeight <= 0 || heights[it] <= maxHeight }
        return if (fitting.isNotEmpty()) fitting.maxBy { heights[it] } else candidates.minBy { heights[it] }
    }
}
