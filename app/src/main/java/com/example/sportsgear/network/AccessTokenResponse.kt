package com.example.sportsgear.network

import com.google.gson.annotations.SerializedName

data class AccessTokenResponse(
    @SerializedName("access_token") val accessToken: String,
    // ✅ FIX — Daraja sends expires_in as a String ("3599"), not an Int.
    // Declaring it as Int causes Gson to either throw or silently skip the
    // field depending on strictness mode. Changed to String to match reality.
    @SerializedName("expires_in") val expiresIn: String
)