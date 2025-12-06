package com.example.licky.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.example.licky.data.local.ScanResultDao
import com.example.licky.data.model.ScanResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ScanRepository(private val scanResultDao: ScanResultDao) {

    val allScanResults: LiveData<List<ScanResult>> =
        scanResultDao.getAllScanResultsFlow().asLiveData()

    fun getRecentScansFlow(limit: Int): Flow<List<ScanResult>> {
        return scanResultDao.getRecentScansFlow(limit)
    }

    fun getScanCountFlow(): Flow<Int> {
        return scanResultDao.getScanCountFlow()
    }


    suspend fun insertScanResult(scanResult: ScanResult): Long = withContext(Dispatchers.IO) {
        scanResultDao.insert(scanResult)
    }

    suspend fun updateScanResult(scanResult: ScanResult) = withContext(Dispatchers.IO) {
        scanResultDao.update(scanResult)
    }

    suspend fun deleteScanResult(scanResult: ScanResult) = withContext(Dispatchers.IO) {
        scanResultDao.delete(scanResult)
    }

    suspend fun deleteAllScans() = withContext(Dispatchers.IO) {
        scanResultDao.deleteAll()
    }

    suspend fun getScanResultById(id: String): ScanResult? = withContext(Dispatchers.IO) {
        scanResultDao.getScanResultById(id)
    }
}
