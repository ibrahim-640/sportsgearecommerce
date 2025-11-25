package com.example.sportsgear.models

data class Order(
    var orderId: String = "",
    var userId: String = "",
    var total: Double = 0.0,
    var orderDate: String = "",
    var timestamp: Long = 0L,
    var items: List<CartItem> = emptyList(),
    var status: String = "Pending",
    var paymentMethod: String = ""
)
