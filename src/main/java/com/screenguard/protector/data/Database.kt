package com.screenguard.protector.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow

@Dao
interface AlertDao {
    @Insert
    suspend fun insertAlert(alert: Alert)

    @Query("SELECT * FROM alerts ORDER BY timestamp DESC")
    fun getAllAlerts(): Flow<List<Alert>>

    @Query("SELECT COUNT(*) FROM alerts WHERE isUnknown = 1")
    fun getUnknownAlertCount(): Flow<Int>

    @Query("DELETE FROM alerts")
    suspend fun clearAll()

    @Delete
    suspend fun deleteAlert(alert: Alert)
}

@Dao
interface WhitelistDao {
    @Insert
    suspend fun insertItem(item: WhitelistItem)

    @Query("SELECT * FROM whitelist WHERE isActive = 1 ORDER BY addedDate DESC")
    fun getAllItems(): Flow<List<WhitelistItem>>

    @Query("SELECT COUNT(*) FROM whitelist WHERE isActive = 1")
    suspend fun getItemCount(): Int

    @Delete
    suspend fun deleteItem(item: WhitelistItem)

    @Query("DELETE FROM whitelist")
    suspend fun clearAll()
}

@Database(entities = [Alert::class, WhitelistItem::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun alertDao(): AlertDao
    abstract fun whitelistDao(): WhitelistDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "screenguard_db"
                ).build().also { instance = it }
            }
        }
    }
}
