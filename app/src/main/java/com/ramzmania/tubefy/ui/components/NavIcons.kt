package com.ramzmania.tubefy.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
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
