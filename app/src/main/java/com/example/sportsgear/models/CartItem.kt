package com.example.sportsgear.models

data class CartItem(
    var productId: String = "",
    var name: String = "",
    var imageUrl: String = "",
    var price: String = "",
    var quantity: Int = 1,
    var category: String = "",

)
