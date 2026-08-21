package com.jarvis.assistant

import android.content.Context
import android.content.Intent

object AppLauncherHelper {
    private val knownApps = mapOf(
        "whatsapp" to "com.whatsapp",
        "youtube" to "com.google.android.youtube",
        "instagram" to "com.instagram.android",
        "facebook" to "com.facebook.katana",
        "chrome" to "com.android.chrome",
        "camera" to "com.android.camera",
        "gallery" to "com.google.android.apps.photos",
        "gmail" to "com.google.android.gm",
        "maps" to "com.google.android.apps.maps",
        "settings" to "com.android.settings",
        "play store" to "com.android.vending",
        "telegram" to "org.telegram.messenger",
        "spotify" to "com.spotify.music"
    )

    fun launchApp(context: Context, spokenName: String): Boolean {
        val key = spokenName.trim().lowercase()
        val packageName = knownApps[key]
            ?: knownApps.entries.firstOrNull { key.contains(it.key) }?.value
            ?: return false

        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            ?: return false

        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(launchIntent)
        return true
    }
}
