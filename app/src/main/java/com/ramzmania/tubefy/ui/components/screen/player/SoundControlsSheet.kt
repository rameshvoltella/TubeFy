package com.ramzmania.tubefy.ui.components.screen.player

import com.ramzmania.tubefy.core.analytics.Analytics
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.ElectricBolt
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Nightlife
import androidx.compose.material.icons.rounded.Piano
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Speaker
import androidx.compose.material.icons.rounded.SurroundSound
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Waves
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ramzmania.tubefy.player.SoundEffects
import com.ramzmania.tubefy.ui.theme.DiscoverAccent
import com.ramzmania.tubefy.ui.theme.DiscoverAccentContainer
import com.ramzmania.tubefy.ui.theme.DiscoverBorder
import com.ramzmania.tubefy.ui.theme.DiscoverSurface
import com.ramzmania.tubefy.ui.theme.DiscoverTextSecondary
import kotlin.math.roundToInt

private val AccentLight = Color(0xFFFF5A6A)
private val AccentGradient = Brush.horizontalGradient(listOf(AccentLight, DiscoverAccent))

private class PresetTile(val key: String, val label: String, val icon: ImageVector)

private val PRESET_TILES = listOf(
    PresetTile(SoundEffects.PRESET_CUSTOM, "Custom", Icons.Rounded.GraphicEq),
    PresetTile("pop", "Pop", Icons.Rounded.MusicNote),
    PresetTile("rock", "Rock", Icons.Rounded.ElectricBolt),
    PresetTile("vocal", "Vocal", Icons.Rounded.Mic),
    PresetTile("bass", "Bass Boost", Icons.Rounded.VolumeUp),
    PresetTile("jazz", "Jazz", Icons.Rounded.Nightlife),
    PresetTile("classical", "Classical", Icons.Rounded.Piano),
    PresetTile("electronic", "Electronic", Icons.Rounded.Waves)
)

private class ReverbChip(val key: String, val label: String, val icon: ImageVector)

private val REVERB_CHIPS = listOf(
    ReverbChip(SoundEffects.REVERB_NONE, "None", Icons.Rounded.Block),
    ReverbChip("small_room", "Small Room", Icons.Rounded.Home),
    ReverbChip("hall", "Hall", Icons.Rounded.AccountBalance),
    ReverbChip("concert", "Concert", Icons.Rounded.Groups)
)

/** Whether any sound control is on (the player's Sound button lights up). */
fun SoundEffects.Settings.anyOn() =
    eqEnabled || bassEnabled || virtualizerEnabled || loudnessEnabled || reverb != SoundEffects.REVERB_NONE

/** Sound controls over the player: Equalizer (bands and presets) and Effects, heard live on the song. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoundControlsSheet(onDismiss: () -> Unit) {
    val settings by SoundEffects.settings.collectAsState()
    val capabilities by SoundEffects.capabilities.collectAsState()
    var effectsTab by rememberSaveable { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color(0xFF161314)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(start = 18.dp, end = 18.dp, bottom = 24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("Sound Controls", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                IconButton(
                    onClick = SoundEffects::reset,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.06f))
                ) {
                    Icon(Icons.Rounded.RestartAlt, "Reset", tint = Color.White)
                }
            }
            Spacer(Modifier.height(16.dp))
            Tabs(effectsTab) { effectsTab = it }
            Spacer(Modifier.height(16.dp))
            if (effectsTab) EffectsTab(settings, capabilities) else EqualizerTab(settings, capabilities)
        }
    }
}

@Composable
private fun Tabs(effects: Boolean, onSelect: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(DiscoverSurface)
            .border(1.dp, DiscoverBorder, RoundedCornerShape(28.dp))
            .padding(4.dp)
    ) {
        listOf(false to "Equalizer", true to "Effects").forEach { (tab, label) ->
            val selected = tab == effects
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .then(if (selected) Modifier.background(AccentGradient) else Modifier)
                    .clickable { onSelect(tab) }
                    .padding(vertical = 12.dp)
            ) {
                Text(
                    label,
                    color = if (selected) Color.White else DiscoverTextSecondary,
                    fontSize = 15.sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
                )
            }
        }
    }
}

// --- Equalizer ---

@Composable
private fun EqualizerTab(settings: SoundEffects.Settings, capabilities: SoundEffects.Capabilities) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(DiscoverSurface)
            .border(1.dp, DiscoverBorder, RoundedCornerShape(22.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconCircle(Icons.Rounded.GraphicEq)
            Text("Equalizer", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp))
            AccentSwitch(settings.eqEnabled, onChange = SoundEffects::setEqEnabled)
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            // A flat curve changes nothing: say so
            Text(
                if (settings.eqEnabled && settings.isFlat) "Flat – pick a preset or drag the bands" else "",
                color = DiscoverAccent,
                fontSize = 12.sp,
                modifier = Modifier.weight(1f)
            )
            PresetDropdown(settings.preset)
        }
        Spacer(Modifier.height(8.dp))
        BandSliders(
            curveDb = settings.curveDb,
            maxDb = capabilities.maxDb,
            enabled = settings.eqEnabled,
            onChange = SoundEffects::setBand
        )
    }

    Text("Presets", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 20.dp, bottom = 12.dp))
    PRESET_TILES.chunked(4).forEach { row ->
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)) {
            row.forEach { tile ->
                val selected = settings.eqEnabled && settings.preset == tile.key
                Tile(
                    label = tile.label,
                    icon = tile.icon,
                    selected = selected,
                    onClick = {
                        Analytics.playerFeature("sound_preset", tile.key)
                        SoundEffects.update { it.copy(eqEnabled = true, preset = tile.key) }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun PresetDropdown(preset: String) {
    var open by remember { mutableStateOf(false) }
    Box {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, DiscoverBorder, RoundedCornerShape(20.dp))
                .background(Color.White.copy(alpha = 0.04f))
                .clickable { open = true }
                .padding(start = 14.dp, end = 8.dp, top = 7.dp, bottom = 7.dp)
        ) {
            Text(PRESET_TILES.firstOrNull { it.key == preset }?.label ?: "Custom", color = Color.White, fontSize = 14.sp)
            Icon(Icons.Rounded.KeyboardArrowDown, null, tint = DiscoverTextSecondary, modifier = Modifier.padding(start = 4.dp))
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            PRESET_TILES.forEach { tile ->
                DropdownMenuItem(
                    text = { Text(tile.label) },
                    leadingIcon = { Icon(tile.icon, null) },
                    onClick = {
                        open = false
                        Analytics.playerFeature("sound_preset", tile.key)
                        SoundEffects.update { it.copy(eqEnabled = true, preset = tile.key) }
                    }
                )
            }
        }
    }
}

/** Vertical band sliders over a shaded curve; drag or tap a band to set it. */
@Composable
private fun BandSliders(curveDb: List<Float>, maxDb: Float, enabled: Boolean, onChange: (Int, Float) -> Unit) {
    val bandsHz = SoundEffects.BAND_POINTS_HZ
    val max = maxDb.coerceAtLeast(1f)
    val knobRadius = with(LocalDensity.current) { 11.dp.toPx() }
    val curve by rememberUpdatedState(curveDb)
    val contentAlpha = if (enabled) 1f else 0.45f

    Row(Modifier.fillMaxWidth()) {
        // dB scale
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .height(230.dp)
                .padding(vertical = 4.dp)
        ) {
            listOf("+${max.roundToInt()}", "0", "-${max.roundToInt()}").forEach {
                Text(it, color = DiscoverTextSecondary, fontSize = 12.sp)
            }
        }
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .alpha(contentAlpha)
            ) {
                val bandWidthPx = constraints.maxWidth.toFloat() / bandsHz.size
                val heightPx = constraints.maxHeight.toFloat()
                fun yFor(db: Float) = knobRadius + (1f - (db + max) / (2 * max)) * (heightPx - 2 * knobRadius)
                fun dbFor(y: Float): Float {
                    val fraction = 1f - ((y - knobRadius) / (heightPx - 2 * knobRadius)).coerceIn(0f, 1f)
                    return ((fraction * 2 * max - max) * 2).roundToInt() / 2f
                }

                // Shaded curve through the knobs
                Canvas(Modifier.fillMaxSize()) {
                    val points = curveDb.mapIndexed { i, db -> Offset(bandWidthPx * (i + 0.5f), yFor(db)) }
                    val line = Path().apply {
                        moveTo(points.first().x, points.first().y)
                        for (i in 1 until points.size) {
                            val a = points[i - 1]
                            val b = points[i]
                            val midX = (a.x + b.x) / 2
                            cubicTo(midX, a.y, midX, b.y, b.x, b.y)
                        }
                    }
                    val area = Path().apply {
                        addPath(line)
                        lineTo(points.last().x, size.height)
                        lineTo(points.first().x, size.height)
                        close()
                    }
                    drawPath(area, Brush.verticalGradient(listOf(DiscoverAccent.copy(alpha = 0.22f), Color.Transparent)))
                    drawPath(line, DiscoverAccent.copy(alpha = 0.45f), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx()))
                }

                Row(Modifier.fillMaxSize()) {
                    bandsHz.indices.forEach { band ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .pointerInput(band, max, heightPx) {
                                    detectTapGestures { onChange(band, dbFor(it.y)) }
                                }
                                .pointerInput(band, max, heightPx) {
                                    detectVerticalDragGestures { change, _ ->
                                        change.consume()
                                        val db = dbFor(change.position.y)
                                        if (db != curve[band]) onChange(band, db)
                                    }
                                }
                        ) {
                            Canvas(Modifier.fillMaxSize()) {
                                val cx = size.width / 2
                                val trackWidth = 6.dp.toPx()
                                val knobY = yFor(curveDb[band])
                                // Track
                                drawRoundRect(
                                    color = Color.White.copy(alpha = 0.10f),
                                    topLeft = Offset(cx - trackWidth / 2, knobRadius),
                                    size = Size(trackWidth, size.height - 2 * knobRadius),
                                    cornerRadius = CornerRadius(trackWidth / 2)
                                )
                                // Filled part, from the bottom up to the knob
                                drawRoundRect(
                                    brush = Brush.verticalGradient(listOf(AccentLight, DiscoverAccent.copy(alpha = 0.55f)), startY = knobY, endY = size.height),
                                    topLeft = Offset(cx - trackWidth / 2, knobY),
                                    size = Size(trackWidth, size.height - knobRadius - knobY),
                                    cornerRadius = CornerRadius(trackWidth / 2)
                                )
                                // Knob
                                drawCircle(DiscoverAccent.copy(alpha = 0.35f), radius = knobRadius + 3.dp.toPx(), center = Offset(cx, knobY))
                                drawCircle(Color.White, radius = knobRadius, center = Offset(cx, knobY))
                            }
                        }
                    }
                }
            }
            Row(Modifier.fillMaxWidth()) {
                bandsHz.forEach { hz ->
                    Text(
                        if (hz >= 1000) "${formatK(hz)}K\nHz" else "$hz\nHz",
                        color = DiscoverTextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 15.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .weight(1f)
                            .padding(top = 8.dp)
                    )
                }
            }
        }
    }
}

private fun formatK(hz: Int): String {
    val k = hz / 1000f
    return if (k % 1f == 0f) k.toInt().toString() else "%.1f".format(k)
}

// --- Effects ---

@Composable
private fun EffectsTab(settings: SoundEffects.Settings, capabilities: SoundEffects.Capabilities) {
    EffectCard(
        icon = Icons.Rounded.Speaker,
        title = "Bass Boost",
        subtitle = "Extra low-end punch",
        enabled = settings.bassEnabled,
        onEnabled = { on ->
            if (on) Analytics.playerFeature("sound_effect", "bass_boost")
            SoundEffects.update { it.copy(bassEnabled = on) }
        },
        value = settings.bass,
        onValue = { v -> SoundEffects.update { it.copy(bassEnabled = true, bass = v) } }
    )
    EffectCard(
        icon = Icons.Rounded.SurroundSound,
        title = "Virtualizer",
        subtitle = if (capabilities.virtualizer) "Wider, 3D surround sound" else "Not supported on this phone",
        enabled = settings.virtualizerEnabled && capabilities.virtualizer,
        available = capabilities.virtualizer,
        onEnabled = { on ->
            if (on) Analytics.playerFeature("sound_effect", "virtualizer")
            SoundEffects.update { it.copy(virtualizerEnabled = on) }
        },
        value = settings.virtualizer,
        onValue = { v -> SoundEffects.update { it.copy(virtualizerEnabled = true, virtualizer = v) } }
    )
    EffectCard(
        icon = Icons.Rounded.GraphicEq,
        title = "Loudness Boost",
        subtitle = "Clearer and louder sound",
        enabled = settings.loudnessEnabled,
        onEnabled = { on ->
            if (on) Analytics.playerFeature("sound_effect", "loudness")
            SoundEffects.update { it.copy(loudnessEnabled = on) }
        },
        value = settings.loudness,
        onValue = { v -> SoundEffects.update { it.copy(loudnessEnabled = true, loudness = v) } }
    )
    // Reverb only where the phone supports it
    if (capabilities.reverb) {
        val reverbOn = settings.reverb != SoundEffects.REVERB_NONE
        EffectCardFrame {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconCircle(Icons.Rounded.Waves)
                Column(Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)) {
                    Text("Reverb", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                    Text("Add space to your sound", color = DiscoverTextSecondary, fontSize = 13.sp)
                }
                AccentSwitch(reverbOn) { on ->
                    SoundEffects.update { it.copy(reverb = if (on) "small_room" else SoundEffects.REVERB_NONE) }
                }
            }
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                REVERB_CHIPS.forEach { chip ->
                    Tile(
                        label = chip.label,
                        icon = chip.icon,
                        selected = settings.reverb == chip.key,
                        outlined = true,
                        onClick = {
                            if (chip.key != SoundEffects.REVERB_NONE) Analytics.playerFeature("sound_effect", "reverb_${chip.key}")
                            SoundEffects.update { it.copy(reverb = chip.key) }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun EffectCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    enabled: Boolean,
    onEnabled: (Boolean) -> Unit,
    value: Float,
    onValue: (Float) -> Unit,
    available: Boolean = true
) {
    EffectCardFrame {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconCircle(icon)
            Column(Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)) {
                Text(title, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                Text(subtitle, color = DiscoverTextSecondary, fontSize = 13.sp)
            }
            AccentSwitch(enabled, enabled = available, onChange = onEnabled)
        }
        Slider(
            value = value,
            onValueChange = onValue,
            enabled = available,
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = if (enabled) DiscoverAccent else DiscoverAccent.copy(alpha = 0.4f),
                inactiveTrackColor = Color.White.copy(alpha = 0.10f),
                activeTickColor = Color.Transparent,
                inactiveTickColor = Color.Transparent
            ),
            modifier = Modifier.padding(top = 6.dp)
        )
        Row(Modifier.fillMaxWidth()) {
            Text("0", color = DiscoverTextSecondary, fontSize = 12.sp, modifier = Modifier.weight(1f))
            Text("Max", color = DiscoverTextSecondary, fontSize = 12.sp)
        }
    }
}

@Composable
private fun EffectCardFrame(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(DiscoverSurface)
            .border(1.dp, DiscoverBorder, RoundedCornerShape(22.dp))
            .padding(16.dp)
    ) { content() }
}

// --- Shared pieces ---

@Composable
private fun IconCircle(icon: ImageVector) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(DiscoverAccentContainer)
    ) {
        Icon(icon, null, tint = DiscoverAccent, modifier = Modifier.size(24.dp))
    }
}

@Composable
private fun AccentSwitch(checked: Boolean, enabled: Boolean = true, onChange: (Boolean) -> Unit) {
    Switch(
        checked = checked,
        onCheckedChange = onChange,
        enabled = enabled,
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = DiscoverAccent,
            checkedBorderColor = DiscoverAccent,
            uncheckedThumbColor = Color.White.copy(alpha = 0.8f),
            uncheckedTrackColor = Color.White.copy(alpha = 0.12f),
            uncheckedBorderColor = Color.Transparent
        )
    )
}

@Composable
private fun Tile(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    outlined: Boolean = false
) {
    val shape = RoundedCornerShape(16.dp)
    val background by animateColorAsState(
        when {
            selected && !outlined -> DiscoverAccent
            selected -> DiscoverAccentContainer
            else -> Color.White.copy(alpha = 0.05f)
        },
        label = "tile"
    )
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .then(if (selected && !outlined) Modifier.shadow(10.dp, shape, ambientColor = DiscoverAccent, spotColor = DiscoverAccent) else Modifier)
            .clip(shape)
            .background(if (selected && !outlined) AccentGradient else Brush.linearGradient(listOf(background, background)))
            .border(
                if (selected && outlined) 1.5.dp else 1.dp,
                if (selected && outlined) DiscoverAccent else DiscoverBorder,
                shape
            )
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp)
    ) {
        Icon(icon, null, tint = if (selected) Color.White else Color.White.copy(alpha = 0.8f), modifier = Modifier.size(24.dp))
        Text(
            label,
            color = Color.White,
            fontSize = 12.sp,
            maxLines = 1,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}
