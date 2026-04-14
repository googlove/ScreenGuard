package com.screenguard.protector.utils

import android.util.Log

/**
 * Централізована система логування для ScreenGuard
 */
object LoggingUtils {
    private const val TAG = "ScreenGuard"
    private var debugMode = true

    fun setDebugMode(enable: Boolean) {
        debugMode = enable
    }

    fun d(tag: String, message: String) {
        if (debugMode) {
            Log.d("$TAG:$tag", message)
        }
    }

    fun d(tag: String, message: String, throwable: Throwable) {
        if (debugMode) {
            Log.d("$TAG:$tag", message, throwable)
        }
    }

    fun i(tag: String, message: String) {
        Log.i("$TAG:$tag", message)
    }

    fun w(tag: String, message: String) {
        Log.w("$TAG:$tag", message)
    }

    fun w(tag: String, message: String, throwable: Throwable) {
        Log.w("$TAG:$tag", message, throwable)
    }

    fun e(tag: String, message: String) {
        Log.e("$TAG:$tag", message)
    }

    fun e(tag: String, message: String, throwable: Throwable) {
        Log.e("$TAG:$tag", message, throwable)
    }

    fun logEvent(event: String, details: String = "") {
        val timestamp = java.text.SimpleDateFormat(
            "dd.MM.yyyy HH:mm:ss",
            java.util.Locale("uk")
        ).format(java.util.Date())
        d("EVENT", "[$timestamp] $event - $details")
    }
}
