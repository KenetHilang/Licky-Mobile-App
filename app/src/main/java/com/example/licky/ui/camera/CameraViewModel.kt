package com.example.licky.ui.camera

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.licky.data.local.LickyDatabase
import com.example.licky.data.model.*
import com.example.licky.data.repository.ScanRepository
import com.example.licky.utils.Resource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

/**
 * ViewModel for Camera and Analysis
 */
class CameraViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: ScanRepository
    
    private val _analysisResult = MutableLiveData<Resource<ScanResult>>()
    val analysisResult: LiveData<Resource<ScanResult>> = _analysisResult
    
    init {
        val scanResultDao = LickyDatabase.getDatabase(application).scanResultDao()
        repository = ScanRepository(scanResultDao)
    }
    
    /**
     * Analyzes tongue image (placeholder for ML model integration)
     */
    fun analyzeTongueImage(imagePath: String, bitmap: Bitmap) {
        viewModelScope.launch {
            try {
                _analysisResult.value = Resource.Loading()
                
                // Simulate ML processing delay
                delay(2000)
                
                // TODO: Replace with actual ML model inference
                val mockResult = createMockAnalysisResult(imagePath)
                
                // Save to database
                repository.insertScanResult(mockResult)
                
                _analysisResult.value = Resource.Success(mockResult)
                
            } catch (e: Exception) {
                _analysisResult.value = Resource.Error(
                    "Analysis failed: ${e.localizedMessage}"
                )
            }
        }
    }
    
    /**
     * Creates a mock analysis result for demonstration
     * TODO: Replace with actual ML model predictions
     */
    private fun createMockAnalysisResult(imagePath: String): ScanResult {
        val mockDiseases = listOf(
            Disease(
                id = "1",
                name = "Healthy Tongue",
                description = "Your tongue appears healthy with normal coloration and texture.",
                symptoms = listOf("Pink color", "Smooth texture", "No coating"),
                severity = Severity.LOW,
                recommendations = listOf(
                    "Maintain good oral hygiene",
                    "Stay hydrated",
                    "Eat balanced diet"
                )
            )
        )
        
        val detections = mockDiseases.map { disease ->
            DiseaseDetection(
                disease = disease,
                probability = 0.85f,
                affectedAreas = listOf("Center")
            )
        }
        
        return ScanResult(
            id = UUID.randomUUID().toString(),
            imagePath = imagePath,
            timestamp = System.currentTimeMillis(),
            detectedDiseases = detections,
            overallHealth = HealthStatus.HEALTHY,
            confidenceScore = 0.85f,
            notes = null
        )
    }
    
    fun resetAnalysisResult() {
        _analysisResult.value = null
    }
}
