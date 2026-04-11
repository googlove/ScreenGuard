package com.screenguard.protector.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.screenguard.protector.databinding.ActivitySettingsBinding
import com.screenguard.protector.utils.PreferencesManager
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferencesManager = PreferencesManager(this)
        setupUI()
    }

    private fun setupUI() {
        // Enable notifications toggle
        lifecycleScope.launch {
            preferencesManager.notificationsEnabled.collect { enabled ->
                binding.notificationsToggle.isChecked = enabled
            }
        }

        binding.notificationsToggle.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                preferencesManager.setNotificationsEnabled(isChecked)
            }
        }

        // Vibration toggle
        lifecycleScope.launch {
            preferencesManager.vibrationEnabled.collect { enabled ->
                binding.vibrationToggle.isChecked = enabled
            }
        }

        binding.vibrationToggle.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                preferencesManager.setVibrationEnabled(isChecked)
            }
        }

        // Sound toggle
        lifecycleScope.launch {
            preferencesManager.soundEnabled.collect { enabled ->
                binding.soundToggle.isChecked = enabled
            }
        }

        binding.soundToggle.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                preferencesManager.setSoundEnabled(isChecked)
            }
        }

        // Clear history button
        binding.clearHistoryButton.setOnClickListener {
            val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Видалити історію?")
                .setMessage("Ви впевнені, що хочете видалити всю історію?)
                .setPositiveButton("Видалити") { _, _ ->
                    lifecycleScope.launch {
                        preferencesManager.resetAlertCount()
                    }
                }
                .setNegativeButton("Скасувати", null)
                .create()
            dialog.show()
        }

        // Back button
        binding.backButton.setOnClickListener {
            finish()
        }

        // About button
        binding.aboutButton.setOnClickListener {
            showAboutDialog()
        }
    }

    private fun showAboutDialog() {
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Про додаток")
            .setMessage(
                "ScreenGuard v1.0.0\n\n" +
                "Додаток для захисту вашого приватності від\n" +
                "небажаних спостерігачів.\n\n" +
                "© 2024 ScreenGuard"
            )
            .setPositiveButton("OK", null)
            .create()
        dialog.show()
    }
}
