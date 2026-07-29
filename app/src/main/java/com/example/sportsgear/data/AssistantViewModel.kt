package com.example.sportsgear.data
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportsgear.ai.GeminiClient
import com.example.sportsgear.models.Product
import com.google.firebase.ai.type.QuotaExceededException
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.getValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AssistantViewModel : ViewModel() {

    private val chat = GeminiClient.chatModel.startChat()

    var messages = mutableStateListOf<Pair<String, Boolean>>()
        private set

    fun sendMessage(userText: String) {
        messages.add(userText to true)
        viewModelScope.launch {
            try {
                val catalogText = fetchCatalogText()
                val promptWithContext = """
                    Current product catalog:
                    $catalogText

                    User: $userText
                """.trimIndent()

                val response = sendWithRetry(promptWithContext)
                messages.add((response ?: "Sorry, I couldn't process that.") to false)

            } catch (e: QuotaExceededException) {
                Log.e("Assistant", "Quota exceeded", e)
                messages.add("I've hit my usage limit for the moment — please wait about 30 seconds and try again" to false)

            } catch (e: Exception) {
                Log.e("Assistant", "Send message failed", e)
                val friendly = if (e.message?.contains("high demand", ignoreCase = true) == true) {
                    "I'm a bit busy right now — please try again in a few seconds"
                } else {
                    "Something went wrong. Please try again."
                }
                messages.add(friendly to false)
            }
        }
    }

    private suspend fun fetchCatalogText(): String {
        val snapshot = FirebaseDatabase.getInstance()
            .getReference("Products")
            .get()
            .await()

        val allProducts = snapshot.children.mapNotNull { it.getValue(Product::class.java) }

        return allProducts.joinToString("\n") {
            "name: ${it.name} | category: ${it.category} | price: KES ${it.price} | stock: ${it.quantity}" +
                    if (it.onOffer) " | ON OFFER" else ""
        }
    }

    private suspend fun sendWithRetry(prompt: String, maxRetries: Int = 2): String? {
        repeat(maxRetries) { attempt ->
            try {
                val response = chat.sendMessage(prompt)
                return response.text
            } catch (e: QuotaExceededException) {
                if (attempt < maxRetries - 1) {
                    Log.w("Assistant", "Quota hit, waiting before retry")
                    delay(18_000L) // Google told us ~17s in the trace — pad slightly
                } else {
                    throw e
                }
            }
        }
        return null
    }
}