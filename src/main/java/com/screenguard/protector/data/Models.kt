package com.screenguard.protector.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alerts")
data class Alert(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val timestamp: Long,
    val description: String,
    val isUnknown: Boolean = true
)

@Entity(tableName = "whitelist")
data class WhitelistItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val addedDate: Long,
    val isActive: Boolean = true
)
