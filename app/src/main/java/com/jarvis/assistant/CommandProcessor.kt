package com.jarvis.assistant

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Patterns
import androidx.core.content.ContextCompat

class CommandProcessor(private val context: Context, private val tts: TTSHelper) {

    fun process(spokenText: String) {
        val text = spokenText.lowercase().trim()

        when {
            text.startsWith("open ") || text.contains("website") -> {
                val target = text.removePrefix("open ").trim()
                openWebsite(target)
            }

            text.startsWith("download ") -> {
                val urlCandidate = extractUrl(text)
                if (urlCandidate != null) {
                    downloadFromUrl(urlCandidate)
                } else {
                    tts.speak("Boss, download karne ke liye poora link boliye.")
                }
            }

            text.startsWith("search file ") || text.startsWith("find file ") -> {
                val query = text.substringAfter("file ").trim()
                searchFile(query)
            }

            text.startsWith("call ") -> {
                val name = text.removePrefix("call ").trim()
                callContact(name)
            }

            text.startsWith("message ") || text.startsWith("whatsapp ") -> {
                val withoutCmd = text.substringAfter(" ").trim()
                val parts = withoutCmd.split(" ", limit = 2)
                if (parts.size == 2) {
                    sendWhatsAppMessage(parts[0], parts[1])
                } else {
                    tts.speak("Boss, naam aur message dono boliye — jaise 'message Rahul kaise ho'")
                }
            }

            else -> {
                tts.speak("Samajh nahi paayi boss. 'Open', 'search file', 'download', 'call', ya 'message' bol kar try kijiye.")
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

    private fun callContact(name: String) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            tts.speak("Boss, mujhe Contacts ki permission nahi mili hai.")
            return
        }
        val number = ContactHelper.findPhoneNumber(context, name)
        if (number == null) {
            tts.speak("Boss, '$name' naam ka contact nahi mila.")
            return
        }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            tts.speak("Boss, mujhe call karne ki permission nahi mili hai.")
            return
        }
        val intent = Intent(Intent.ACTION_CALL).apply {
            data = Uri.parse("tel:$number")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        tts.speak("$name ko call kar rahi hoon, boss.")
    }

    private fun sendWhatsAppMessage(name: String, message: String) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            tts.speak("Boss, mujhe Contacts ki permission nahi mili hai.")
            return
        }
        val number = ContactHelper.findPhoneNumber(context, name)
        if (number == null) {
            tts.speak("Boss, '$name' naam ka contact nahi mila.")
            return
        }
        val cleanNumber = number.replace(Regex("[^0-9+]"), "")
        val encodedMsg = Uri.encode(message)
        val url = "https://wa.me/$cleanNumber?text=$encodedMsg"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
            tts.speak("$name ke liye message taiyar kar diya, boss — bas Send dabana hoga.")
        } catch (e: Exception) {
            tts.speak("WhatsApp nahi khul paaya, boss.")
        }
    }
}
