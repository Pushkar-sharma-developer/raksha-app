package com.jarvis.assistant

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment

object DownloadHelper {

    fun downloadFile(context: Context, url: String): Boolean {
        return try {
            val request = DownloadManager.Request(Uri.parse(url))
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    Uri.parse(url).lastPathSegment ?: "raksha_download_${System.currentTimeMillis()}"
                )
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

            val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            dm.enqueue(request)
            true
        } catch (e: Exception) {
            false
        }
    }
}
