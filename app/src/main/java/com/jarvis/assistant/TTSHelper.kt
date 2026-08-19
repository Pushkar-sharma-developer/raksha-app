package com.jarvis.assistant

import android.content.Context
import android.media.MediaPlayer
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class TTSHelper(private val context: Context, onReady: () -> Unit = {}) {

    companion object {
        private const val API_KEY = "sk-fish-vbs2VPGM498QHEU3OjExsVbWoaAFTZmU2IqAbebTZHA"
        private const val VOICE_ID = "eb328c330fd74cc88932b78494c3b187"
        private const val API_URL = "https://api.fish.audio/v1/tts"
    }

    private var mediaPlayer: MediaPlayer? = null

    init {
        onReady()
    }

    fun speak(text: String, onDone: (() -> Unit)? = null) {
        Thread {
            try {
                val url = URL(API_URL)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Authorization", "Bearer $API_KEY")
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true
                connection.connectTimeout = 15000
                connection.readTimeout = 15000

                val body = JSONObject().apply {
                    put("text", text)
                    put("reference_id", VOICE_ID)
                    put("format", "mp3")
                }

                connection.outputStream.use { it.write(body.toString().toByteArray()) }

                if (connection.responseCode == 200) {
                    val tempFile = File(context.cacheDir, "raksha_${System.currentTimeMillis()}.mp3")
                    connection.inputStream.use { input ->
                        FileOutputStream(tempFile).use { output -> input.copyTo(output) }
                    }
                    playAudio(tempFile, onDone)
                } else {
                    onDone?.invoke()
                }
            } catch (e: Exception) {
                onDone?.invoke()
            }
        }.start()
    }

    private fun playAudio(file: File, onDone: (() -> Unit)?) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(file.absolutePath)
            setOnCompletionListener {
                it.release()
                file.delete()
                onDone?.invoke()
            }
            setOnErrorListener { mp, _, _ ->
                mp.release()
                onDone?.invoke()
                true
            }
            prepare()
            start()
        }
    }

    fun shutdown() {
        mediaPlayer?.release()
    }
}
