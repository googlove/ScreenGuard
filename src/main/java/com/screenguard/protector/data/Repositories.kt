package com.screenguard.protector.data

import android.content.Context
import kotlinx.coroutines.flow.Flow

class AlertRepository(context: Context) {
    private val alertDao = AppDatabase.getInstance(context).alertDao()

    suspend fun insertAlert(alert: Alert) {
        alertDao.insertAlert(alert)
    }

    fun getAllAlerts(): Flow<List<Alert>> {
        return alertDao.getAllAlerts()
    }

    fun getUnknownAlertCount(): Flow<Int> {
        return alertDao.getUnknownAlertCount()
    }

    suspend fun clearAll() {
        alertDao.clearAll()
    }

    suspend fun deleteAlert(alert: Alert) {
        alertDao.deleteAlert(alert)
    }
}

class WhitelistRepository(context: Context) {
    private val whitelistDao = AppDatabase.getInstance(context).whitelistDao()

    suspend fun insertItem(item: WhitelistItem) {
        whitelistDao.insertItem(item)
    }

    fun getAllItems(): Flow<List<WhitelistItem>> {
        return whitelistDao.getAllItems()
    }

    suspend fun getItemCount(): Int {
        return whitelistDao.getItemCount()
    }

    suspend fun deleteItem(item: WhitelistItem) {
        whitelistDao.deleteItem(item)
    }

    suspend fun clearAll() {
        whitelistDao.clearAll()
    }
}
