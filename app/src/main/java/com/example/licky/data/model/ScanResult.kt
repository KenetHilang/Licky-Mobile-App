package com.example.licky.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.util.Date

/**
 * Represents the result of a tongue scan analysis
 */
@Parcelize
@Entity(tableName = "scan_results")
data class ScanResult(
    @PrimaryKey
    val id: String,
    val imagePath: String,
    val timestamp: Long = System.currentTimeMillis(),
    val detectedDiseases: List<DiseaseDetection>,
    val overallHealth: HealthStatus,
    val confidenceScore: Float,
    val notes: String? = null
) : Parcelable

@Parcelize
data class DiseaseDetection(
    val disease: Disease,
    val probability: Float,
    val affectedAreas: List<String> = emptyList()
) : Parcelable

enum class HealthStatus {
    HEALTHY,
    MILD_CONCERNS,
    MODERATE_CONCERNS,
    SEVERE_CONCERNS,
    UNKNOWN
}
