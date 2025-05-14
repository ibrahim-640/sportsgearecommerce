package com.example.sportsgear.models
import androidx.annotation.Keep

@Keep
data class ProductModel(
    val productId: String="",
    val name: String="",
    val description: String="",
    val price: String="",
    val imageUri: String="",
    val category: String="",
    val value : String="",
)