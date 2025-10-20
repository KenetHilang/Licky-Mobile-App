package com.example.licky.data.local

import androidx.room.TypeConverter
import com.example.licky.data.model.DiseaseDetection
import com.example.licky.data.model.HealthStatus
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Type converters for Room database
 */
class Converters {
    private val gson = Gson()
    
    @TypeConverter
    fun fromDiseaseDetectionList(value: List<DiseaseDetection>?): String {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toDiseaseDetectionList(value: String): List<DiseaseDetection> {
        val listType = object : TypeToken<List<DiseaseDetection>>() {}.type
        return gson.fromJson(value, listType)
    }
    
    @TypeConverter
    fun fromHealthStatus(value: HealthStatus): String {
        return value.name
    }
    
    @TypeConverter
    fun toHealthStatus(value: String): HealthStatus {
        return HealthStatus.valueOf(value)
    }
}
