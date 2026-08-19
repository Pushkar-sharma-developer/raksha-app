package com.jarvis.assistant

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.core.app.NotificationCompat
import ai.picovoice.porcupine.Porcupine
import ai.picovoice.porcupine.PorcupineManager
import ai.picovoice.porcupine.PorcupineManagerCallback

/**
 * Runs continuously in the background listening only for the "Raksha"
 * wake word (via Porcupine — cheap on battery, doesn't send audio
 * anywhere). Once triggered, it opens the mic briefly to capture the
 * actual command and hands it to CommandProcessor.
 *
 * SETUP REQUIRED before this will run (see README.md):
 *  1. Free Picovoice account -> get an Access Key
 *  2. Train a custom wake word "Raksha" on console.picovoice.ai -> download the .ppn file
 *  3. Drop the .ppn file into app/src/main/assets/raksha.ppn
 *  4. Paste your Access Key below
 */
class WakeWordService : Service() {

    companion object {
        private const val CHANNEL_ID = "raksha_channel"
        private const val NOTIFICATION_ID = 1

        // TODO: paste your free Picovoice Access Key here (console.picovoice.ai)
        private const val PICOVOICE_ACCESS_KEY = "YOUR_PICOVOICE_ACCESS_KEY"

        // Must match the filename you place in app/src/main/assets/
        private const val WAKE_WORD_FILE = "raksha.ppn"
    }

    private var porcupineManager: PorcupineManager? = null
    private lateinit var tts: TTSHelper
    private var speechRecognizer: SpeechRecognizer? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        tts = TTSHelper(this) {
            tts.speak("Raksha online hai, boss.")
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildNotification("Raksha sun rahi hai..."))
        startWakeWordListening()
        return START_STICKY
    }

    private fun startWakeWordListening() {
        try {
            val callback = PorcupineManagerCallback { keywordIndex ->
                if (keywordIndex == 0) {
                    onWakeWordDetected()
                }
            }

            porcupineManager = PorcupineManager.Builder()
                .setAccessKey(PICOVOICE_ACCESS_KEY)
                .setKeywordPath("$WAKE_WORD_FILE") // loaded from assets
                .setSensitivity(0.6f)
                .build(applicationContext, callback)

            porcupineManager?.start()
        } catch (e: Exception) {
            Log.e("Raksha", "Wake word engine failed to start: ${e.message}")
            updateNotification("Setup adhoora hai — README dekho (Access Key / raksha.ppn missing)")
        }
    }

    private fun onWakeWordDetected() {
        // Pause wake-word listening while we capture the actual command
        porcupineManager?.stop()
        updateNotification("Sun rahi hoon...")
        tts.speak("Yes boss, bataiye") {
            listenForCommand()
        }
    }

    private fun listenForCommand() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
        }

        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: android.os.Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val command = matches?.firstOrNull()
                if (command != null) {
                    CommandProcessor(applicationContext, tts).process(command)
                } else {
                    tts.speak("Samajh nahi paayi, phir se try kijiye.")
                }
                resumeWakeWordListening()
            }

            override fun onError(error: Int) {
                tts.speak("Kuch samajh nahi aaya.")
                resumeWakeWordListening()
            }

            override fun onReadyForSpeech(params: android.os.Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partialResults: android.os.Bundle?) {}
            override fun onEvent(eventType: Int, params: android.os.Bundle?) {}
        })

        speechRecognizer?.startListening(recognizerIntent)
    }

    private fun resumeWakeWordListening() {
        speechRecognizer?.destroy()
        updateNotification("Raksha sun rahi hai...")
        try {
            porcupineManager?.start()
        } catch (e: Exception) {
            Log.e("Raksha", "Could not resume listening: ${e.message}")
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Raksha Assistant",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(text: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Raksha")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(text: String) {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, buildNotification(text))
    }

    override fun onDestroy() {
        super.onDestroy()
        porcupineManager?.stop()
        porcupineManager?.delete()
        speechRecognizer?.destroy()
        tts.shutdown()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
