package com.jarvis.assistant

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Patterns

/**
 * Turns Raksha's transcribed command text into an actual action.
 * Keyword-based for now — extend patterns here as you add more skills.
 */
class CommandProcessor(private val context: Context, private val tts: TTSHelper) {

    fun process(spokenText: String) {
        val text = spokenText.lowercase().trim()

        when {
            // "open <website>" / "<website> khol do"
            text.startsWith("open ") || text.contains("website") -> {
                val target = text.removePrefix("open ").trim()
                openWebsite(target)
            }

            // "download <url>"
            text.startsWith("download ") -> {
                val urlCandidate = extractUrl(text)
                if (urlCandidate != null) {
                    downloadFromUrl(urlCandidate)
                } else {
                    tts.speak("Boss, download karne ke liye poora link boliye.")
                }
            }

            // "search file <name>" / "find file <name>"
            text.startsWith("search file ") || text.startsWith("find file ") -> {
                val query = text.substringAfter("file ").trim()
                searchFile(query)
            }

            else -> {
                tts.speak("Samajh nahi paayi boss. 'Open', 'search file', ya 'download' bol kar try kijiye.")
            }
        }
    }

    private fun openWebsite(target: String) {
        var url = target.trim()
        if (!url.startsWith("http")) {
            url = if (url.contains(".")) "https://$url" else "https://www.google.com/search?q=$url"
        }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
            tts.speak("Abhi kholti hoon, boss.")
        } catch (e: Exception) {
            tts.speak("Ye nahi khul paaya, boss.")
        }
    }

    private fun extractUrl(text: String): String? {
        val matcher = Patterns.WEB_URL.matcher(text)
        return if (matcher.find()) matcher.group() else null
    }

    private fun downloadFromUrl(url: String) {
        val success = DownloadHelper.downloadFile(context, url)
        tts.speak(if (success) "Download shuru ho gaya, boss." else "Download nahi ho paaya, boss.")
    }

    private fun searchFile(query: String) {
        if (!FileSearchHelper.hasFullAccess()) {
            tts.speak("Boss, pehle 'All files access' ki permission dijiye, tabhi poora phone search kar paaungi.")
            return
        }
        val results = FileSearchHelper.searchByName(query)
        if (results.isEmpty()) {
            tts.speak("'$query' naam ki koi file nahi mili, boss.")
        } else {
            tts.speak("Boss, ${results.size} file mili hain. Pehli hai ${results.first().name}")
        }
    }
}
