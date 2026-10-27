package com.ramzmania.tubefy.core.analytics

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.os.Build

/**
 * Tells analytics which app a share went to (WhatsApp, Telegram…): Android's share menu calls this
 * back only when an app is actually picked, so backing out of the menu isn't counted as a share.
 */
class ShareTracker : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val chosen: ComponentName? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(Intent.EXTRA_CHOSEN_COMPONENT, ComponentName::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(Intent.EXTRA_CHOSEN_COMPONENT)
        }
        Analytics.share(
            contentType = intent.getStringExtra(KEY_TYPE) ?: "unknown",
            itemId = intent.getStringExtra(KEY_ITEM).orEmpty(),
            title = intent.getStringExtra(KEY_TITLE),
            method = chosen?.packageName?.let(::appName) ?: "unknown"
        )
    }

    companion object {
        private const val KEY_TYPE = "content_type"
        private const val KEY_ITEM = "item_id"
        private const val KEY_TITLE = "title"

        /** For Intent.createChooser: reports the picked app as a `share` of [contentType]. */
        fun callback(context: Context, contentType: String, itemId: String, title: String?): IntentSender {
            val intent = Intent(context, ShareTracker::class.java)
                .putExtra(KEY_TYPE, contentType)
                .putExtra(KEY_ITEM, itemId)
                .putExtra(KEY_TITLE, title)
            // Mutable: Android adds the chosen app to it
            val flags = PendingIntent.FLAG_UPDATE_CURRENT or
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0
            return PendingIntent.getBroadcast(context, contentType.hashCode(), intent, flags).intentSender
        }

        /** Short, readable names for the usual apps; others by package. */
        private fun appName(packageName: String) = when {
            packageName.startsWith("com.whatsapp.w4b") -> "whatsapp_business"
            packageName.startsWith("com.whatsapp") -> "whatsapp"
            packageName.startsWith("org.telegram") -> "telegram"
            packageName.startsWith("com.instagram") -> "instagram"
            packageName.startsWith("com.facebook.orca") -> "messenger"
            packageName.startsWith("com.google.android.gm") -> "gmail"
            packageName.startsWith("com.google.android.apps.docs") -> "drive"
            packageName.startsWith("com.android.bluetooth") -> "bluetooth"
            packageName.contains("nearby") || packageName.contains("quickshare") -> "quick_share"
            packageName.contains("messaging") || packageName.contains(".mms") -> "messages"
            else -> packageName
        }
    }
}
