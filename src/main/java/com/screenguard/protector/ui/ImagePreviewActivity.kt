package com.screenguard.protector.ui

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.screenguard.protector.R
import com.screenguard.protector.databinding.ActivityImagePreviewBinding
import java.io.File

/**
 * Активність для переглядання знімків у повний екран
 */
class ImagePreviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImagePreviewBinding
    private var currentFile: File? = null

    companion object {
        const val EXTRA_FILE_PATH = "file_path"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImagePreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        loadImage()
    }

    private fun setupUI() {
        binding.backButton.setOnClickListener {
            finish()
        }

        binding.deleteButton.setOnClickListener {
            deleteImage()
        }

        binding.shareButton.setOnClickListener {
            shareImage()
        }

        binding.imagePreview.scaleType = ImageView.ScaleType.CENTER_INSIDE
    }

    private fun loadImage() {
        val filePath = intent.getStringExtra(EXTRA_FILE_PATH)
        if (filePath == null) {
            Toast.makeText(this, "Помилка: файл не знайдено", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        currentFile = File(filePath)
        if (!currentFile!!.exists()) {
            Toast.makeText(this, "Файл видалено", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        try {
            val bitmap = BitmapFactory.decodeFile(filePath)
            if (bitmap != null) {
                binding.imagePreview.setImageBitmap(bitmap)
                binding.imageFileName.text = currentFile!!.name
                binding.imageFileSize.text = "%.2f MB".format(currentFile!!.length() / (1024.0 * 1024.0))
            } else {
                Toast.makeText(this, "Не вдалось завантажити зображення", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Помилка: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteImage() {
        if (currentFile == null) return

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Видалити знімок?")
            .setMessage("${currentFile!!.name}")
            .setPositiveButton("Видалити") { _, _ ->
                if (currentFile!!.delete()) {
                    Toast.makeText(this, "Видалено", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Помилка при видаленні", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Скасувати", null)
            .show()
    }

    private fun shareImage() {
        if (currentFile == null || !currentFile!!.exists()) {
            Toast.makeText(this, "Файл недоступний", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val uri = androidx.core.content.FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                currentFile!!
            )

            val shareIntent = android.content.Intent().apply {
                action = android.content.Intent.ACTION_SEND
                putExtra(android.content.Intent.EXTRA_STREAM, uri)
                type = "image/jpeg"
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(
                android.content.Intent.createChooser(
                    shareIntent,
                    "Поділитися знімком"
                )
            )
        } catch (e: Exception) {
            Toast.makeText(this, "Помилка при поділенні: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
