package com.example.sportsgear.models

import androidx.annotation.Keep

@Keep
data class Product(
    var name: String = "",
    var description: String = "",
    var price: String = "",
    var imageUrl: String = "",
    var quantity: String = "",
    var category: String = "",
    var value: String = "",
    var productId: String = "",
    var isOnOffer: Boolean = false
) {
    // ✅ Required by Firebase for deserialization
    constructor() : this("", "", "", "", "", "", "", "")
}
