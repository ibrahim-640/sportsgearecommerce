package com.example.sportsgear.network
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
class MpesaRepository {
    private val api = RetrofitClient.provideMpesaApi()

    // 🔒 Move credentials here only once
    private val consumerKey = "OC7HldYBQ1TKAUrER2XyQ8GftnkHogXq1nWA67US7I2jKG8r"
    private val consumerSecret = "zvZXkfOx0dTTl0cBmjtbtVuo5mSGs6dAvnRyfuq5DZzYMJzunmy9YhOoP9WpapHu"
    private val passkey = "bfb279f9aa9bdbcf158e97dd71a467cd2e0c893059b10f78e6b72ada1ed2c919"
    private val shortCode = "174379" // Test Paybill for Sandbox

    suspend fun getAccessToken(): String? = withContext(Dispatchers.IO) {
        val credentials = "$consumerKey:$consumerSecret"
        val basicAuth = "Basic " + Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)

        val response = api.getAccessToken(basicAuth)
        if (response.isSuccessful) response.body()?.accessToken else null
    }

    suspend fun initiatePayment(phone: String, amount: Int): Boolean = withContext(Dispatchers.IO) {
        val timestamp = getTimestamp()
        val password = getPassword(shortCode, passkey, timestamp)
        val token = getAccessToken() ?: return@withContext false

        val request = MpesaRequest(
            BusinessShortCode = "174379",
            Password = password,
            Timestamp = timestamp,
            TransactionType = "CustomerPayBillOnline",
            Amount = amount,
            PartyA = "254757894179",
            PartyB = "174379",
            PhoneNumber = "254757894179",
            CallBackURL = "https://8c8d-102-214-157-197.ngrok-free.app/callback",
            AccountReference = "SportsGear",
            TransactionDesc = "SportsGear Order Payment"
        )

        val response = api.stkPush("Bearer $token", request)
        return@withContext response.isSuccessful
    }

    private fun getTimestamp(): String {
        val sdf = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun getPassword(shortCode: String, passkey: String, timestamp: String): String {
        val dataToEncode = "$shortCode$passkey$timestamp"
        return Base64.encodeToString(dataToEncode.toByteArray(), Base64.NO_WRAP)
    }
}