package com.example.sportsgear.network
import android.util.Base64
import android.util.Log
import com.example.sportsgear.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MpesaRepository {
    private val api = RetrofitClient.provideMpesaApi()

    // ✅ FIX — credentials now read from BuildConfig instead of being
    // hardcoded in source code. Values come from local.properties which
    // is in .gitignore and never committed to version control.
    private val consumerKey = BuildConfig.MPESA_CONSUMER_KEY
    private val consumerSecret = BuildConfig.MPESA_CONSUMER_SECRET
    private val passkey = BuildConfig.MPESA_PASSKEY
    private val shortCode = BuildConfig.MPESA_SHORTCODE
    private val callbackUrl = BuildConfig.MPESA_CALLBACK_URL

    suspend fun getAccessToken(): String? = withContext(Dispatchers.IO) {
        try {
            val credentials = "$consumerKey:$consumerSecret"
            val basicAuth = "Basic " + Base64.encodeToString(
                credentials.toByteArray(), Base64.NO_WRAP
            )
            val response = api.getAccessToken(basicAuth)
            if (response.isSuccessful) {
                val token = response.body()?.accessToken
                token
            } else {
                Log.e("MPESA", "Token fetch failed: ${response.code()} " +
                        "${response.errorBody()?.string()}")
                null
            }
        } catch (e: Exception) {
            Log.e("MPESA", "Token fetch exception: ${e.localizedMessage}")
            null
        }
    }

    suspend fun initiatePayment(phone: String, amount: Int): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val timestamp = getTimestamp()
                val password = getPassword(shortCode, passkey, timestamp)
                val token = getAccessToken() ?: run {
                    Log.e("MPESA", "Failed to get access token — aborting STK push")
                    return@withContext false
                }

                val formattedPhone = formatPhone(phone)

                val request = MpesaRequest(
                    BusinessShortCode = shortCode,
                    Password = password,
                    Timestamp = timestamp,
                    TransactionType = "CustomerPayBillOnline",
                    Amount = amount,
                    PartyA = formattedPhone,
                    PartyB = shortCode,
                    PhoneNumber = formattedPhone,
                    // ✅ FIX — now reads from BuildConfig, not hardcoded.
                    // Update MPESA_CALLBACK_URL in local.properties whenever
                    // your callback endpoint changes — no code change needed.
                    CallBackURL = callbackUrl,
                    AccountReference = "SportsGear",
                    TransactionDesc = "SG Payment"
                )

                val response = api.stkPush("Bearer $token", request)

                if (!response.isSuccessful) {
                    val errorBody = response.errorBody()?.string()
                    Log.e("MPESA", "STK push failed: ${response.code()} -> " +
                            "${response.message()}")
                    Log.e("MPESA", "Error body: $errorBody")
                    when (response.code()) {
                        401 -> Log.e("MPESA", "Unauthorized — check Consumer Key/Secret/Passkey")
                        400 -> Log.e("MPESA", "Bad Request — check STK request parameters")
                        404 -> Log.e("MPESA", "404 — likely invalid callback URL or token")
                        else -> Log.e("MPESA", "Unexpected HTTP error")
                    }
                    return@withContext false
                }

                val body = response.body()
                Log.d("MPESA", "ResponseCode=${body?.ResponseCode} " +
                        "Message=${body?.CustomerMessage}")

                body?.ResponseCode == "0"

            } catch (e: retrofit2.HttpException) {
                Log.e("MPESA", "HttpException: ${e.code()} -> ${e.message()}")
                false
            } catch (e: IOException) {
                Log.e("MPESA", "Network error: ${e.localizedMessage}")
                false
            } catch (e: Exception) {
                Log.e("MPESA", "Unexpected error: ${e.localizedMessage}")
                false
            }
        }

    private fun formatPhone(phone: String): String {
        val cleaned = phone.trim()
            .replace(" ", "")
            .replace("-", "")
            .replace("+", "")
        return when {
            cleaned.startsWith("254") && cleaned.length == 12 -> cleaned
            cleaned.startsWith("0") && cleaned.length == 10 ->
                "254${cleaned.removePrefix("0")}"
            cleaned.length == 9 -> "254$cleaned"
            else -> cleaned
        }
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