package com.example.sportsgear.models

data class UserModel(
    val userId: String = "",
    val firstname: String = "",
    val lastname: String = "",
    val password: String = "",
    val email: String = "",
    val profileImageUri: String = "",
    val favoriteCategories: List<String> = emptyList(),
    val cartItems: List<String> = emptyList(),
    val wishlist: List<String> = emptyList(),
    val isAdmin: Boolean = false
)