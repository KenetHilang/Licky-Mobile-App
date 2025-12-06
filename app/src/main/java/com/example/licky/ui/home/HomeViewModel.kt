package com.example.licky.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.licky.data.local.LickyDatabase
import com.example.licky.data.model.ScanResult
import com.example.licky.data.repository.ScanRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for Home screen
 */
class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ScanRepository

    val recentScans: LiveData<List<ScanResult>>

    val totalScans: LiveData<Int>

    init {
        val scanResultDao = LickyDatabase.getDatabase(application).scanResultDao()
        repository = ScanRepository(scanResultDao)

        // Get recent 4 scans (matches your previous request)
        recentScans = repository.getRecentScansFlow(4).asLiveData()

        // Connect the counter flow
        totalScans = repository.getScanCountFlow().asLiveData()
    }


    fun deleteScanResult(scanResult: ScanResult) {
        viewModelScope.launch {
            repository.deleteScanResult(scanResult)
        }
    }
}
