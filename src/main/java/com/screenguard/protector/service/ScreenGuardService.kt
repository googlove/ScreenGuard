package com.screenguard.protector.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.screenguard.protector.MainActivity
import com.screenguard.protector.R
import com.screenguard.protector.data.Alert
import com.screenguard.protector.data.AlertRepository
import com.screenguard.protector.ui.AlertActivity
import com.screenguard.protector.utils.PreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ScreenGuardService : Service(), LifecycleOwner {
    
    private val lifecycleRegistry = LifecycleRegistry(this)
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var alertRepository: AlertRepository
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var faceDetector: FaceDetector
    private var cameraProvider: ProcessCameraProvider? = null
    private var lastCaptureTime = 0L
    private val CAPTURE_COOLDOWN = 5000L // 5 seconds between captures
    private var captureFrame: ImageProxy? = null

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override fun onCreate() {
        super.onCreate()
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
        
        try {
            preferencesManager = PreferencesManager(this)
            alertRepository = AlertRepository(this)
            cameraExecutor = Executors.newSingleThreadExecutor()
            setupFaceDetector()
            Log.d(TAG, "Service created successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error during service creation: ${e.message}", e)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
        
        try {
            startForeground(NOTIFICATION_ID, createNotification())
            
            serviceJob = CoroutineScope(Dispatchers.Main).launch {
                preferencesManager.saveServiceRunning(true)
                startCameraDetection()
            }
            
            Log.d(TAG, "Service started successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting service: ${e.message}", e)
            return START_NOT_STICKY
        }
        
        return START_STICKY
    }

    private fun setupFaceDetector() {
        try {
            val options = FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
                .setMinFaceSize(0.1f)
                .build()
            
            faceDetector = FaceDetection.getClient(options)
            Log.d(TAG, "Face detector initialized")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up face detector: ${e.message}", e)
            throw e
        }
    }

    private fun startCameraDetection() {
        try {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
            cameraProviderFuture.addListener({
                try {
                    if (!cameraProviderFuture.isCancelled && cameraProviderFuture.isDone) {
                        cameraProvider = cameraProviderFuture.get()
                        bindCameraUseCases()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Camera initialization error: ${e.message}", e)
                }
            }, ContextCompat.getMainExecutor(this))
        } catch (e: Exception) {
            Log.e(TAG, "Error starting camera detection: ${e.message}", e)
        }
    }

    private fun bindCameraUseCases() {
        val cameraProvider = cameraProvider ?: run {
            Log.w(TAG, "Camera provider is null")
            return
        }
        
        try {
            val preview = Preview.Builder().build().apply {
                setSurfaceProvider { }
            }

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_NV21)
                .build()
                .apply {
                    setAnalyzer(cameraExecutor) { imageProxy ->
                        try {
                            if (detectionActive) {
                                detectFaces(imageProxy)
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error in analyzer: ${e.message}")
                        } finally {
                            imageProxy.close()
                        }
                    }
                }

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                this,
                CameraSelector.DEFAULT_FRONT_CAMERA,
                preview,
                imageAnalysis
            )
            Log.d(TAG, "Camera use cases bound successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Use case binding failed: ${e.message}", e)
        }
    }

    private fun detectFaces(imageProxy: ImageProxy) {
        try {
            val mediaImage = imageProxy.image
            if (mediaImage == null) {
                Log.w(TAG, "Media image is null")
                return
            }
            
            // Зберігаємо кадр для можливого захоплення
            captureFrame = imageProxy
            
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            
            faceDetector.process(image)
                .addOnSuccessListener { faces ->
                    if (faces.isNotEmpty() && detectionActive) {
                        Log.d(TAG, "Detected ${faces.size} faces")
                        handleFaceDetected(faces.size, imageProxy)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Face detection failed: ${e.message}", e)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Image processing error: ${e.message}", e)
        }
    }

    private fun handleFaceDetected(faceCount: Int, imageProxy: ImageProxy) {
        val currentTime = System.currentTimeMillis()
        
        if (currentTime - lastAlertTime < ALERT_COOLDOWN) {
            Log.d(TAG, "Alert cooldown active, skipping")
            return
        }
        
        lastAlertTime = currentTime
        
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val isWhitelisted = preferencesManager.isCurrentDeviceWhitelisted()
                
                if (!isWhitelisted) {
                    // Захоплюємо знімок
                    captureSnapshot(imageProxy)
                    
                    val alert = Alert(
                        id = 0,
                        timestamp = currentTime,
                        description = "Виявлено $faceCount ${if (faceCount == 1) "особи" else "осіб"} на екрані",
                        isUnknown = true
                    )
                    
                    alertRepository.insertAlert(alert)
                    
                    CoroutineScope(Dispatchers.Main).launch {
                        preferencesManager.incrementAlertCount()
                        showAlertNotification(faceCount)
                        triggerAlertScreen(faceCount)
                    }
                    
                    Log.d(TAG, "Alert triggered and stored")
                } else {
                    Log.d(TAG, "Device is whitelisted, skipping alert")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error handling face detection: ${e.message}", e)
            }
        }
    }

    /**
     * Захоплює знімок з камери та зберігає його
     */
    private fun captureSnapshot(imageProxy: ImageProxy) {
        val currentTime = System.currentTimeMillis()
        
        // Перевіряємо cooldown для захоплень
        if (currentTime - lastCaptureTime < CAPTURE_COOLDOWN) {
            Log.d(TAG, "Capture cooldown active, skipping")
            return
        }
        
        lastCaptureTime = currentTime
        
        CoroutineScope(Dispatchers.Default).launch {
            try {
                Log.d(TAG, "Capturing snapshot...")
                
                // Конвертуємо ImageProxy в Bitmap
                val bitmap = CaptureUtils.imageProxyToBitmap(imageProxy)
                if (bitmap != null) {
                    // Зберігаємо локально
                    val filePath = CaptureUtils.saveBitmapToFile(this@ScreenGuardService, bitmap)
                    
                    if (filePath != null) {
                        Log.i(TAG, "Snapshot saved: $filePath")
                        
                        // Намагаємось завантажити в Google Photos
                        val file = java.io.File(filePath)
                        val uploaded = GooglePhotosManager.uploadToGooglePhotos(
                            this@ScreenGuardService,
                            file
                        )
                        
                        if (uploaded) {
                            Log.i(TAG, "Snapshot uploaded to Google Photos")
                        } else {
                            Log.w(TAG, "Failed to upload to Google Photos, keeping local copy")
                        }
                    } else {
                        Log.w(TAG, "Failed to save snapshot")
                    }
                } else {
                    Log.w(TAG, "Failed to convert frame to bitmap")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error capturing snapshot: ${e.message}", e)
            }
        }
    }

    private fun showAlertNotification(faceCount: Int) {
        try {
            val notificationManager = getSystemService(NotificationManager::class.java)
            
            val alertIntent = Intent(this, AlertActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("face_count", faceCount)
            }
            
            val pendingIntent = PendingIntent.getActivity(
                this,
                ALERT_REQUEST_CODE,
                alertIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(this, "screenguard_alerts")
                .setContentTitle("🚨 ПОПЕРЕДЖЕННЯ ScreenGuard")
                .setContentText("На ваш екран дивиться стороння особа!")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setVibrate(longArrayOf(0, 500, 200, 500))
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
                .build()

            notificationManager?.notify(ALERT_NOTIFICATION_ID, notification)
            Log.d(TAG, "Alert notification shown")
        } catch (e: Exception) {
            Log.e(TAG, "Error showing notification: ${e.message}", e)
        }
    }

    private fun triggerAlertScreen(faceCount: Int) {
        try {
            val alertIntent = Intent(this, AlertActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("face_detected", true)
                putExtra("face_count", faceCount)
            }
            startActivity(alertIntent)
            Log.d(TAG, "Alert screen triggered")
        } catch (e: Exception) {
            Log.e(TAG, "Error triggering alert screen: ${e.message}", e)
        }
    }

    private fun createNotification(): Notification {
        return try {
            val intent = Intent(this, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            NotificationCompat.Builder(this, "screenguard_alerts")
                .setContentTitle("ScreenGuard активний")
                .setContentText("Ваш телефон захищено")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build()
        } catch (e: Exception) {
            Log.e(TAG, "Error creating notification: ${e.message}", e)
            Notification()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroying")
        
        try {
            lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
            detectionActive = false
            serviceJob?.cancel()
            cameraProvider?.unbindAll()
            cameraExecutor.shutdown()
            faceDetector.close()
            
            CoroutineScope(Dispatchers.Main).launch {
                preferencesManager.saveServiceRunning(false)
            }
            
            Log.d(TAG, "Service destroyed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error during service destruction: ${e.message}", e)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val TAG = "ScreenGuardService"
        private const val NOTIFICATION_ID = 1
        private const val ALERT_REQUEST_CODE = 100
        private const val ALERT_NOTIFICATION_ID = 2
    }
}
