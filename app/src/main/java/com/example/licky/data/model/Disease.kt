package com.example.licky.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Represents a tongue disease that can be detected
 */
@Parcelize
data class Disease(
    val id: String,
    val name: String,
    val description: String,
    val symptoms: List<String>,
    val severity: Severity,
    val recommendations: List<String>,
    val imageUrl: String? = null
) : Parcelable

enum class Severity {
    LOW,
    MODERATE,
    HIGH,
    CRITICAL
}
