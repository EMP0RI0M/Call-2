package com.example.data

import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class CallConverters {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
    
    private val listType = Types.newParameterizedType(List::class.java, CallMessage::class.java)
    private val adapter = moshi.adapter<List<CallMessage>>(listType)

    @TypeConverter
    fun fromMessageList(messages: List<CallMessage>?): String? {
        return messages?.let { adapter.toJson(it) }
    }

    @TypeConverter
    fun toMessageList(json: String?): List<CallMessage>? {
        return json?.let { adapter.fromJson(it) }
    }
}
