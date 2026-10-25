package com.ramzmania.tubefy.ui.components.screen.search

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MicOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.ramzmania.tubefy.R
import com.ramzmania.tubefy.ui.theme.DiscoverAccent
import com.ramzmania.tubefy.ui.theme.DiscoverAccentContainer
import com.ramzmania.tubefy.ui.theme.DiscoverSurface
import com.ramzmania.tubefy.ui.theme.DiscoverTextSecondary

/**
 * Shown before Identify song until the microphone is allowed: what the feature does and why it needs
 * the mic. After a denial ([denied]) it offers TubeFy's settings, plus [onAllow] again while Android
 * still lets TubeFy ask ([canAskAgain]).
 */
@Composable
internal fun IdentifySongIntroDialog(
    denied: Boolean,
    canAskAgain: Boolean,
    onAllow: () -> Unit,
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(26.dp), color = DiscoverSurface) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(start = 22.dp, end = 22.dp, top = 26.dp, bottom = 14.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .shadow(14.dp, CircleShape, ambientColor = DiscoverAccent, spotColor = DiscoverAccent)
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Color(0xFFFF5A6A), DiscoverAccent)))
                        .border(1.5.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                ) {
                    Image(painterResource(R.drawable.ic_identify_song_symbol), contentDescription = null, modifier = Modifier.size(46.dp))
                }
                Spacer(Modifier.height(18.dp))
                Text("Identify any song", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                Text(
                    "Hear a song playing nearby, on the radio, a TV or another phone? TubeFy listens for a few " +
                        "seconds, tells you its name, singer and movie, and plays it for you.",
                    color = DiscoverTextSecondary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Spacer(Modifier.height(18.dp))

                // Why the microphone, or that it's off
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.06f))
                        .padding(12.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(DiscoverAccentContainer)
                    ) {
                        Icon(if (denied) Icons.Rounded.MicOff else Icons.Rounded.Mic, null, tint = DiscoverAccent, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            if (denied) "Microphone access is off" else "Microphone access needed",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            if (denied) "Turn on Microphone in TubeFy's settings (Permissions) to identify songs."
                            else "Used only while listening, after you tap. The recording stays on your phone.",
                            color = DiscoverTextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
                Spacer(Modifier.height(18.dp))

                if (denied) {
                    PrimaryButton("Go to Settings", onOpenSettings)
                    if (canAskAgain) {
                        TextButton(onClick = onAllow) { Text("Try again", color = Color.White) }
                    }
                } else {
                    PrimaryButton("Allow microphone", onAllow)
                }
                TextButton(onClick = onDismiss) { Text("Not now", color = DiscoverTextSecondary) }
            }
        }
    }
}

@Composable
private fun PrimaryButton(text: String, onClick: () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(DiscoverAccent)
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp)
    ) {
        Text(text, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
    }
}

internal fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

internal fun openAppSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
    runCatching { context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
}
