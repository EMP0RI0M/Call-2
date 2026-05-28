package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calls")
data class CallEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val phoneNumber: String,
    val callerName: String,
    val timestamp: Long,
    val duration: String,
    val detectedLanguage: String, // "English", "Hindi", "Bilingual EN/HI"
    val callStatus: String, // "Active", "Completed", "Missed"
    val isBilingual: Boolean,
    val messages: List<CallMessage>
)
