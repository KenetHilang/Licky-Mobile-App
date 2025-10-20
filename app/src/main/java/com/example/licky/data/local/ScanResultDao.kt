package com.example.licky.data.local

import androidx.room.*
import com.example.licky.data.model.ScanResult
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanResultDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(scanResult: ScanResult): Long

    @Update
    suspend fun update(scanResult: ScanResult)

    @Delete
    suspend fun delete(scanResult: ScanResult)

    @Query("SELECT * FROM scan_results WHERE id = :id")
    suspend fun getScanResultById(id: String): ScanResult?

    @Query("SELECT * FROM scan_results ORDER BY timestamp DESC")
    suspend fun getAllScanResults(): List<ScanResult>

    @Query("SELECT * FROM scan_results ORDER BY timestamp DESC")
    fun getAllScanResultsFlow(): Flow<List<ScanResult>>

    @Query("SELECT * FROM scan_results ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentScans(limit: Int): List<ScanResult>

    // ADD THIS - Missing method causing error on line 31
    @Query("SELECT * FROM scan_results ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentScansFlow(limit: Int): Flow<List<ScanResult>>

    // ADD THIS - Missing method causing error on line 40
    @Query("SELECT COUNT(*) FROM scan_results")
    suspend fun getScanCount(): Int

    @Query("DELETE FROM scan_results")
    suspend fun deleteAll()
}