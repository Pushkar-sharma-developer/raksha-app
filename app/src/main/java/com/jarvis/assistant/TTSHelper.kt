package com.jarvis.assistant

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

/**
 * Wraps Android's TextToSpeech and tries to pick a female-sounding voice
 * so Raksha's replies sound consistent every time.
 */
class TTSHelper(context: Context, private val onReady: () -> Unit = {}) {

    private var tts: TextToSpeech? = null
    private var isReady = false

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale("hi", "IN") // Hindi/Hinglish friendly
                selectFemaleVoice()
                isReady = true
                onReady()
            }
        }
    }

    private fun selectFemaleVoice() {
        val engine = tts ?: return
        val voices: Set<Voice>? = engine.voices
        val femaleVoice = voices?.firstOrNull {
            it.name.contains("female", ignoreCase = true) &&
                !it.isNetworkConnectionRequired
        } ?: voices?.firstOrNull { it.name.contains("female", ignoreCase = true) }

        if (femaleVoice != null) {
            engine.voice = femaleVoice
        }
    }

    fun speak(text: String, onDone: (() -> Unit)? = null) {
        if (!isReady) return
        val utteranceId = "raksha_${System.currentTimeMillis()}"
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) {
                onDone?.invoke()
            }
            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {}
        })
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}
