package com.screenguard.protector.utils

/**
 * Централізована конфігурація додатку ScreenGuard
 */
object AppConfig {
    // Application Info
    const val APP_NAME = "ScreenGuard"
    const val APP_VERSION = "1.0.1"
    const val BUILD_NUMBER = 1

    // Camera Detection
    const val FACE_DETECTION_INTERVAL_MS = 100L
    const val ALERT_COOLDOWN_MS = 3000L
    const val MIN_FACE_SIZE = 0.1f
    
    // Snapshot Capture (НОВИЙ)
    const val CAPTURE_COOLDOWN_MS = 5000L  // 5 seconds between captures
    const val CAPTURE_JPEG_QUALITY = 90    // 0-100
    const val MAX_CAPTURES_STORAGE_MB = 500 // Maximum storage for captures

    // Notification
    const val NOTIFICATION_CHANNEL_ID = "screenguard_alerts"
    const val NOTIFICATION_CHANNEL_NAME = "ScreenGuard Оповіщення"
    const val NOTIFICATION_SERVICE_ID = 1
    const val NOTIFICATION_ALERT_ID = 2

    // Alert
    const val AUTO_DISMISS_ALERT_MS = 15000L

    // Database
    const val DATABASE_NAME = "screenguard_db"

    // Preferences
    const val PREFERENCES_NAME = "screenguard_settings"

    // Permissions required
    val REQUIRED_PERMISSIONS = arrayOf(
        android.Manifest.permission.CAMERA,
        android.Manifest.permission.INTERNET,
        android.Manifest.permission.ACCESS_NETWORK_STATE,
        android.Manifest.permission.POST_NOTIFICATIONS
    )

    // API Levels
    const val MIN_SDK = 21
    const val TARGET_SDK = 34

    // Feature flags
    const val ENABLE_DETAILED_LOGGING = true
    const val ENABLE_VIBRATION = true
    const val ENABLE_SOUND = true
    const val ENABLE_NOTIFICATIONS = true
}
