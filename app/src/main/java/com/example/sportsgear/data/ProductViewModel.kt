package com.example.sportsgear.data

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.sportsgear.models.CartItem
import com.example.sportsgear.models.Product
import com.example.sportsgear.network.ImgurService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

class ProductViewModel : ViewModel() {

    // 🔹 Firebase References
    private val database = FirebaseDatabase.getInstance().reference.child("Products")
    private val rootDatabase = FirebaseDatabase.getInstance().reference
    private val auth = FirebaseAuth.getInstance()

    // 🔹 State Variables
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _productList = MutableStateFlow<List<Product>>(emptyList())
    val productList: StateFlow<List<Product>> = _productList

    private val _offerProducts = MutableStateFlow<List<Product>>(emptyList())
    val offerProducts: StateFlow<List<Product>> = _offerProducts

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    // 🔹 ADD THIS FUNCTION: Check if current user is admin
    private fun checkAdminAccess(callback: (Boolean) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            callback(false)
            return
        }

        rootDatabase.child("Admins").child(currentUser.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    callback(snapshot.exists())
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false)
                }
            })
    }

    // 🔹 Add Product to Cart
    fun addToCart(product: Product, context: Context) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(context, "Please log in first", Toast.LENGTH_SHORT).show()
            return
        }

        _isLoading.value = true
        val userId = currentUser.uid
        // ✅ FIXED PATH: Use productId instead of push() for consistent cart structure
        val cartRef = rootDatabase.child("Cart").child(userId).child(product.productId)

        val cartItem = CartItem(
            productId = product.productId,
            name = product.name,
            price = product.price,
            imageUrl = product.imageUrl,
            category = product.category
        )

        cartRef.setValue(cartItem).addOnCompleteListener { task ->
            _isLoading.value = false
            if (task.isSuccessful) {
                Toast.makeText(context, "Added to cart", Toast.LENGTH_SHORT).show()
                Log.d("Cart", "Added ${cartItem.name} for user $userId")
            } else {
                Toast.makeText(context, "Failed to add to cart", Toast.LENGTH_SHORT).show()
                Log.e("Cart", "Failed to add: ${task.exception?.message}")
                _errorMessage.value = "Failed to add to cart"
            }
        }
    }


    // 🔹 Fetch All Products (Real-time)
    fun fetchProducts() {
        _isLoading.value = true
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val products = snapshot.children.mapNotNull { it.getValue(Product::class.java) }
                _productList.value = products
                _isLoading.value = false
            }

            override fun onCancelled(error: DatabaseError) {
                _errorMessage.value = "Failed to load products: ${error.message}"
                _isLoading.value = false
            }
        })
    }

    // 🔹 Fetch Products on Offer / Promotion
    fun fetchOfferProducts() {
        val offerRef = rootDatabase.child("Products")
            .orderByChild("isOnOffer")
            .equalTo(true)

        offerRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val offerList = mutableListOf<Product>()
                for (child in snapshot.children) {
                    val product = child.getValue(Product::class.java)
                    if (product != null) offerList.add(product)
                }
                _offerProducts.value = offerList
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ProductViewModel", "Error fetching offer products: ${error.message}")
            }
        })
    }

    // 🔹 Upload New Product with Image - ✅ ADDED ADMIN CHECK
    fun uploadProductWithImage(
        uri: Uri,
        context: Context,
        name: String,
        category: String,
        price: String,
        description: String,
        navController: NavController,
        isOnOffer: Boolean
    ) {
        // ✅ ADD ADMIN CHECK
        checkAdminAccess { isAdmin ->
            if (!isAdmin) {
                _isLoading.value = false
                _errorMessage.value = "Access denied. Admin privileges required."
                Toast.makeText(context, "❌ Admin access required to add products", Toast.LENGTH_LONG).show()
                return@checkAdminAccess
            }

            viewModelScope.launch(Dispatchers.IO) {
                try {
                    _isLoading.value = true
                    val imageUrl = uploadImageToImgur(context, uri) ?: return@launch

                    val productId = database.push().key ?: return@launch
                    val product = Product(
                        productId = productId,
                        name = name,
                        description = description,
                        price = price,
                        imageUrl = imageUrl,
                        category = category,
                        isOnOffer = isOnOffer
                    )

                    withContext(Dispatchers.Main) {
                        database.child(productId).setValue(product).addOnCompleteListener { task ->
                            _isLoading.value = false
                            if (task.isSuccessful) {
                                fetchProducts()
                                fetchOfferProducts()
                                navController.popBackStack()
                                Toast.makeText(context, "✅ Product added successfully", Toast.LENGTH_SHORT).show()
                            } else {
                                _errorMessage.value = "Failed to add product"
                            }
                        }
                    }
                } catch (e: Exception) {
                    _isLoading.value = false
                    _errorMessage.value = "Error: ${e.message}"
                }
            }
        }
    }

    // 🔹 Update Existing Product - ✅ ADDED ADMIN CHECK
    fun updateProduct(
        context: Context,
        navController: NavController,
        name: String,
        price: String,
        category: String,
        description: String,
        imageUri: Uri?,
        productId: String,
        oldImageUrl: String,
        isOnOffer: Boolean
    ) {
        // ✅ ADD ADMIN CHECK
        checkAdminAccess { isAdmin ->
            if (!isAdmin) {
                _isLoading.value = false
                _errorMessage.value = "Access denied. Admin privileges required."
                Toast.makeText(context, "❌ Admin access required to update products", Toast.LENGTH_LONG).show()
                return@checkAdminAccess
            }

            viewModelScope.launch(Dispatchers.IO) {
                try {
                    _isLoading.value = true
                    val newImageUrl = if (imageUri != null)
                        uploadImageToImgur(context, imageUri) ?: oldImageUrl
                    else oldImageUrl

                    val updatedProduct = Product(
                        productId = productId,
                        name = name,
                        description = description,
                        price = price,
                        imageUrl = newImageUrl,
                        category = category,
                        isOnOffer = isOnOffer
                    )

                    withContext(Dispatchers.Main) {
                        database.child(productId).setValue(updatedProduct)
                            .addOnSuccessListener {
                                _isLoading.value = false
                                fetchProducts()
                                fetchOfferProducts()
                                navController.popBackStack()
                                Toast.makeText(context, "✅ Product updated successfully", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                _isLoading.value = false
                                _errorMessage.value = "Failed to update product: ${it.message}"
                            }
                    }
                } catch (e: Exception) {
                    _isLoading.value = false
                    _errorMessage.value = "Error updating product: ${e.message}"
                }
            }
        }
    }

    // 🔹 Delete Product - ✅ ADDED ADMIN CHECK
    fun deleteProduct(
        productId: String,
        context: Context,
        navController: NavController
    ) {
        // ✅ ADD ADMIN CHECK
        checkAdminAccess { isAdmin ->
            if (!isAdmin) {
                Toast.makeText(context, "❌ Admin access required to delete products", Toast.LENGTH_LONG).show()
                return@checkAdminAccess
            }

            viewModelScope.launch {
                try {
                    database.child(productId).removeValue().addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(context, "✅ Product deleted successfully", Toast.LENGTH_SHORT).show()
                            fetchProducts()
                            navController.popBackStack()
                        } else {
                            Toast.makeText(context, "❌ Failed to delete product", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    // 🔹 Upload Image to Imgur
    suspend fun uploadImageToImgur(context: Context, uri: Uri): String? {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val file = File(context.cacheDir, "temp_${System.currentTimeMillis()}.jpg")
                inputStream?.use { it.copyTo(file.outputStream()) }

                val client = OkHttpClient.Builder()
                    .addInterceptor(HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BODY
                    }).build()

                val imgurService = Retrofit.Builder()
                    .baseUrl("https://api.imgur.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build()
                    .create(ImgurService::class.java)

                val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("image", file.name, requestFile)

                val response = imgurService.uploadImage(imagePart, "Client-ID cb719f3230afca9")

                if (response.isSuccessful && response.body()?.success == true)
                    response.body()?.data?.link
                else null
            } catch (e: Exception) {
                Log.e("ProductViewModel", "Imgur upload failed: ${e.message}")
                null
            }
        }
    }
}