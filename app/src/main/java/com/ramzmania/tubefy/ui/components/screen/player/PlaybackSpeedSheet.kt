package com.ramzmania.tubefy.ui.components.screen.player

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ramzmania.tubefy.ui.theme.DiscoverAccent
import com.ramzmania.tubefy.ui.theme.DiscoverBorder
import com.ramzmania.tubefy.ui.theme.DiscoverTextSecondary
import kotlin.math.abs
import kotlin.math.roundToInt

private val SPEED_STEPS = listOf(0.5f, 0.75f, 1f, 1.25f, 1.5f, 2f)

/** "1.25×", "1×". */
fun formatSpeed(speed: Float): String {
    val rounded = (speed * 100).roundToInt() / 100f
    val text = if (rounded % 1f == 0f) rounded.toInt().toString() else rounded.toString().trimEnd('0')
    return "$text×"
}

/** Playback speed: quick steps and a fine slider (0.5× – 2×); the pitch stays the same. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaybackSpeedSheet(speed: Float, onSpeed: (Float) -> Unit, onDismiss: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color(0xFF121011)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 24.dp, end = 24.dp, bottom = 20.dp)
        ) {
            Text("Playback speed", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(
                if (speed == 1f) "Normal" else "Songs play at ${formatSpeed(speed)}, at the same pitch",
                color = if (speed == 1f) DiscoverTextSecondary else DiscoverAccent,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                formatSpeed(speed),
                color = Color.White,
                fontSize = 44.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 14.dp)
            )
            Slider(
                value = speed,
                onValueChange = { onSpeed((it * 20).roundToInt() / 20f) },
                valueRange = 0.5f..2f,
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = DiscoverAccent,
                    inactiveTrackColor = Color.White.copy(alpha = 0.10f),
                    activeTickColor = Color.Transparent,
                    inactiveTickColor = Color.Transparent
                )
            )
            Row(Modifier.fillMaxWidth()) {
                Text("0.5×", color = DiscoverTextSecondary, fontSize = 12.sp, modifier = Modifier.weight(1f))
                Text("2×", color = DiscoverTextSecondary, fontSize = 12.sp)
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp)
            ) {
                SPEED_STEPS.forEach { step ->
                    val selected = abs(step - speed) < 0.001f
                    val shape = RoundedCornerShape(14.dp)
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(shape)
                            .background(
                                if (selected) Brush.horizontalGradient(listOf(Color(0xFFFF5A6A), DiscoverAccent))
                                else Brush.linearGradient(listOf(Color.White.copy(alpha = 0.05f), Color.White.copy(alpha = 0.05f)))
                            )
                            .border(1.dp, if (selected) Color.Transparent else DiscoverBorder, shape)
                            .clickable { onSpeed(step) }
                            .padding(vertical = 12.dp)
                    ) {
                        Text(
                            formatSpeed(step),
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
            }
            if (speed != 1f) {
                TextButton(onClick = { onSpeed(1f) }, modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp)) {
                    Text("Back to normal", color = DiscoverAccent, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
