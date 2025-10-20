package com.example.licky.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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

    private val _totalScans = MutableLiveData<Int>()
    val totalScans: LiveData<Int> = _totalScans

    init {
        val scanResultDao = LickyDatabase.getDatabase(application).scanResultDao()
        repository = ScanRepository(scanResultDao)

        // Get recent 5 scans as Flow and convert to LiveData
        recentScans = scanResultDao.getRecentScansFlow(5).asLiveData()

        // Get total scan count
        loadTotalScans()
    }

    private fun loadTotalScans() {
        viewModelScope.launch {
            val dao = LickyDatabase.getDatabase(getApplication()).scanResultDao()
            _totalScans.value = dao.getScanCount()
        }
    }

    fun deleteScanResult(scanResult: ScanResult) {
        viewModelScope.launch {
            repository.deleteScanResult(scanResult)
            // Refresh total count after deletion
            loadTotalScans()
        }
    }
}