package com.example.licky.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.licky.data.model.ScanResult
import com.example.licky.data.model.User

/**
 * Room database for local data storage
 */
@Database(
    entities = [ScanResult::class, User::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class LickyDatabase : RoomDatabase() {
    
    abstract fun scanResultDao(): ScanResultDao
    abstract fun userDao(): UserDao
    
    companion object {
        @Volatile
        private var INSTANCE: LickyDatabase? = null
        
        fun getDatabase(context: Context): LickyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LickyDatabase::class.java,
                    "licky_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
