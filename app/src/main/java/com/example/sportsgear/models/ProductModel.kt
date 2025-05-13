package com.example.sportsgear.models
import androidx.annotation.Keep

@Keep
data class ProductModel(
    val id: String="",
    val name: String="",
    val description: String="",
    val price: Double=0.0,
    val imageUri: String="",
    val category: String="",
    val value : String="",
)