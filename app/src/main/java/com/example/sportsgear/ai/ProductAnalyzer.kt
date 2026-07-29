package com.example.sportsgear.ai

import android.graphics.Bitmap
import com.google.firebase.ai.type.content
import org.json.JSONObject

data class ProductSuggestion(
    val name: String,
    val description: String,
    val category: String,
    val suggestedPrice: String,
    val reasoning: String
)

suspend fun analyzeProductImage(bitmap: Bitmap): ProductSuggestion {
    val prompt = """
        You are helping an admin list a new product on a sports-gear e-commerce store.
        Look at the image and identify the item. Respond with ONLY valid JSON in this
        exact shape:

        {
          "name": "short, sellable product name",
          "description": "2-3 sentence appealing product description",
          "category": "one of: sports wear, Jerseys, Equipment, Accessories",
          "suggestedPrice": "a reasonable price in KES as a plain number string, no currency symbol",
          "reasoning": "one short sentence on why you picked this category and price range"
        }

        Be realistic about pricing for the Kenyan sports-gear retail market.
        If the image doesn't clearly show a sports-related product, still make your
        best reasonable guess rather than refusing.
    """.trimIndent()

    val response = GeminiClient.productAnalysisModel.generateContent(
        content {
            image(bitmap)
            text(prompt)
        }
    )

    val json = JSONObject(response.text?.trim() ?: "{}")

    return ProductSuggestion(
        name = json.optString("name", ""),
        description = json.optString("description", ""),
        category = json.optString("category", ""),
        suggestedPrice = json.optString("suggestedPrice", ""),
        reasoning = json.optString("reasoning", "")
    )
}