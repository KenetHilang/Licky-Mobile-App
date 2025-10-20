package com.example.licky.ui.result

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.licky.data.local.LickyDatabase
import com.example.licky.data.model.ScanResult
import com.example.licky.data.repository.ScanRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for Result Detail screen
 */
class ResultDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ScanRepository

    private val _scanResult = MutableLiveData<ScanResult?>()
    val scanResult: LiveData<ScanResult?> = _scanResult

    private val _updateSuccess = MutableLiveData<Boolean>()
    val updateSuccess: LiveData<Boolean> = _updateSuccess

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        val scanResultDao = LickyDatabase.getDatabase(application).scanResultDao()
        repository = ScanRepository(scanResultDao)
    }

    fun loadScanResult(id: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = repository.getScanResultById(id)
                _scanResult.value = result
            } catch (e: Exception) {
                _scanResult.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateNotes(id: String, notes: String) {
        viewModelScope.launch {
            try {
                _scanResult.value?.let { result ->
                    val updatedResult = result.copy(notes = notes)
                    repository.insertScanResult(updatedResult)
                    _scanResult.value = updatedResult
                    _updateSuccess.value = true
                }
            } catch (e: Exception) {
                _updateSuccess.value = false
            }
        }
    }
}