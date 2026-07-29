package com.example.sportsgear.data

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportsgear.ai.GeminiClient
import com.example.sportsgear.models.Product
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.getValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.json.JSONObject

class SmartSearchViewModel : ViewModel() {

    var results = mutableStateListOf<Product>()
        private set
    var isLoading = mutableStateOf(false)
        private set
    var errorMessage = mutableStateOf<String?>(null)
        private set
    var matchNote = mutableStateOf<String?>(null)
        private set

    fun search(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null
            matchNote.value = null
            results.clear()
            try {
                val snapshot = FirebaseDatabase.getInstance()
                    .getReference("Products")
                    .get()
                    .await()

                val allProducts = snapshot.children.mapNotNull { it.getValue(Product::class.java) }
                if (allProducts.isEmpty()) return@launch

                val catalogText = allProducts.joinToString("\n") {
                    val shortDesc = it.description.take(80) // ✅ cap description length
                    "id: ${it.productId} | name: ${it.name} | category: ${it.category} | price: ${it.price} | desc: $shortDesc"
                }

                val prompt = """
                    Sports-gear store search. Match user intent to products using MEANING
                    (synonyms, plurals, misspellings count — e.g. sneakers/trainers/running shoes
                    are the same idea). Only return genuinely relevant items; empty list if none fit.
                
                    JSON only: {"matchType": "match"|"none", "productIds": ["id1","id2"]}
                    Max 10 items, most relevant first.
                
                    Query: "$query"
                
                    Catalog:
                    $catalogText
                """.trimIndent()

                val responseText = generateWithRetry(prompt)
                Log.d("SmartSearch", "Raw Gemini response: $responseText")

                val json = JSONObject(responseText.trim())
                val matchType = json.optString("matchType", "none")
                val idArray = json.optJSONArray("productIds")
                val orderedIds = idArray?.let { arr ->
                    (0 until arr.length()).map { arr.getString(it) }
                } ?: emptyList()

                val productMap = allProducts.associateBy { it.productId }
                results.addAll(orderedIds.mapNotNull { productMap[it] })

                matchNote.value = if (matchType == "none") {
                    "No matches yet — try browsing a category below, or rephrase your search"
                } else null

            } catch (e: Exception) {
                Log.e("SmartSearch", "Search failed", e)
                errorMessage.value = if (e.message?.contains("high demand", ignoreCase = true) == true) {
                    "Our search assistant is a bit busy right now — please try again in a few seconds"
                } else {
                    "Search failed: ${e.message}"
                }
            } finally {
                isLoading.value = false
            }
        }
    }

    private suspend fun generateWithRetry(prompt: String, maxRetries: Int = 3): String {
        var lastError: Exception? = null
        repeat(maxRetries) { attempt ->
            try {
                val response = GeminiClient.searchModel.generateContent(prompt)
                return response.text ?: "{}"
            } catch (e: Exception) {
                lastError = e
                val isOverloaded = e.message?.contains("high demand", ignoreCase = true) == true ||
                        e.message?.contains("503", ignoreCase = true) == true ||
                        e.message?.contains("UNAVAILABLE", ignoreCase = true) == true

                if (isOverloaded && attempt < maxRetries - 1) {
                    val delayMs = 1000L * (attempt + 1)
                    Log.w("SmartSearch", "Model overloaded, retrying in ${delayMs}ms (attempt ${attempt + 1})")
                    delay(delayMs)
                } else if (!isOverloaded) {
                    throw e
                }
            }
        }
        throw lastError ?: Exception("Search failed after $maxRetries attempts")
    }
}