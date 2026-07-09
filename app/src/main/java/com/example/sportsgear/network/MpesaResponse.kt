package com.example.sportsgear.network

import com.google.gson.annotations.SerializedName

data class MpesaResponse(
    // ✅ FIX — removed the `success: Boolean` field that doesn't exist in
    // Daraja's response. Gson defaults non-nullable Boolean to false when the
    // field is missing, which was always false regardless of actual outcome.
    // ResponseCode == "0" is the correct Daraja success signal, which
    // MpesaRepository already checks correctly.
    @SerializedName("MerchantRequestID") val MerchantRequestID: String?,
    @SerializedName("CheckoutRequestID") val CheckoutRequestID: String?,
    @SerializedName("ResponseCode") val ResponseCode: String?,
    @SerializedName("ResponseDescription") val ResponseDescription: String?,
    @SerializedName("CustomerMessage") val CustomerMessage: String?
)