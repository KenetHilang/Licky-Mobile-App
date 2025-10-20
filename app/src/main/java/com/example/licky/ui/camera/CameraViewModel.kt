package com.example.licky.ui.camera

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.licky.data.local.LickyDatabase
import com.example.licky.data.model.Disease
import com.example.licky.data.model.DiseaseDetection
import com.example.licky.data.model.HealthStatus
import com.example.licky.data.model.ScanResult
import com.example.licky.data.repository.ScanRepository
import com.example.licky.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.UUID

/**
 * ViewModel for Camera and Analysis
 */
class CameraViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: ScanRepository
    
    private val _analysisResult = MutableLiveData<Resource<ScanResult>>()
    val analysisResult: LiveData<Resource<ScanResult>> = _analysisResult
    
    // Publishes the full per-class probabilities for the latest inference
    private val _classProbabilities = MutableLiveData<List<Pair<String, Float>>>()
    val classProbabilities: LiveData<List<Pair<String, Float>>> = _classProbabilities
    
    init {
        val scanResultDao = LickyDatabase.getDatabase(application).scanResultDao()
        repository = ScanRepository(scanResultDao)
    }
    
    /**
     * Analyze tongue image using TFLite model with ImageNet normalization.
     * Saves ScanResult and emits via LiveData.
     */
    fun analyzeTongueImage(imagePath: String, bitmap: Bitmap) {
        viewModelScope.launch {
            _analysisResult.value = Resource.Loading()

            try {
                val interpreter = getInterpreter()

                val input = preprocessImage(bitmap, IMG_SIZE)
                val output = Array(1) { FloatArray(NUM_CLASSES) }

                // Run inference off main thread
                withContext(Dispatchers.Default) {
                    interpreter.run(input, output)
                }

                val probs = softmax(output[0])
                // Publish sorted probabilities for UI/log
                val pairs = CLASSES.mapIndexed { idx, label -> label to probs.getOrElse(idx) { 0f } }
                    .sortedByDescending { it.second }
                _classProbabilities.postValue(pairs)
                val topIdx = probs.indices.maxByOrNull { probs[it] } ?: 0
                var topProb = probs[topIdx]
                var topLabel = CLASSES.getOrElse(topIdx) { "Unknown" }

                // Calibration: prefer "healthy" if its probability is high and close to top
                val idxHealthy = CLASSES.indexOf("healthy")
                if (idxHealthy >= 0) {
                    val pHealthy = probs[idxHealthy]
                    val margin = 0.10f // within 10% of top
                    if (pHealthy >= 0.50f && pHealthy + margin >= topProb) {
                        topLabel = "healthy"
                        topProb = pHealthy
                    }
                }

                val disease = Disease(
                    name = displayNameFor(topLabel),
                    description = descriptionFor(topLabel),
                    symptoms = symptomsFor(topLabel),
                    recommendations = recommendationsFor(topLabel)
                )

                val diseaseDetection = DiseaseDetection(
                    disease = disease,
                    probability = topProb,
                    affectedAreas = listOf("Center")
                )

                val health = overallHealthFor(topLabel, topProb)

                val scanResult = ScanResult(
                    id = UUID.randomUUID().toString(),
                    imagePath = imagePath,
                    timestamp = System.currentTimeMillis(),
                    detectedDiseases = listOf(diseaseDetection),
                    overallHealth = health,
                    confidenceScore = topProb,
                    notes = null
                )

                repository.insertScanResult(scanResult)
                _analysisResult.value = Resource.Success(scanResult)
            } catch (e: Exception) {
                Log.e(TAG, "Analysis failed", e)
                _analysisResult.value = Resource.Error(
                    "Analysis failed: ${e.localizedMessage ?: "Unknown error"}"
                )
            }
        }
    }
    
    private fun displayNameFor(label: String): String = when (label) {
        "healthy" -> "Healthy"
        "benign" -> "Benign Condition"
        "OPMD_Pra-Cancer" -> "OPMD (Pre-Cancer)"
        "OSCC_Cancer" -> "OSCC (Cancer)"
        "Diabetes" -> "Diabetes Indicator"
        else -> label
    }

    private fun descriptionFor(label: String): String = when (label) {
        "healthy" -> "Your tongue appears healthy with normal color and texture."
        "benign" -> "Likely a benign tongue variation with minimal health risk."
        "OPMD_Pra-Cancer" -> "Oral potentially malignant disorder; consider clinical evaluation."
        "OSCC_Cancer" -> "Findings suggest oral squamous cell carcinoma; seek immediate care."
        "Diabetes" -> "Patterns that may correlate with diabetes; consider testing."
        else -> "Unknown condition detected."
    }

    private fun symptomsFor(label: String): List<String> = when (label) {
        "healthy" -> listOf("Pink color", "Uniform texture")
        "benign" -> listOf("Mild discoloration", "Localized patches")
        "OPMD_Pra-Cancer" -> listOf("Persistent patches", "Texture changes")
        "OSCC_Cancer" -> listOf("Lesions", "Ulceration", "Bleeding")
        "Diabetes" -> listOf("Dryness", "Coating", "Cracks")
        else -> listOf("Unspecified")
    }

    private fun recommendationsFor(label: String): List<String> = when (label) {
        "healthy" -> listOf("Maintain oral hygiene", "Stay hydrated")
        "benign" -> listOf("Monitor changes", "Avoid irritants")
        "OPMD_Pra-Cancer" -> listOf("Consult a dentist/ENT", "Avoid tobacco and alcohol")
        "OSCC_Cancer" -> listOf("Seek urgent specialist care", "Diagnostic biopsy recommended")
        "Diabetes" -> listOf("Check blood glucose", "Consult physician")
        else -> listOf("Consult a healthcare professional")
    }

    private fun overallHealthFor(label: String, prob: Float): HealthStatus = when (label) {
        "healthy" -> if (prob >= 0.7f) HealthStatus.HEALTHY else HealthStatus.UNKNOWN
        "benign" -> if (prob >= 0.6f) HealthStatus.MILD_CONCERNS else HealthStatus.UNKNOWN
        "OPMD_Pra-Cancer" -> if (prob >= 0.5f) HealthStatus.MODERATE_CONCERNS else HealthStatus.MILD_CONCERNS
        "OSCC_Cancer" -> if (prob >= 0.4f) HealthStatus.SEVERE_CONCERNS else HealthStatus.MODERATE_CONCERNS
        "Diabetes" -> if (prob >= 0.5f) HealthStatus.MODERATE_CONCERNS else HealthStatus.MILD_CONCERNS
        else -> HealthStatus.UNKNOWN
    }
    
    companion object {
        private const val TAG = "CameraViewModel"
    }
    
    
    fun resetAnalysisResult() {
        _analysisResult.postValue(Resource.Loading())
    }

    override fun onCleared() {
        super.onCleared()
        try {
            tflite?.close()
        } catch (_: Exception) {
        }
        tflite = null
    }

    // --- TFLite helpers ---
    private val IMG_SIZE = 224
    private val NUM_CLASSES = 5
    private val CLASSES = arrayOf("healthy", "benign", "OPMD_Pra-Cancer", "OSCC_Cancer", "Diabetes")

    @Volatile private var tflite: Interpreter? = null

    @Synchronized
    private fun getInterpreter(): Interpreter {
        tflite?.let { return it }
        val buffer = try {
            loadModelFile("ml/tongue_classifier_model.tflite")
        } catch (e: Exception) {
            // Fallback to root assets location
            loadModelFile("tongue_classifier_model.tflite")
        }
        val interpreter = Interpreter(buffer)
        tflite = interpreter
        return interpreter
    }

    private fun loadModelFile(assetPath: String): MappedByteBuffer {
        val am = getApplication<Application>().assets
        val fd = am.openFd(assetPath)
        FileInputStream(fd.fileDescriptor).use { input ->
            val channel = input.channel
            return channel.map(FileChannel.MapMode.READ_ONLY, fd.startOffset, fd.declaredLength)
        }
    }

    private fun preprocessImage(bitmap: Bitmap, size: Int): Array<Array<Array<FloatArray>>> {
        val scaled = Bitmap.createScaledBitmap(bitmap, size, size, true)
        val input = Array(1) { Array(size) { Array(size) { FloatArray(3) } } }
        for (y in 0 until size) {
            for (x in 0 until size) {
                val p = scaled.getPixel(x, y)
                input[0][y][x][0] = ((Color.red(p) / 255.0f) - 0.485f) / 0.229f
                input[0][y][x][1] = ((Color.green(p) / 255.0f) - 0.456f) / 0.224f
                input[0][y][x][2] = ((Color.blue(p) / 255.0f) - 0.406f) / 0.225f
            }
        }
        return input
    }

    private fun softmax(logits: FloatArray): FloatArray {
        val max = logits.maxOrNull() ?: 0f
        val exp = DoubleArray(logits.size) { i -> kotlin.math.exp((logits[i] - max).toDouble()) }
        val sum = exp.sum()
        return FloatArray(exp.size) { i -> (exp[i] / sum).toFloat() }
    }
}
