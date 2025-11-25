package com.example.sportsgear.network

data class MpesaResponse(
    val success: Boolean,
    val CustomerMessage: String,
    val MerchantRequestID: String?,
    val CheckoutRequestID: String?,
    val ResponseCode: String?,
    val ResponseDescription: String?,
)
