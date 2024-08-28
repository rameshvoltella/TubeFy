package com.ramzmania.tubefy.ui.components.screen.player

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
@Composable
fun LoadBitmapAndExtractPalette(imageUrl: String, onPaletteExtracted: (Palette) -> Unit) {
    val context = LocalContext.current
    val imageLoader = remember { ImageLoader.Builder(context).build() }

    LaunchedEffect(imageUrl) {
        val bitmap = withContext(Dispatchers.IO) {
            val request = ImageRequest.Builder(context)
                .data(imageUrl)
                .build()
            val drawable = (imageLoader.execute(request) as? SuccessResult)?.drawable as? BitmapDrawable
            val originalBitmap = drawable?.bitmap

            // Convert HARDWARE bitmap to ARGB_8888 if necessary
            val bitmapToUse = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (originalBitmap?.config == Bitmap.Config.HARDWARE) {
                    originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
                } else {
                    originalBitmap
                }
            } else {
                originalBitmap // No need to check for HARDWARE config on API < 26
            }
            bitmapToUse
        }

        bitmap?.let {
            val palette = Palette.from(it).generate()
            onPaletteExtracted(palette)
        }
    }
}
