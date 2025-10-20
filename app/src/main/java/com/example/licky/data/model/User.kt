package com.example.licky.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

/**
 * Represents the user profile
 */
@Parcelize
@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val id: String,
    val name: String,
    val email: String,
    val age: Int? = null,
    val gender: Gender? = null,
    val profileImagePath: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) : Parcelable

enum class Gender {
    MALE,
    FEMALE,
    OTHER
}
