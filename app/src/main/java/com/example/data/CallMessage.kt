package com.example.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CallMessage(
    val id: String,
    val sender: String, // "Caller" or "Assistant"
    val text: String,
    val translation: String? = null, // Custom English translation or helpful phonetic pronunciation
    val timestampOffset: String, // e.g. "+00:04"
    val language: String // "en" or "hi"
)
