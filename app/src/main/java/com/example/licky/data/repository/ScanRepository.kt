package com.example.licky.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.example.licky.data.local.ScanResultDao
import com.example.licky.data.model.ScanResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ScanRepository(private val scanResultDao: ScanResultDao) {

    // LiveData for observing all scan results
    val allScanResults: LiveData<List<ScanResult>> =
        scanResultDao.getAllScanResultsFlow().asLiveData()

    // Insert a new scan result
    suspend fun insertScanResult(scanResult: ScanResult): Long = withContext(Dispatchers.IO) {
        scanResultDao.insert(scanResult)
    }

    // Update an existing scan result
    suspend fun updateScanResult(scanResult: ScanResult) = withContext(Dispatchers.IO) {
        scanResultDao.update(scanResult)
    }

    // Delete a scan result
    suspend fun deleteScanResult(scanResult: ScanResult) = withContext(Dispatchers.IO) {
        scanResultDao.delete(scanResult)
    }

    // Get a specific scan result by ID
    suspend fun getScanResultById(id: String): ScanResult? = withContext(Dispatchers.IO) {
        scanResultDao.getScanResultById(id)
    }

    // Get all scan results
    suspend fun getAllScanResults(): List<ScanResult> = withContext(Dispatchers.IO) {
        scanResultDao.getAllScanResults()
    }

    // Get recent scans with limit
    suspend fun getRecentScans(limit: Int): List<ScanResult> = withContext(Dispatchers.IO) {
        scanResultDao.getRecentScans(limit)
    }

    // Delete all scans
    suspend fun deleteAllScans() = withContext(Dispatchers.IO) {
        scanResultDao.deleteAll()
    }
}