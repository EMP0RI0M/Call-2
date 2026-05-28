package com.example.data

import kotlinx.coroutines.flow.Flow

class CallRepository(private val callDao: CallDao) {
    val allCalls: Flow<List<CallEntity>> = callDao.getAllCalls()

    suspend fun getCallById(id: Int): CallEntity? {
        return callDao.getCallById(id)
    }

    suspend fun insertCall(call: CallEntity): Long {
        return callDao.insertCall(call)
    }

    suspend fun updateCall(call: CallEntity) {
        callDao.updateCall(call)
    }

    suspend fun deleteCallById(id: Int) {
        callDao.deleteCallById(id)
    }

    suspend fun clearAll() {
        callDao.clearAllCalls()
    }
}
