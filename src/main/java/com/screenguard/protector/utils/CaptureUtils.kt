package com.screenguard.protector.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.Image
import android.util.Log
import androidx.camera.core.ImageProxy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Утиліта для захоплення кадрів з камери та збереження у внутрішню пам'ять
 */
object CaptureUtils {
    private const val TAG = "CaptureUtils"
    private const val CAPTURE_FOLDER = "ScreenGuard/Captures"

    /**
     * Конвертує ImageProxy у Bitmap
     */
    fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap? {
        return try {
            val planes = imageProxy.image?.planes ?: return null
            val ySize = planes[0].buffer.remaining()
            val u8888Data = ByteArray(ySize)
            planes[0].buffer.get(u8888Data)
            
            val bitmap = Bitmap.createBitmap(
                imageProxy.width,
                imageProxy.height,
                Bitmap.Config.ARGB_8888
            )
            
            // Конвертуємо NV21 в ARGB
            val pixels = IntArray(imageProxy.width * imageProxy.height)
            
            for (i in pixels.indices) {
                val y = (u8888Data[i].toInt() and 0xff)
                pixels[i] = -0x1000000 or (y shl 16) or (y shl 8) or y
            }
            
            bitmap.setPixels(pixels, 0, imageProxy.width, 0, 0, imageProxy.width, imageProxy.height)
            
            // Ротуємо зображення для фронтальної камери
            rotateImage(bitmap, 90f)
        } catch (e: Exception) {
            Log.e(TAG, "Error converting ImageProxy to Bitmap: ${e.message}", e)
            null
        }
    }

    /**
     * Ротує зображення на вказаний кут
     */
    private fun rotateImage(bitmap: Bitmap, degrees: Float): Bitmap {
        return try {
            val matrix = Matrix().apply {
                postRotate(degrees)
            }
            Bitmap.createBitmap(
                bitmap,
                0, 0,
                bitmap.width,
                bitmap.height,
                matrix,
                true
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error rotating image: ${e.message}", e)
            bitmap
        }
    }

    /**
     * Зберігає Bitmap у внутрішню пам'ять
     */
    suspend fun saveBitmapToFile(context: Context, bitmap: Bitmap?): String? {
        return withContext(Dispatchers.IO) {
            if (bitmap == null) {
                Log.w(TAG, "Bitmap is null, cannot save")
                return@withContext null
            }

            try {
                // Створюємо папку для захоплень
                val captureDir = File(context.getExternalFilesDir(null), CAPTURE_FOLDER)
                if (!captureDir.exists()) {
                    captureDir.mkdirs()
                }

                // Генеруємо ім'я файлу з часом
                val timestamp = SimpleDateFormat(
                    "yyyy-MM-dd_HH-mm-ss-SSS",
                    Locale("uk")
                ).format(Date())
                val fileName = "capture_$timestamp.jpg"
                val file = File(captureDir, fileName)

                // Зберігаємо JPEG
                FileOutputStream(file).use { output ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, output)
                    output.flush()
                }

                Log.d(TAG, "Image saved successfully: ${file.absolutePath}")
                file.absolutePath
            } catch (e: Exception) {
                Log.e(TAG, "Error saving bitmap to file: ${e.message}", e)
                null
            }
        }
    }

    /**
     * Отримує папку зі знімками
     */
    fun getCaptureFolder(context: Context): File {
        return File(context.getExternalFilesDir(null), CAPTURE_FOLDER)
    }

    /**
     * Отримує всі знімки
     */
    fun getAllCaptures(context: Context): List<File> {
        return try {
            val folder = getCaptureFolder(context)
            if (!folder.exists()) {
                emptyList()
            } else {
                folder.listFiles()?.toList() ?: emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting captures: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Видаляє знімок
     */
    fun deleteCapture(file: File): Boolean {
        return try {
            val deleted = file.delete()
            if (deleted) {
                Log.d(TAG, "Capture deleted: ${file.name}")
            }
            deleted
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting capture: ${e.message}", e)
            false
        }
    }

    /**
     * Очищає всі знімки
     */
    fun clearAllCaptures(context: Context): Boolean {
        return try {
            val folder = getCaptureFolder(context)
            if (folder.exists()) {
                folder.listFiles()?.forEach { file ->
                    file.delete()
                }
                Log.d(TAG, "All captures cleared")
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing captures: ${e.message}", e)
            false
        }
    }

    /**
     * Отримує розмір папки зі знімками (в МБ)
     */
    fun getCapturesFolderSize(context: Context): Double {
        return try {
            val folder = getCaptureFolder(context)
            if (!folder.exists()) {
                return 0.0
            }

            var size = 0L
            folder.listFiles()?.forEach { file ->
                size += file.length()
            }

            // Конвертуємо в МБ
            size / (1024.0 * 1024.0)
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating folder size: ${e.message}", e)
            0.0
        }
    }
}
