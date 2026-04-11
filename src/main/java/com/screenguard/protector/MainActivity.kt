package com.screenguard.protector

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
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
            toggleProtection(true)
        } else {
            Toast.makeText(
                this,
                "Дозвіл на камеру необхідний",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferencesManager = PreferencesManager(this)
        setupUI()
        createNotificationChannel()
        checkServiceStatus()
    }

    private fun setupUI() {
        // Toggle Protection
        binding.toggleProtection.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    toggleProtection(true)
                } else {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            } else {
                toggleProtection(false)
            }
        }

        // Settings Button
        binding.settingsButton.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // Whitelist Button
        binding.whitelistButton.setOnClickListener {
            startActivity(Intent(this, WhitelistActivity::class.java))
        }

        // History Button
        binding.historyButton.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        // Status Updates
        lifecycleScope.launch {
            preferencesManager.isServiceRunning.collect { isRunning ->
                isServiceRunning = isRunning
                binding.toggleProtection.isChecked = isRunning
                binding.statusText.text = if (isRunning) {
                    "🛡️ Захист активний"
                } else {
                    "⚠️ Захист вимкнено"
                }
                binding.statusText.setTextColor(
                    ContextCompat.getColor(
                        this@MainActivity,
                        if (isRunning) android.R.color.holo_green_dark else android.R.color.holo_red_dark
                    )
                )
            }
        }

        // Alert Count
        lifecycleScope.launch {
            preferencesManager.alertCount.collect { count ->
                binding.alertCountText.text = "Спроб доступу: $count"
            }
        }
    }

    private fun toggleProtection(enable: Boolean) {
        val intent = Intent(this, ScreenGuardService::class.java)
        
        if (enable) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
            lifecycleScope.launch {
                preferencesManager.saveServiceRunning(true)
            }
            Toast.makeText(this, "Захист включено", Toast.LENGTH_SHORT).show()
        } else {
            stopService(intent)
            lifecycleScope.launch {
                preferencesManager.saveServiceRunning(false)
            }
            Toast.makeText(this, "Захист вимкнено", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "screenguard_alerts",
                "ScreenGuard Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Сповіщення про спроби доступу"
                enableVibration(true)
                setShowBadge(true)
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun checkServiceStatus() {
        lifecycleScope.launch {
            preferencesManager.isServiceRunning.collect { isRunning ->
                binding.toggleProtection.isChecked = isRunning
            }
        }
    }
}
