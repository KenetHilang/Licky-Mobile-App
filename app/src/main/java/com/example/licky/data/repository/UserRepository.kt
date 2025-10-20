package com.example.licky.data.repository

import androidx.lifecycle.LiveData
import com.example.licky.data.local.UserDao
import com.example.licky.data.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for managing user data
 */
class UserRepository(private val userDao: UserDao) {
    
    val user: LiveData<User?> = userDao.getUser()
    
    suspend fun insertUser(user: User) = withContext(Dispatchers.IO) {
        userDao.insertUser(user)
    }
    
    suspend fun updateUser(user: User) = withContext(Dispatchers.IO) {
        userDao.updateUser(user)
    }
    
    suspend fun deleteUser() = withContext(Dispatchers.IO) {
        userDao.deleteUser()
    }
}
