
package com.example.licky.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Disease(
    val name: String,
    val description: String,
    val symptoms: List<String>,
    val recommendations: List<String>
) : Parcelable
