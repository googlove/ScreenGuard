package com.screenguard.protector.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.screenguard.protector.R
import com.screenguard.protector.databinding.ActivityAlertBinding

class AlertActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityAlertBinding
    private var faceCount = 0
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlertBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Show on lock screen
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )

        faceCount = intent.getIntExtra("face_count", 1)
        setupUI()
    }

    private fun setupUI() {
        binding.alertTitle.text = "🚨 ПОПЕРЕДЖЕННЯ !"
        binding.alertMessage.text = "Стороння особа дивиться на ваш екран!\n\nВиявлено осіб: $faceCount"
        binding.warningIcon.setImageDrawable(
            ContextCompat.getDrawable(this, R.drawable.ic_warning)
        )

        binding.dismissButton.setOnClickListener {
            finish()
        }

        binding.reportButton.setOnClickListener {
            // Show report dialog
            showReportDialog()
        }

        // Auto-dismiss after 10 seconds if no action
        handler.postDelayed({
            if (!isFinishing) {
                finish()
            }
        }, 10000)

        // Animate alert
        animateAlert()
    }

    private fun animateAlert() {
        binding.alertContainer.alpha = 0f
        binding.alertContainer.animate()
            .alpha(1f)
            .setDuration(300)
            .start()
    }

    private fun showReportDialog() {
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Повідомити про інцидент")
            .setMessage("Деталі інциденту збережені. Дякуємо за інформацію.")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .create()
        dialog.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}
