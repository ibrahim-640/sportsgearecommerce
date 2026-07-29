package com.example.sportsgear.ai
// ⚠️ Currently unused — AssistantViewModel switched to passing catalog
// context directly in the prompt instead of function calling, due to a
// role-compatibility error in firebase-ai:33.16.0. Kept here in case
// function calling is revisited once the SDK stabilizes.
import com.example.sportsgear.models.Product
import com.google.firebase.ai.type.FunctionDeclaration
import com.google.firebase.ai.type.Schema
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.getValue
import kotlinx.coroutines.tasks.await

val getProductInfoTool = FunctionDeclaration(
    name = "getProductInfo",
    description = "Look up live product details (price, stock quantity, category) from the store catalog by product name or category keyword",
    parameters = mapOf(
        "query" to Schema.string("Product name or category keyword to search for")
    )
)

suspend fun getProductInfo(query: String): String {
    val snapshot = FirebaseDatabase.getInstance()
        .getReference("Products")
        .get()
        .await()

    val matches = snapshot.children.mapNotNull { it.getValue(Product::class.java) }
        .filter {
            it.name.contains(query, ignoreCase = true) ||
                    it.category.contains(query, ignoreCase = true)
        }
        .take(5)

    return if (matches.isEmpty()) {
        "No matching products found."
    } else {
        matches.joinToString("\n") {
            "${it.name} — KES ${it.price}, stock: ${it.quantity}, category: ${it.category}" +
                    if (it.onOffer) " (on offer)" else ""
        }
    }
}