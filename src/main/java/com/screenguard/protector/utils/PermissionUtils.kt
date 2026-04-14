package com.screenguard.protector.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

/**
 * Утиліта для управління дозволами додатку
 */
object PermissionUtils {
    
    /**
     * Перевіряє чи надано дозвіл
     */
    fun isPermissionGranted(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Перевіряє чи потрібен запит дозволу
     */
    fun isPermissionNeeded(context: Context, permission: String): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            !isPermissionGranted(context, permission)
        } else {
            false
        }
    }

    /**
     * Перевіряє наявність всіх необхідних дозволів
     */
    fun hasAllRequiredPermissions(context: Context): Boolean {
        return AppConfig.REQUIRED_PERMISSIONS.all { permission ->
            isPermissionGranted(context, permission)
        }
    }

    /**
     * Отримує список дозволів що не надано
     */
    fun getMissingPermissions(context: Context): List<String> {
        return AppConfig.REQUIRED_PERMISSIONS.filter { permission ->
            isPermissionNeeded(context, permission)
        }
    }

    /**
     * Перевіряє критичні дозволи (камера для ScreenGuard)
     */
    fun hasCriticalPermissions(context: Context): Boolean {
        return isPermissionGranted(context, android.Manifest.permission.CAMERA)
    }

    /**
     * Отримує опис дозволу українською мовою
     */
    fun getPermissionDescription(permission: String): String {
        return when (permission) {
            android.Manifest.permission.CAMERA -> "Доступ до камери"
            android.Manifest.permission.INTERNET -> "Доступ в Інтернет"
            android.Manifest.permission.ACCESS_NETWORK_STATE -> "Стан мережі"
            android.Manifest.permission.POST_NOTIFICATIONS -> "Надсилання сповіщень"
            else -> "Невідомий дозвіл"
        }
    }
}
