package com.example.sportsgear.models

data class Order(
    val orderId: String = "",
    val userId: String = "",
    val total: Double = 0.0,
    val orderDate: String = "",
    val timestamp: Long = 0L,
    val items: List<CartItem> = emptyList(),
    val status: String = "Pending",
    val paymentMethod: String = ""
)
