package com.example.sportsgear.models

data class OrderItem(
    var name: String = "",
    var productId: String = "",
    var price: Double = 0.0,
    var quantity: Int = 0,
)
