package com.jarvis.assistant

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.app.NotificationCompat

class WakeWordService : Service() {

    companion object {
        private const val CHANNEL_ID = "raksha_channel"
        private const val NOTIFICATION_ID = 1
        private const val WAKE_WORD = "raksha"
    }

    private lateinit var tts: TTSHelper
    private var speechRecognizer: SpeechRecognizer? = null
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        tts = TTSHelper(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildNotification("Raksha sun rahi hai..."))
        startListeningCycle()
        return START_STICKY
    }

    private fun startListeningCycle() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            updateNotification("Is phone par speech recognition available nahi hai")
            return
        }

        speechRecognizer?.destroy()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)

        val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-IN")
        }

        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val heard = matches?.firstOrNull()?.lowercase() ?: ""

                if (heard.contains(WAKE_WORD)) {
                    handleWakeWordDetected(heard)
                } else if (heard.isNotEmpty()) {
                    updateNotification("Suna: \"$heard\" — 'Raksha' boliye")
                    scheduleRestart()
                } else {
                    updateNotification("Raksha sun rahi hai...")
                    scheduleRestart()
                }
            }

            override fun onError(error: Int) {
                updateNotification("Raksha sun rahi hai... [err:${errorName(error)}]")
                scheduleRestart()
            }

            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        speechRecognizer?.startListening(recognizerIntent)
    }

    private fun handleWakeWordDetected(heardText: String) {
        updateNotification("Sun rahi hoon...")
        val afterWakeWord = heardText.substringAfter(WAKE_WORD).trim()

        if (afterWakeWord.isNotEmpty()) {
            CommandProcessor(applicationContext, tts).process(afterWakeWord)
            restartListening()
        } else {
            tts.speak("Yes boss, bataiye") {
                handler.post { listenForCommand() }
            }
        }
    }

    private fun listenForCommand() {
        speechRecognizer?.destroy()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-IN")
        }

        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val command = matches?.firstOrNull()
                if (command != null) {
                    val lower = command.lowercase()
                    val isExit = lower.contains("bas") || lower.contains("band karo") ||
                        lower.contains("bye") || lower.contains("thank you") || lower.contains("dhanyavaad")
                    if (isExit) {
                        tts.speak("Theek hai, boss.")
                        restartListening()
                    } else {
                        CommandProcessor(applicationContext, tts).process(command)
                        listenForCommand()
                    }
                } else {
                    restartListening()
                }
            }

            override fun onError(error: Int) {
                restartListening()
            }

            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        speechRecognizer?.startListening(recognizerIntent)
    }

    private fun scheduleRestart(delayMs: Long = 300) {
        handler.postDelayed({ startListeningCycle() }, delayMs)
    }

    private fun restartListening() {
        updateNotification("Raksha sun rahi hai...")
        scheduleRestart()
    }

    private fun errorName(error: Int): String {
        return when (error) {
            SpeechRecognizer.ERROR_AUDIO -> "AUDIO"
            SpeechRecognizer.ERROR_CLIENT -> "CLIENT"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "NO_PERMISSION"
            SpeechRecognizer.ERROR_NETWORK -> "NETWORK"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "NETWORK_TIMEOUT"
            SpeechRecognizer.ERROR_NO_MATCH -> "NO_MATCH"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "BUSY"
            SpeechRecognizer.ERROR_SERVER -> "SERVER"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "TIMEOUT"
            else -> "CODE_$error"
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
        speechRecognizer?.destroy()
        tts.shutdown()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
