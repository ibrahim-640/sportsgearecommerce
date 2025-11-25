package com.example.sportsgear.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val SAFARICOM_BASE_URL = "https://sandbox.safaricom.co.ke/"
    private const val NGROK_BASE_URL = "https://8c8d-102-214-157-197.ngrok-free.app/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    // ✅ Safaricom M-Pesa API client
    private val mpesaApiInstance: MpesaApi by lazy {
        Retrofit.Builder()
            .baseUrl(SAFARICOM_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient)
            .build()
            .create(MpesaApi::class.java)
    }

    // ✅ Optional: backend client using your ngrok callback server
    val backendApi by lazy {
        Retrofit.Builder()
            .baseUrl(NGROK_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient)
            .build()
    }

    // ✅ Public method to access M-Pesa API
    fun provideMpesaApi(): MpesaApi = mpesaApiInstance
}
