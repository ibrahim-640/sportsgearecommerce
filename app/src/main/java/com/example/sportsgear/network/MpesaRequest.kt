package com.example.sportsgear.network

import com.google.gson.annotations.SerializedName

data class MpesaRequest(
    // ✅ FIX — added @SerializedName to every field. Without these, Gson's
    // default behavior may apply a naming policy (e.g. if one is set on the
    // Retrofit builder later) that converts PascalCase to something Daraja
    // doesn't recognize. Explicit annotations make serialization immune to
    // any Gson configuration changes.
    @SerializedName("BusinessShortCode") val BusinessShortCode: String,
    @SerializedName("Password") val Password: String,
    @SerializedName("Timestamp") val Timestamp: String,
    @SerializedName("TransactionType") val TransactionType: String,
    @SerializedName("Amount") val Amount: Int,
    @SerializedName("PartyA") val PartyA: String,
    @SerializedName("PartyB") val PartyB: String,
    @SerializedName("PhoneNumber") val PhoneNumber: String,
    @SerializedName("CallBackURL") val CallBackURL: String,
    @SerializedName("AccountReference") val AccountReference: String,
    @SerializedName("TransactionDesc") val TransactionDesc: String
)