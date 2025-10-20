package com.example.licky.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.licky.data.local.LickyDatabase
import com.example.licky.data.model.User
import com.example.licky.data.repository.UserRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for Profile screen
 */
class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: UserRepository
    val user: LiveData<User?>
    
    init {
        val userDao = LickyDatabase.getDatabase(application).userDao()
        repository = UserRepository(userDao)
        user = repository.user
    }
    
    fun saveUser(user: User) {
        viewModelScope.launch {
            repository.insertUser(user)
        }
    }
    
    fun updateUser(user: User) {
        viewModelScope.launch {
            repository.updateUser(user)
        }
    }
}
