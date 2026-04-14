package com.screenguard.protector.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.screenguard.protector.adapter.CapturesAdapter
import com.screenguard.protector.databinding.ActivityCapturesBinding
import com.screenguard.protector.utils.CaptureUtils
import com.screenguard.protector.utils.GooglePhotosManager
import kotlinx.coroutines.launch
import java.io.File

/**
 * Активність для перегляду та керування захопленими знімками
 */
class CapturesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCapturesBinding
    private lateinit var adapter: CapturesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCapturesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        loadCaptures()
    }

    private fun setupUI() {
        // Grid adapter для знімків
        adapter = CapturesAdapter { file ->
            showCaptureOptions(file)
        }

        binding.capturesGridView.apply {
            layoutManager = GridLayoutManager(this@CapturesActivity, 2)
            adapter = this@CapturesActivity.adapter
        }

        // Back button
        binding.backButton.setOnClickListener {
            finish()
        }

        // Upload all button
        binding.uploadAllButton.setOnClickListener {
            uploadAllCaptures()
        }

        // Clear all button
        binding.clearAllButton.setOnClickListener {
            clearAllCaptures()
        }

        // Refresh button
        binding.refreshButton.setOnClickListener {
            loadCaptures()
        }
    }

    private fun loadCaptures() {
        lifecycleScope.launch {
            try {
                val captures = CaptureUtils.getAllCaptures(this@CapturesActivity)
                adapter.submitList(captures)

                val storageSize = CaptureUtils.getCapturesFolderSize(this@CapturesActivity)
                binding.storageInfo.text = "Знімки: ${captures.size} | Розмір: %.2f MB".format(storageSize)

                if (captures.isEmpty()) {
                    binding.emptyState.text = "Немає захоплених знімків"
                } else {
                    binding.emptyState.text = ""
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@CapturesActivity,
                    "Помилка при завантаженні: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun showCaptureOptions(file: File) {
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Опції для знімка")
            .setMessage(file.name)
            .setPositiveButton("Видалити") { _, _ ->
                deleteCapture(file)
            }
            .setNegativeButton("Відправити в Google Photos") { _, _ ->
                uploadCapture(file)
            }
            .setNeutralButton("Скасувати", null)
            .create()
        dialog.show()
    }

    private fun deleteCapture(file: File) {
        lifecycleScope.launch {
            try {
                if (CaptureUtils.deleteCapture(file)) {
                    Toast.makeText(this@CapturesActivity, "Видалено", Toast.LENGTH_SHORT).show()
                    loadCaptures()
                } else {
                    Toast.makeText(this@CapturesActivity, "Помилка при видаленні", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@CapturesActivity, "Помилка: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadCapture(file: File) {
        lifecycleScope.launch {
            try {
                binding.progressBar.visibility = android.view.View.VISIBLE
                val uploaded = GooglePhotosManager.uploadToGooglePhotos(this@CapturesActivity, file)
                binding.progressBar.visibility = android.view.View.GONE

                if (uploaded) {
                    Toast.makeText(
                        this@CapturesActivity,
                        "✅ Завантажено в Google Photos",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this@CapturesActivity,
                        "⚠️ Не вдалось завантажити (перевірте інтернет)",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = android.view.View.GONE
                Toast.makeText(this@CapturesActivity, "Помилка: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadAllCaptures() {
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Відправити всі знімки?")
            .setMessage("Всі знімки будуть завантажені в Google Photos")
            .setPositiveButton("Так") { _, _ ->
                lifecycleScope.launch {
                    try {
                        binding.progressBar.visibility = android.view.View.VISIBLE
                        val count = GooglePhotosManager.uploadAllCaptures(this@CapturesActivity)
                        binding.progressBar.visibility = android.view.View.GONE

                        Toast.makeText(
                            this@CapturesActivity,
                            "✅ Завантажено $count знімків",
                            Toast.LENGTH_SHORT
                        ).show()
                    } catch (e: Exception) {
                        binding.progressBar.visibility = android.view.View.GONE
                        Toast.makeText(this@CapturesActivity, "Помилка: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Скасувати", null)
            .create()
        dialog.show()
    }

    private fun clearAllCaptures() {
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Видалити всі знімки?")
            .setMessage("Ця дія неможливо скасувати")
            .setPositiveButton("Видалити") { _, _ ->
                lifecycleScope.launch {
                    try {
                        if (CaptureUtils.clearAllCaptures(this@CapturesActivity)) {
                            Toast.makeText(this@CapturesActivity, "Всі знімки видалено", Toast.LENGTH_SHORT).show()
                            loadCaptures()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@CapturesActivity, "Помилка: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Скасувати", null)
            .create()
        dialog.show()
    }

    override fun onResume() {
        super.onResume()
        loadCaptures()
    }
}
