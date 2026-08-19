package com.jarvis.assistant

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var toggleButton: Button
    private var isRunning = false

    private val permissionsNeeded = mutableListOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.INTERNET
    ).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.READ_MEDIA_IMAGES)
            add(Manifest.permission.READ_MEDIA_VIDEO)
            add(Manifest.permission.READ_MEDIA_AUDIO)
            add(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            add(Manifest.permission.READ_EXTERNAL_STORAGE)
            add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }.toTypedArray()

    private val requestPermissionLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val allGranted = results.values.all { it }
        if (allGranted) {
            checkAllFilesAccess()
        } else {
            Toast.makeText(this, "Raksha ko kaam karne ke liye saari permissions chahiye.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)
        toggleButton = findViewById(R.id.toggleButton)

        toggleButton.setOnClickListener {
            if (!isRunning) {
                startRaksha()
            } else {
                stopRaksha()
            }
        }

        updateUi()
    }

    private fun startRaksha() {
        if (!hasAllPermissions()) {
            requestPermissionLauncher.launch(permissionsNeeded)
            return
        }
        if (!FileSearchHelper.hasFullAccess()) {
            checkAllFilesAccess()
            return
        }

        val serviceIntent = Intent(this, WakeWordService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
        isRunning = true
        updateUi()
    }

    private fun stopRaksha() {
        stopService(Intent(this, WakeWordService::class.java))
        isRunning = false
        updateUi()
    }

    private fun hasAllPermissions(): Boolean {
        return permissionsNeeded.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun checkAllFilesAccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !FileSearchHelper.hasFullAccess()) {
            Toast.makeText(this, "Agle screen par 'Allow all files access' ON kar dijiye.", Toast.LENGTH_LONG).show()
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                data = Uri.parse("package:$packageName")
            }
            startActivity(intent)
        }
    }

    private fun updateUi() {
        statusText.text = if (isRunning)
            "Raksha ON hai — 'Raksha' bol kar activate kijiye"
        else
            "Raksha OFF hai"
        toggleButton.text = if (isRunning) "Raksha Band Karein" else "Raksha Shuru Karein"
    }
}
