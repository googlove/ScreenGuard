package com.screenguard.protector.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.screenguard.protector.service.ScreenGuardService
import com.screenguard.protector.utils.PreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "BootReceiver.onReceive called with action: ${intent?.action}")
        
        if (context == null || intent == null) {
            Log.w(TAG, "Context or Intent is null")
            return
        }
        
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "Not a BOOT_COMPLETED action, ignoring")
            return
        }

        try {
            val preferencesManager = PreferencesManager(context)
            
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    // Check if service was running before reboot
                    val wasRunning = preferencesManager.isServiceRunning.firstOrNull() ?: false
                    Log.d(TAG, "Service was running before reboot: $wasRunning")
                    
                    if (wasRunning) {
                        startScreenGuardService(context)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error checking preferences: ${e.message}", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onReceive: ${e.message}", e)
        }
    }
    
    private fun startScreenGuardService(context: Context) {
        try {
            val serviceIntent = Intent(context, ScreenGuardService::class.java)
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ContextCompat.startForegroundService(context, serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
            
            Log.d(TAG, "ScreenGuardService started successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting ScreenGuardService: ${e.message}", e)
        }
    }
    
    companion object {
        private const val TAG = "BootReceiver"
    }
}
