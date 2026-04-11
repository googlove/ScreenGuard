package com.screenguard.protector.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
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
    private var lastAlertTime = 0L
    private val ALERT_COOLDOWN = 3000L // 3 seconds
    private var detectionActive = true

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override fun onCreate() {
        super.onCreate()
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
        
        preferencesManager = PreferencesManager(this)
        alertRepository = AlertRepository(this)
        cameraExecutor = Executors.newSingleThreadExecutor()
        
        setupFaceDetector()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
        
        startForeground(NOTIFICATION_ID, createNotification())
        
        CoroutineScope(Dispatchers.Main).launch {
            preferencesManager.saveServiceRunning(true)
            startCameraDetection()
        }
        
        return START_STICKY
    }

    private fun setupFaceDetector() {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
            .build()
        
        faceDetector = FaceDetection.getClient(options)
    }

    private fun startCameraDetection() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.result
                bindCameraUseCases()
            } catch (e: Exception) {
                Log.e(TAG, "Camera initialization error: ${e.message}")
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun bindCameraUseCases() {
        val cameraProvider = cameraProvider ?: return
        
        val preview = Preview.Builder().build().apply {
            setSurfaceProvider { }
        }

        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .apply {
                setAnalyzer(cameraExecutor) { imageProxy ->
                    if (detectionActive) {
                        detectFaces(imageProxy)
                    }
                    imageProxy.close()
                }
            }

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                this,
                CameraSelector.DEFAULT_FRONT_CAMERA,
                preview,
                imageAnalysis
            )
        } catch (e: Exception) {
            Log.e(TAG, "Use case binding failed: ${e.message}")
        }
    }

    private fun detectFaces(imageProxy: ImageAnalysis.ResultImage) {
        try {
            val image = InputImage.fromMediaImage(imageProxy.image!!, imageProxy.imageInfo.rotationDegrees)
            
            faceDetector.process(image)
                .addOnSuccessListener { faces ->
                    if (faces.isNotEmpty() && detectionActive) {
                        handleFaceDetected(faces.size)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Face detection failed: ${e.message}")
                }
        } catch (e: Exception) {
            Log.e(TAG, "Image processing error: ${e.message}")
        }
    }

    private fun handleFaceDetected(faceCount: Int) {
        val currentTime = System.currentTimeMillis()
        
        if (currentTime - lastAlertTime < ALERT_COOLDOWN) {
            return
        }
        
        lastAlertTime = currentTime
        
        CoroutineScope(Dispatchers.Default).launch {
            val isWhitelisted = preferencesManager.isCurrentDeviceWhitelisted()
            
            if (!isWhitelisted) {
                val alert = Alert(
                    id = 0,
                    timestamp = currentTime,
                    description = "Виявлено $faceCount осіб на екрані",
                    isUnknown = true
                )
                
                alertRepository.insertAlert(alert)
                
                CoroutineScope(Dispatchers.Main).launch {
                    preferencesManager.incrementAlertCount()
                    showAlertNotification(faceCount)
                    triggerAlertScreen()
                }
            }
        }
    }

    private fun showAlertNotification(faceCount: Int) {
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
            .build()

        notificationManager?.notify(ALERT_NOTIFICATION_ID, notification)
    }

    private fun triggerAlertScreen() {
        val alertIntent = Intent(this, AlertActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("face_detected", true)
        }
        startActivity(alertIntent)
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, "screenguard_alerts")
            .setContentTitle("ScreenGuard активний")
            .setContentText("Ваш телефон захищено")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        detectionActive = false
        cameraProvider?.unbindAll()
        cameraExecutor.shutdown()
        faceDetector.close()
        
        CoroutineScope(Dispatchers.Main).launch {
            preferencesManager.saveServiceRunning(false)
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
