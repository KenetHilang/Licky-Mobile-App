package com.example.licky.ui.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.licky.data.local.LickyDatabase
import com.example.licky.data.model.ScanResult
import com.example.licky.data.repository.ScanRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for History screen
 */
class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ScanRepository
    val allScans: LiveData<List<ScanResult>>

    init {
        val scanResultDao = LickyDatabase.getDatabase(application).scanResultDao()
        repository = ScanRepository(scanResultDao)
        allScans = repository.allScanResults
    }

    fun deleteScanResult(scanResult: ScanResult) {
        viewModelScope.launch {
            repository.deleteScanResult(scanResult)
        }
    }

    fun deleteAllScans() {
        viewModelScope.launch {
            repository.deleteAllScans()
        }
    }
}
