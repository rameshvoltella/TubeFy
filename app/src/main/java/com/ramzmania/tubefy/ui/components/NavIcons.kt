package com.ramzmania.tubefy.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val LibraryIconFilled: ImageVector by lazy { buildLibraryIcon(filled = true) }
val LibraryIconOutlined: ImageVector by lazy { buildLibraryIcon(filled = false) }

/** "Your Library" icon: two upright books and one leaning book; [filled] for the selected tab. */
private fun buildLibraryIcon(filled: Boolean): ImageVector = ImageVector.Builder(
    name = if (filled) "LibraryFilled" else "LibraryOutlined",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    val fill = if (filled) SolidColor(Color.Black) else null
    fun book(block: androidx.compose.ui.graphics.vector.PathBuilder.() -> Unit) = path(
        fill = fill,
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.8f,
        strokeLineJoin = StrokeJoin.Round,
        pathBuilder = block
    )
    // Upright book 1
    book {
        moveTo(4f, 4f); lineTo(7.2f, 4f); lineTo(7.2f, 20f); lineTo(4f, 20f); close()
    }
    // Upright book 2
    book {
        moveTo(9.6f, 4f); lineTo(12.8f, 4f); lineTo(12.8f, 20f); lineTo(9.6f, 20f); close()
    }
    // Leaning book
    book {
        moveTo(14.6f, 5.3f); lineTo(17.6f, 4.3f); lineTo(21.4f, 18.9f); lineTo(18.4f, 19.9f); close()
    }
}.build()

val HomeIconFilled: ImageVector by lazy { buildHomeIcon(filled = true) }
val HomeIconOutlined: ImageVector by lazy { buildHomeIcon(filled = false) }
val SearchIconBold: ImageVector by lazy { buildSearchIcon(strokeWidth = 2.6f) }
val SearchIconRegular: ImageVector by lazy { buildSearchIcon(strokeWidth = 1.8f) }

/** Rounded house with a door cut-out; solid when selected, outlined otherwise. */
private fun buildHomeIcon(filled: Boolean): ImageVector = ImageVector.Builder(
    name = if (filled) "HomeFilled" else "HomeOutlined",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path(
        fill = if (filled) SolidColor(Color.Black) else null,
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.8f,
        strokeLineJoin = StrokeJoin.Round,
        strokeLineCap = StrokeCap.Round
    ) {
        moveTo(3.8f, 10.2f)
        lineTo(12f, 3.6f)
        lineTo(20.2f, 10.2f)
        lineTo(20.2f, 19f)
        quadTo(20.2f, 20.4f, 18.8f, 20.4f)
        lineTo(14.6f, 20.4f)
        lineTo(14.6f, 15f)
        quadTo(14.6f, 14.2f, 13.8f, 14.2f)
        lineTo(10.2f, 14.2f)
        quadTo(9.4f, 14.2f, 9.4f, 15f)
        lineTo(9.4f, 20.4f)
        lineTo(5.2f, 20.4f)
        quadTo(3.8f, 20.4f, 3.8f, 19f)
        close()
    }
}.build()

/** Magnifier with a rounded handle; a bolder stroke marks the selected tab. */
private fun buildSearchIcon(strokeWidth: Float): ImageVector = ImageVector.Builder(
    name = "Search$strokeWidth",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    // Lens
    path(stroke = SolidColor(Color.Black), strokeLineWidth = strokeWidth) {
        moveTo(17.2f, 10.6f)
        arcToRelative(6.6f, 6.6f, 0f, true, true, -13.2f, 0f)
        arcToRelative(6.6f, 6.6f, 0f, true, true, 13.2f, 0f)
        close()
    }
    // Handle
    path(stroke = SolidColor(Color.Black), strokeLineWidth = strokeWidth + 0.4f, strokeLineCap = StrokeCap.Round) {
        moveTo(15.6f, 15.6f)
        lineTo(20.2f, 20.2f)
    }
}.build()
