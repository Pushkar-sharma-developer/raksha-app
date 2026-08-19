package com.jarvis.assistant

import android.content.Context
import android.os.Environment
import java.io.File

/**
 * Searches the phone's storage for files whose name contains the given
 * keyword. Requires MANAGE_EXTERNAL_STORAGE ("All files access") to be
 * granted for a full-phone search — otherwise it falls back to the
 * app-visible folders only (Downloads, Documents, etc).
 */
object FileSearchHelper {

    fun searchByName(query: String, maxResults: Int = 20): List<File> {
        val results = mutableListOf<File>()
        val root = Environment.getExternalStorageDirectory()
        searchRecursive(root, query.lowercase(), results, maxResults)
        return results
    }

    private fun searchRecursive(
        dir: File,
        query: String,
        results: MutableList<File>,
        maxResults: Int
    ) {
        if (results.size >= maxResults) return
        val files = dir.listFiles() ?: return

        for (file in files) {
            if (results.size >= maxResults) return
            // Skip Android's protected/system dirs to avoid permission errors
            if (file.name == "Android" || file.isHidden) continue

            if (file.isDirectory) {
                searchRecursive(file, query, results, maxResults)
            } else if (file.name.lowercase().contains(query)) {
                results.add(file)
            }
        }
    }

    fun hasFullAccess(): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            true
        }
    }
