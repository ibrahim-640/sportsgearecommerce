package com.example.sportsgear.repository

import android.content.Context
import androidx.compose.runtime.MutableState
import com.example.sportsgear.models.Product
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class ProductRepository {

    private val database: DatabaseReference =
        FirebaseDatabase.getInstance().getReference("Products")

    // ✅ Add product
    fun addProduct(product: Product, onResult: (Boolean) -> Unit) {
        val productId = database.push().key
        if (productId != null) {
            val productWithId = product.copy(productId = productId)
            database.child(productId).setValue(productWithId)
                .addOnCompleteListener { task ->
                    onResult(task.isSuccessful)
                }
        } else {
            onResult(false)
        }
    }

    // ✅ Get all products (for ViewModel)
    fun getAllProducts(
        onSuccess: (List<Product>) -> Unit,
        onEmpty: () -> Unit,
        onError: (String) -> Unit
    ) {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val products = snapshot.children.mapNotNull { data ->
                    data.getValue(Product::class.java)?.copy(productId = data.key ?: "")
                }
                if (products.isEmpty()) {
                    onEmpty()
                } else {
                    onSuccess(products)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                onError(error.message)
            }
        })
    }

    // ✅ View single product by ID
    fun getProductById(productId: String, onResult: (Product?) -> Unit) {
        database.child(productId).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val product = snapshot.getValue(Product::class.java)?.copy(productId = productId)
                onResult(product)
            } else {
                onResult(null)
            }
        }.addOnFailureListener {
            onResult(null)
        }
    }

    // ✅ Delete product
    fun deleteProduct(productId: String, onResult: (Boolean) -> Unit) {
        database.child(productId).removeValue()
            .addOnCompleteListener { task ->
                onResult(task.isSuccessful)
            }
    }

    // ✅ Optional: Used by View screens with MutableState
    fun viewProducts(
        emptyProductState: MutableState<Product>,
        productListState: MutableState<List<Product>>,
        context: Context
    ) {
        database.get()
            .addOnSuccessListener { snapshot ->
                val products = snapshot.children.mapNotNull { data ->
                    data.getValue(Product::class.java)?.copy(productId = data.key ?: "")
                }
                productListState.value = products

                if (products.isEmpty()) {
                    emptyProductState.value = Product(
                        productId = "",
                        name = "No Products Found",
                        price = "",
                        imageUrl = "",
                        category = ""
                    )
                }
            }
            .addOnFailureListener {
                // Optional: Log or toast error
            }
    }
}
