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
import com.example.licky.ml.TongueClassifierModel
import com.example.licky.utils.Resource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.util.Log
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder
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
            _analysisResult.value = Resource.Loading()
            
            try {
                // Resize bitmap to model input size (224x224)
                val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true)
                
                // Load the model
                val model = TongueClassifierModel.newInstance(getApplication())

                // Create input tensor
                val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.FLOAT32)
                val byteBuffer = ByteBuffer.allocateDirect(4 * 224 * 224 * 3)
                byteBuffer.order(ByteOrder.nativeOrder())
                
                // Normalize pixel values to [0, 1]
                val intValues = IntArray(224 * 224)
                resizedBitmap.getPixels(intValues, 0, 224, 0, 0, 224, 224)
                
                var pixel = 0
                for (i in 0 until 224) {
                    for (j in 0 until 224) {
                        val value = intValues[pixel++]
                        // Extract RGB values and normalize to [0, 1]
                        byteBuffer.putFloat(((value shr 16) and 0xFF) / 255.0f) // R
                        byteBuffer.putFloat(((value shr 8) and 0xFF) / 255.0f)  // G
                        byteBuffer.putFloat((value and 0xFF) / 255.0f)          // B
                    }
                }
                
                inputFeature0.loadBuffer(byteBuffer)

                // Run inference
                val outputs = model.process(inputFeature0)
                val outputFeature0 = outputs.outputFeature0AsTensorBuffer
                
                // Get predictions (assuming output is class probabilities)
                val confidences = outputFeature0.floatArray
                val maxIndex = confidences.indices.maxByOrNull { confidences[it] } ?: 0
                val maxConfidence = confidences[maxIndex]
                
                // Map output to diseases (adjust based on your model's classes)
                val diseaseLabels = listOf(
                    "Healthy",
                    "Geographic Tongue",
                    "Oral Thrush",
                    "Vitamin Deficiency",
                    "Dehydration"
                )
                
                val detectedDisease = diseaseLabels.getOrNull(maxIndex) ?: "Unknown"
                
                // Create disease detection result
                val disease = Disease(
                    id = "disease_$maxIndex",
                    name = detectedDisease,
                    description = getDescriptionForDisease(detectedDisease),
                    symptoms = getSymptomsForDisease(detectedDisease),
                    severity = getSeverityForConfidence(maxConfidence),
                    recommendations = getRecommendationsForDisease(detectedDisease)
                )
                
                val diseaseDetection = DiseaseDetection(
                    disease = disease,
                    probability = maxConfidence,
                    affectedAreas = listOf("Center")
                )
                
                // Determine overall health status
                val healthStatus = when {
                    detectedDisease == "Healthy" && maxConfidence > 0.8f -> HealthStatus.HEALTHY
                    maxConfidence > 0.7f -> HealthStatus.MODERATE_CONCERNS
                    maxConfidence > 0.5f -> HealthStatus.MILD_CONCERNS
                    else -> HealthStatus.UNKNOWN
                }
                
                // Save scan result to database
                val scanResult = ScanResult(
                    id = UUID.randomUUID().toString(),
                    imagePath = imagePath,
                    timestamp = System.currentTimeMillis(),
                    detectedDiseases = listOf(diseaseDetection),
                    overallHealth = healthStatus,
                    confidenceScore = maxConfidence,
                    notes = null
                )
                
                repository.insertScanResult(scanResult)
                
                _analysisResult.value = Resource.Success(scanResult)
                
                // Close model
                model.close()
                
            } catch (e: Exception) {
                Log.e(TAG, "Analysis failed", e)
                _analysisResult.value = Resource.Error(
                    "Analysis failed: ${e.localizedMessage ?: "Unknown error"}"
                )
            }
        }
    }
    
    private fun getDescriptionForDisease(disease: String): String {
        return when (disease) {
            "Healthy" -> "Your tongue appears healthy with normal color and texture."
            "Geographic Tongue" -> "A benign condition causing map-like patches on the tongue surface."
            "Oral Thrush" -> "A fungal infection causing white patches in the mouth."
            "Vitamin Deficiency" -> "May indicate deficiency in vitamins B12, iron, or folate."
            "Dehydration" -> "Your tongue shows signs of dehydration."
            else -> "Unknown condition detected."
        }
    }
    
    private fun getSymptomsForDisease(disease: String): List<String> {
        return when (disease) {
            "Healthy" -> listOf("Pink color", "Smooth texture", "No discoloration")
            "Geographic Tongue" -> listOf("Map-like patches", "Smooth red areas", "May be sensitive")
            "Oral Thrush" -> listOf("White patches", "Redness", "Soreness")
            "Vitamin Deficiency" -> listOf("Pale color", "Swelling", "Smooth appearance")
            "Dehydration" -> listOf("Dry texture", "White coating", "Cracked surface")
            else -> listOf("Unknown symptoms")
        }
    }
    
    private fun getRecommendationsForDisease(disease: String): List<String> {
        return when (disease) {
            "Healthy" -> listOf(
                "Maintain good oral hygiene",
                "Brush tongue regularly",
                "Stay hydrated"
            )
            "Geographic Tongue" -> listOf(
                "Avoid spicy or acidic foods",
                "Maintain good oral hygiene",
                "Consult dentist if painful"
            )
            "Oral Thrush" -> listOf(
                "Consult a doctor immediately",
                "Antifungal medication may be needed",
                "Maintain oral hygiene"
            )
            "Vitamin Deficiency" -> listOf(
                "Consult a healthcare provider",
                "Consider vitamin supplements",
                "Eat balanced diet"
            )
            "Dehydration" -> listOf(
                "Drink more water",
                "Reduce caffeine intake",
                "Monitor hydration levels"
            )
            else -> listOf("Consult a healthcare professional")
        }
    }
    
    private fun getSeverityForConfidence(confidence: Float): Severity {
        return when {
            confidence > 0.8f -> Severity.LOW
            confidence > 0.6f -> Severity.MODERATE
            confidence > 0.4f -> Severity.HIGH
            else -> Severity.CRITICAL
        }
    }
    
    companion object {
        private const val TAG = "CameraViewModel"
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
        _analysisResult.postValue(Resource.Loading())
    }
}
