package com.example.sportsgear.repository
import com.example.sportsgear.models.ProductModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ProductRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val productsCollection = firestore.collection("products")


    fun addProduct(product: ProductModel, onResult: (Boolean) -> Unit) {
        productsCollection.add(product)
            .addOnCompleteListener { task ->
                onResult(task.isSuccessful)
            }
    }


    suspend fun getProducts(): List<ProductModel> {
        return try {
            val snapshot = productsCollection.get().await()
            snapshot.documents.mapNotNull { document ->
                document.toObject(ProductModel::class.java)?.copy(id = document.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }


    fun deleteProduct(productId: String, onResult: (Boolean) -> Unit) {
        productsCollection.document(productId)
            .delete()
            .addOnCompleteListener { task ->
                onResult(task.isSuccessful)
            }
    }
}


