package com.example.licky.data.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.licky.data.model.User

/**
 * Data Access Object for User entities
 */
@Dao
interface UserDao {
    
    @Query("SELECT * FROM users LIMIT 1")
    fun getUser(): LiveData<User?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)
    
    @Update
    suspend fun updateUser(user: User)
    
    @Query("DELETE FROM users")
    suspend fun deleteUser()
}
