package com.screenguard.protector

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.screenguard.protector.databinding.ActivityMainBinding
import com.screenguard.protector.service.ScreenGuardService
import com.screenguard.protector.ui.HistoryActivity
import com.screenguard.protector.ui.SettingsActivity
import com.screenguard.protector.ui.WhitelistActivity
import com.screenguard.protector.utils.PreferencesManager
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var preferencesManager: PreferencesManager
    private var isServiceRunning = false

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d(TAG, "Camera permission granted")
            toggleProtection(true)
        } else {
            Log.w(TAG, "Camera permission denied")
            Toast.makeText(
                this,
                "Дозвіл на камеру необхідний для роботи додатку",
                Toast.LENGTH_LONG
            ).show()
            binding.toggleProtection.isChecked = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d(TAG, "MainActivity created")
        
        preferencesManager = PreferencesManager(this)
        setupUI()
        createNotificationChannel()
        checkServiceStatus()
    }

    private fun setupUI() {
        // Toggle Protection
        binding.toggleProtection.setOnCheckedChangeListener { _, isChecked ->
            Log.d(TAG, "Protection toggle changed to: $isChecked")
            if (isChecked) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    toggleProtection(true)
                } else {
                    Log.d(TAG, "Requesting camera permission")
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            } else {
                toggleProtection(false)
            }
        }

        // Settings Button
        binding.settingsButton.setOnClickListener {
            Log.d(TAG, "Settings button clicked")
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // Whitelist Button
        binding.whitelistButton.setOnClickListener {
            Log.d(TAG, "Whitelist button clicked")
            startActivity(Intent(this, WhitelistActivity::class.java))
        }

        // History Button
        binding.historyButton.setOnClickListener {
            Log.d(TAG, "History button clicked")
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        // Captures Button (NEW)
        binding.capturesButton.setOnClickListener {
            Log.d(TAG, "Captures button clicked")
            startActivity(Intent(this, CapturesActivity::class.java))
        }

        // Status Updates
        lifecycleScope.launch {
            try {
                preferencesManager.isServiceRunning.collect { isRunning ->
                    Log.d(TAG, "Service running status: $isRunning")
                    isServiceRunning = isRunning
                    binding.toggleProtection.isChecked = isRunning
                    updateStatusUI(isRunning)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error observing service status: ${e.message}", e)
            }
        }

        // Alert Count
        lifecycleScope.launch {
            try {
                preferencesManager.alertCount.collect { count ->
                    Log.d(TAG, "Alert count updated to: $count")
                    binding.alertCountText.text = "Спроб доступу: $count"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error observing alert count: ${e.message}", e)
            }
        }
    }

    private fun updateStatusUI(isRunning: Boolean) {
        binding.statusText.text = if (isRunning) {
            "🛡️ Захист активний"
        } else {
            "⚠️ Захист вимкнено"
        }
        
        binding.statusText.setTextColor(
            ContextCompat.getColor(
                this,
                if (isRunning) android.R.color.holo_green_dark else android.R.color.holo_red_dark
            )
        )
    }

    private fun toggleProtection(enable: Boolean) {
        try {
            val intent = Intent(this, ScreenGuardService::class.java)
            
            if (enable) {
                Log.d(TAG, "Starting ScreenGuardService")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(intent)
                } else {
                    @Suppress("DEPRECATION")
                    startService(intent)
                }
                lifecycleScope.launch {
                    preferencesManager.saveServiceRunning(true)
                }
                Toast.makeText(this, "✅ Захист включено", Toast.LENGTH_SHORT).show()
            } else {
                Log.d(TAG, "Stopping ScreenGuardService")
                stopService(intent)
                lifecycleScope.launch {
                    preferencesManager.saveServiceRunning(false)
                }
                Toast.makeText(this, "⛔ Захист вимкнено", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling protection: ${e.message}", e)
            Toast.makeText(this, "Помилка: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val channel = NotificationChannel(
                    "screenguard_alerts",
                    "ScreenGuard Оповіщення",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Сповіщення про спроби несанкціонованого доступу"
                    enableVibration(true)
                    setShowBadge(true)
                }
                val notificationManager = getSystemService(NotificationManager::class.java)
                notificationManager?.createNotificationChannel(channel)
                Log.d(TAG, "Notification channel created")
            } catch (e: Exception) {
                Log.e(TAG, "Error creating notification channel: ${e.message}", e)
            }
        }
    }

    private fun checkServiceStatus() {
        lifecycleScope.launch {
            try {
                preferencesManager.isServiceRunning.collect { isRunning ->
                    binding.toggleProtection.isChecked = isRunning
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking service status: ${e.message}", e)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "MainActivity resumed")
        checkServiceStatus()
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
