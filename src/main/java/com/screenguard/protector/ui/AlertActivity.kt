package com.screenguard.protector.ui

import android.media.RingtoneManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.screenguard.protector.R
import com.screenguard.protector.databinding.ActivityAlertBinding
import com.screenguard.protector.utils.PreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlertActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityAlertBinding
    private var faceCount = 0
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlertBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferencesManager = PreferencesManager(this)

        // Show on lock screen with full screen settings
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )

        faceCount = intent.getIntExtra("face_count", 1)
        setupUI()
        playAlertSound()
        triggerVibration()
    }

    private fun setupUI() {
        binding.apply {
            alertTitle.text = "🚨 ПОПЕРЕДЖЕННЯ !"
            alertMessage.text = "Стороння особа дивиться на ваш екран!\n\n" +
                    "Виявлено осіб: $faceCount"
            warningIcon.setImageDrawable(
                ContextCompat.getDrawable(this@AlertActivity, R.drawable.ic_warning)
            )

            dismissButton.setOnClickListener {
                finish()
            }

            reportButton.setOnClickListener {
                showReportDialog()
            }
        }

        // Auto-dismiss after 15 seconds if no action
        handler.postDelayed({
            if (!isFinishing) {
                finish()
            }
        }, 15000)

        // Animate alert
        animateAlert()
        animateIcon()
    }

    private fun animateAlert() {
        try {
            val slideInAnimation = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left)
            binding.alertContainer.startAnimation(slideInAnimation)
        } catch (e: Exception) {
            binding.alertContainer.alpha = 1f
        }
    }

    private fun animateIcon() {
        try {
            val scaleAnimation = AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
            binding.warningIcon.startAnimation(scaleAnimation)
            
            // Pulse animation
            binding.warningIcon.animate()
                .scaleX(1.2f)
                .scaleY(1.2f)
                .setDuration(600)
                .withEndAction {
                    binding.warningIcon.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(600)
                        .start()
                }
                .start()
        } catch (e: Exception) {
            // Fallback if animation fails
        }
    }

    private fun playAlertSound() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                preferencesManager.soundEnabled.collect { isEnabled ->
                    if (isEnabled) {
                        try {
                            val ringtone = RingtoneManager.getRingtone(
                                this@AlertActivity,
                                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                            )
                            ringtone.play()
                        } catch (e: Exception) {
                            // Silently fail if sound system is not available
                        }
                    }
                }
            } catch (e: Exception) {
                // Ignore errors in sound playback
            }
        }
    }

    private fun triggerVibration() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                preferencesManager.vibrationEnabled.collect { isEnabled ->
                    if (isEnabled) {
                        try {
                            val vibrator = getSystemService(VIBRATOR_SERVICE) as? android.os.Vibrator
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                vibrator?.vibrate(
                                    android.os.VibrationEffect.createPattern(
                                        longArrayOf(0, 500, 200, 500, 200, 500)
                                    )
                                )
                            } else {
                                @Suppress("DEPRECATION")
                                vibrator?.vibrate(longArrayOf(0, 500, 200, 500, 200, 500), -1)
                            }
                        } catch (e: Exception) {
                            // Vibration not available
                        }
                    }
                }
            } catch (e: Exception) {
                // Ignore errors
            }
        }
    }

    private fun showReportDialog() {
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Повідомити про інцидент")
            .setMessage(
                "Інцидент записано в базу даних ScreenGuard.\n\n" +
                "Дата/час: ${java.text.SimpleDateFormat("dd.MM.yyyy HH:mm:ss", java.util.Locale("uk")).format(java.util.Date())}\n" +
                "Осіб виявлено: $faceCount"
            )
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .setCancelable(false)
            .create()
        dialog.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}
