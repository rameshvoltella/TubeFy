package com.ramzmania.tubefy.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ramzmania.tubefy.ui.theme.DiscoverAccent

data class BottomTab(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val icon: ImageVector
)

/** Sizes of the bar; [compact] (landscape) is shorter and drops the labels. */
private data class BarDims(
    val barHeight: Dp,
    val bumpHeight: Dp,
    val bumpHalfWidth: Dp,
    val corner: Dp,
    val circle: Dp,
    val showLabels: Boolean
)

private val RegularDims = BarDims(72.dp, 20.dp, 48.dp, 24.dp, 50.dp, showLabels = true)
private val CompactDims = BarDims(54.dp, 12.dp, 36.dp, 20.dp, 40.dp, showLabels = false)

private val BarColor = Color(0xFF141214)
private val BarBorder = Color.White.copy(alpha = 0.09f)
private val UnselectedColor = Color.White.copy(alpha = 0.78f)
private val SelectedIconTint = Color(0xFFFFA3AD)

/**
 * Floating bottom bar: the selected tab rises into a bump in the bar's outline and shows its
 * icon in a glowing circle. The bump slides to the newly selected tab.
 */
@Composable
fun FloatingBottomBar(
    tabs: List<BottomTab>,
    selectedRoute: String?,
    onSelect: (BottomTab) -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val dims = if (compact) CompactDims else RegularDims
    val density = LocalDensity.current
    val selectedIndex = tabs.indexOfFirst { it.route == selectedRoute }
    var widthPx by remember { mutableFloatStateOf(0f) }
    val bumpCenter = remember { Animatable(0f) }
    val bumpVisible by animateFloatAsState(if (selectedIndex >= 0) 1f else 0f, label = "bumpVisible")

    LaunchedEffect(selectedIndex, widthPx) {
        if (selectedIndex < 0 || widthPx == 0f) return@LaunchedEffect
        val target = widthPx * (selectedIndex + 0.5f) / tabs.size
        if (bumpCenter.value == 0f) {
            bumpCenter.snapTo(target)
        } else {
            bumpCenter.animateTo(target, spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMediumLow))
        }
    }

    Box(
        modifier = modifier
            .navigationBarsPadding()
            .padding(start = 12.dp, end = 12.dp, bottom = 8.dp)
            .fillMaxWidth()
            .height(dims.barHeight + dims.bumpHeight)
            .onSizeChanged { widthPx = it.width.toFloat() }
            .drawBehind {
                val path = barPath(
                    size = size,
                    barTop = dims.bumpHeight.toPx(),
                    bumpRise = dims.bumpHeight.toPx() * bumpVisible,
                    centerX = bumpCenter.value,
                    halfWidth = dims.bumpHalfWidth.toPx(),
                    corner = dims.corner.toPx()
                )
                drawPath(path, BarColor)
                drawPath(path, BarBorder, style = Stroke(width = with(density) { 1.dp.toPx() }))
            }
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            tabs.forEachIndexed { index, tab ->
                BottomTabItem(
                    tab = tab,
                    dims = dims,
                    selected = index == selectedIndex,
                    onClick = { onSelect(tab) },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }
        }
    }
}

@Composable
private fun BottomTabItem(
    tab: BottomTab,
    dims: BarDims,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lift by animateDpAsState(if (selected) (-4).dp else 0.dp, label = "tabLift")
    val labelColor by animateColorAsState(if (selected) DiscoverAccent else UnselectedColor, label = "tabLabel")

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(bottom = if (dims.showLabels) 10.dp else (dims.barHeight - dims.circle) / 2)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .offset(y = lift)
                .size(dims.circle)
        ) {
            if (selected) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(dims.circle)
                        .shadow(18.dp, CircleShape, ambientColor = DiscoverAccent, spotColor = DiscoverAccent)
                        .clip(CircleShape)
                        .background(Brush.radialGradient(listOf(Color(0xFF5C1A26), Color(0xFF2A0C12))))
                        .border(2.dp, DiscoverAccent, CircleShape)
                ) {
                    Icon(tab.selectedIcon, contentDescription = tab.label, tint = SelectedIconTint, modifier = Modifier.size(26.dp))
                }
            } else {
                Icon(tab.icon, contentDescription = tab.label, tint = UnselectedColor, modifier = Modifier.size(26.dp))
            }
        }
        if (dims.showLabels) Text(
            text = tab.label,
            color = labelColor,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            maxLines = 1,
            modifier = Modifier.offset(y = (-2).dp)
        )
    }
}

/** Rounded bar whose top edge rises in a smooth bump centred on [centerX]. */
private fun barPath(
    size: Size,
    barTop: Float,
    bumpRise: Float,
    centerX: Float,
    halfWidth: Float,
    corner: Float
): Path = Path().apply {
    val width = size.width
    val bottom = size.height
    val hasBump = bumpRise > 0.5f
    val left = (centerX - halfWidth).coerceAtLeast(0f)
    val right = (centerX + halfWidth).coerceAtMost(width)
    val peak = barTop - bumpRise
    // When the bump reaches an end tab, tighten that top corner so the bump keeps its shape
    val topLeftCorner = if (hasBump) corner.coerceAtMost(left).coerceAtLeast(2f) else corner
    val topRightCorner = if (hasBump) corner.coerceAtMost(width - right).coerceAtLeast(2f) else corner

    moveTo(0f, barTop + topLeftCorner)
    quadraticTo(0f, barTop, topLeftCorner, barTop)
    if (hasBump) {
        lineTo(left, barTop)
        cubicTo(left + (centerX - left) * 0.5f, barTop, centerX - (centerX - left) * 0.45f, peak, centerX, peak)
        cubicTo(centerX + (right - centerX) * 0.45f, peak, right - (right - centerX) * 0.5f, barTop, right, barTop)
    }
    lineTo(width - topRightCorner, barTop)
    quadraticTo(width, barTop, width, barTop + topRightCorner)
    lineTo(width, bottom - corner)
    quadraticTo(width, bottom, width - corner, bottom)
    lineTo(corner, bottom)
    quadraticTo(0f, bottom, 0f, bottom - corner)
    close()
}
