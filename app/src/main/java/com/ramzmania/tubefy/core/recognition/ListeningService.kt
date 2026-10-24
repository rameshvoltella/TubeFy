package com.ramzmania.tubefy.core.recognition

import android.Manifest
import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.ramzmania.tubefy.R
import com.ramzmania.tubefy.ui.components.HomeActivity
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Keeps the microphone working while "Identify song" listens, even after the user switches to another
 * app (Android silences the microphone of background apps without a microphone foreground service).
 * The listening itself stays in RecognitionViewModel; when it ends with TubeFy in the background,
 * [showResult] posts the outcome as a notification that opens TubeFy on the result.
 */
class ListeningService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createChannels(this)
        val notification = NotificationCompat.Builder(this, LISTENING_CHANNEL)
            .setSmallIcon(R.drawable.ic_notification_player_icon)
            .setContentTitle("Identifying song…")
            .setContentText("TubeFy is listening")
            .setContentIntent(openApp(this))
            .setOngoing(true)
            .setSilent(true)
            .build()
        runCatching {
            ServiceCompat.startForeground(this, LISTENING_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
        }.onFailure { stopSelf() }
        return START_NOT_STICKY
    }

    companion object {
        private const val LISTENING_CHANNEL = "tubefy_listening"
        private const val RESULT_CHANNEL = "tubefy_identified"
        private const val LISTENING_ID = 7301
        private const val RESULT_ID = 7302

        fun start(context: Context) {
            runCatching { ContextCompat.startForegroundService(context, Intent(context, ListeningService::class.java)) }
            NotificationManagerCompat.from(context).cancel(RESULT_ID)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, ListeningService::class.java))
        }

        /** Ends listening; if TubeFy isn't on screen, tells the user how it went. */
        suspend fun showResult(context: Context, song: RecognizedSong?, failure: String? = null) {
            val inBackground = !isInForeground()
            stop(context)
            if (!inBackground || !canNotify(context)) return
            createChannels(context)
            val builder = NotificationCompat.Builder(context, RESULT_CHANNEL)
                .setSmallIcon(R.drawable.ic_notification_player_icon)
                .setContentIntent(openApp(context))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
            if (song != null) {
                val details = listOf(song.artist, song.album, song.year).filter { !it.isNullOrBlank() }.joinToString(" · ")
                builder.setContentTitle(song.title)
                    .setContentText(details.ifBlank { "Tap to play it in TubeFy" })
                    .setSubText("Song found")
                    .setLargeIcon(cover(context, song.coverUrl))
            } else {
                builder.setContentTitle("Couldn't identify the song")
                    .setContentText(failure ?: "Tap to open TubeFy and try again")
            }
            NotificationManagerCompat.from(context).notify(RESULT_ID, builder.build())
        }

        private fun isInForeground(): Boolean {
            val info = ActivityManager.RunningAppProcessInfo()
            ActivityManager.getMyMemoryState(info)
            return info.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
        }

        private fun canNotify(context: Context) = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

        private suspend fun cover(context: Context, url: String?): Bitmap? {
            if (url.isNullOrBlank()) return null
            val request = ImageRequest.Builder(context).data(url).allowHardware(false).build()
            val result = withTimeoutOrNull(5_000) { context.imageLoader.execute(request) }
            return ((result as? SuccessResult)?.drawable as? BitmapDrawable)?.bitmap
        }

        /** Brings TubeFy back as it was, where the result sheet shows the song. */
        private fun openApp(context: Context): PendingIntent {
            val intent = Intent(context, HomeActivity::class.java)
                .setAction(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_LAUNCHER)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
            return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        }

        private fun createChannels(context: Context) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
            val manager = context.getSystemService(NotificationManager::class.java)
            if (manager.getNotificationChannel(LISTENING_CHANNEL) == null) {
                manager.createNotificationChannel(NotificationChannel(LISTENING_CHANNEL, "Identifying songs", NotificationManager.IMPORTANCE_LOW))
            }
            if (manager.getNotificationChannel(RESULT_CHANNEL) == null) {
                manager.createNotificationChannel(NotificationChannel(RESULT_CHANNEL, "Identified songs", NotificationManager.IMPORTANCE_HIGH))
            }
        }
    }
}
