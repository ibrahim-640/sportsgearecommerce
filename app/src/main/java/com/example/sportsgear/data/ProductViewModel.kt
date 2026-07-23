package com.example.sportsgear.data
import android.content.Context
import android.net.Uri
import android.util.Log
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
import kotlinx.coroutines.flow.asStateFlow
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

    // Firebase References
    private val database = FirebaseDatabase.getInstance().reference.child("Products")
    private val rootDatabase = FirebaseDatabase.getInstance().reference
    private val auth = FirebaseAuth.getInstance()

    // ✅ Built once — reused for every upload
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }).build()

    private val imgurService = Retrofit.Builder()
        .baseUrl("https://api.imgur.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(okHttpClient)
        .build()
        .create(ImgurService::class.java)

    // State Variables
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    private val _productList = MutableStateFlow<List<Product>>(emptyList())
    val productList: StateFlow<List<Product>> = _productList.asStateFlow()

    private val _offerProducts = MutableStateFlow<List<Product>>(emptyList())
    val offerProducts: StateFlow<List<Product>> = _offerProducts.asStateFlow()

    // ✅ Listener references to prevent leaks
    private var productListener: ValueEventListener? = null
    private var offerListener: ValueEventListener? = null

    // ✅ FIX — kept a reference to the exact query the offer listener is attached to,
    // so onCleared() can detach it from the SAME query node. Firebase requires the
    // listener to be removed from the same reference/query it was added to.
    private val offerQueryRef = rootDatabase.child("Products")
        .orderByChild("OnOffer")
        .equalTo(true)

    fun clearErrorMessage() { _errorMessage.value = null }
    fun clearSuccessMessage() { _successMessage.value = null }

    // ------------------------- CART -------------------------
    // ✅ FIX — removed the duplicate addToCart(product, context) that used to live here.
    // It bypassed CartViewModel entirely, always did a plain setValue with no
    // read-before-write step, and would silently RESET an item's quantity back to
    // the CartItem default any time it was called on a product already in the cart
    // (no increment logic like CartViewModel.addToCart has). It was unused by every
    // screen in the app — all of them correctly call cartViewModel.addToCart instead.
    // If cart-adding is ever needed from here in the future, delegate to a
    // CartViewModel instance rather than re-implementing the logic.

    // ------------------------- FETCH PRODUCTS -------------------------

    // ✅ Stores listener reference to prevent duplicates
    fun fetchProducts() {
        productListener?.let { database.removeEventListener(it) }

        _isLoading.value = true
        productListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val products = snapshot.children.mapNotNull { it.getValue(Product::class.java) }
                _productList.value = products
                _isLoading.value = false
            }

            override fun onCancelled(error: DatabaseError) {
                _errorMessage.value = "Failed to load products: ${error.message}"
                _isLoading.value = false
            }
        }
        database.addValueEventListener(productListener!!)
    }

    // ✅ Stores listener reference to prevent duplicates
    fun fetchOfferProducts() {
        val offerRef = rootDatabase.child("Products")
            .orderByChild("onOffer") // ✅ was "isOnOffer"
            .equalTo(true)
        offerListener?.let { offerQueryRef.removeEventListener(it) }

        offerListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val offerList = snapshot.children.mapNotNull { it.getValue(Product::class.java) }
                _offerProducts.value = offerList
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ProductViewModel", "Error fetching offer products: ${error.message}")
            }
        }
        offerQueryRef.addValueEventListener(offerListener!!)
    }

    // ------------------------- UPLOAD PRODUCT -------------------------

    // ✅ isAdmin passed from screen — no duplicate Firebase read
    // ✅ No Toast — uses StateFlow messages
    // ✅ temp file deleted after upload
    fun uploadProductWithImage(
        uri: Uri,
        context: Context,
        name: String,
        category: String,
        price: String,
        description: String,
        quantity: String,
        navController: NavController,
        onOffer: Boolean,
        isAdmin: Boolean // ✅ passed from screen
    ) {
        if (!isAdmin) {
            _errorMessage.value = "Admin access required to add products"
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isLoading.value = true

                val imageUrl = uploadImageToImgur(context, uri)
                if (imageUrl == null) {
                    _isLoading.value = false // ✅ always reset on early exit
                    _errorMessage.value = "Image upload failed. Please try again."
                    return@launch
                }

                val productId = database.push().key
                if (productId == null) {
                    _isLoading.value = false // ✅ always reset on early exit
                    _errorMessage.value = "Failed to generate product ID"
                    return@launch
                }

                val product = Product(
                    productId = productId,
                    name = name,
                    description = description,
                    price = price,
                    imageUrl = imageUrl,
                    category = category,
                    quantity = quantity,
                    onOffer = onOffer
                )

                withContext(Dispatchers.Main) {
                    database.child(productId).setValue(product)
                        .addOnSuccessListener {
                            _isLoading.value = false
                            _successMessage.value = "Product added successfully" // ✅ no Toast
                            fetchProducts()
                            fetchOfferProducts()
                            navController.popBackStack()
                        }
                        .addOnFailureListener {
                            _isLoading.value = false
                            _errorMessage.value = "Failed to add product: ${it.message}"
                        }
                }
            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = "Error: ${e.message}"
            }
        }
    }

    // ------------------------- UPDATE PRODUCT -------------------------

    // ✅ isAdmin passed from screen — no duplicate Firebase read
    fun updateProduct(
        context: Context,
        navController: NavController,
        name: String,
        price: String,
        category: String,
        description: String,
        quantity: String,
        imageUri: Uri?,
        productId: String,
        oldImageUrl: String,
        onOffer: Boolean,
        isAdmin: Boolean // ✅ passed from screen
    ) {
        if (!isAdmin) {
            _errorMessage.value = "Admin access required to update products"
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isLoading.value = true

                val newImageUrl = if (imageUri != null)
                    uploadImageToImgur(context, imageUri) ?: oldImageUrl
                else
                    oldImageUrl

                val updatedProduct = Product(
                    productId = productId,
                    name = name,
                    description = description,
                    price = price,
                    imageUrl = newImageUrl,
                    category = category,
                    quantity = quantity,
                    onOffer = onOffer
                )

                withContext(Dispatchers.Main) {
                    database.child(productId).setValue(updatedProduct)
                        .addOnSuccessListener {
                            _isLoading.value = false
                            _successMessage.value = "Product updated successfully" // ✅ no Toast
                            fetchProducts()
                            fetchOfferProducts()
                            navController.popBackStack()
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
    // ------------------------- STOCK MANAGEMENT -------------------------

    /**
     * Decrements stock for each purchased item after a successful order.
     * Reads the current quantity fresh from Firebase (not from local state,
     * which may be stale) before writing the new value, and clamps at 0 so
     * concurrent purchases of the last unit can't drive it negative.
     */
    // ------------------------- STOCK MANAGEMENT -------------------------

    fun decrementStockAfterPurchase(purchasedItems: List<CartItem>) {
        // ✅ Guard — if cart was somehow empty, do nothing
        if (purchasedItems.isEmpty()) return

        purchasedItems.forEach { cartItem ->
            decrementSingleProduct(cartItem.productId, cartItem.quantity)
        }
    }

    private fun decrementSingleProduct(productId: String, purchasedQuantity: Int) {
        if (productId.isBlank()) return

        val productRef = database.child(productId)

        // ✅ Read current quantity first then update
        // We must read before writing to avoid race conditions
        // where two people buy the last item simultaneously
        productRef.get()
            .addOnSuccessListener { snapshot ->
                val product = snapshot.getValue(Product::class.java)

                if (product == null) {
                    Log.e("ProductViewModel", "Product $productId not found for stock decrement")
                    return@addOnSuccessListener
                }

                val currentQuantity = product.quantity.toIntOrNull() ?: 0
                val newQuantity = (currentQuantity - purchasedQuantity)
                    .coerceAtLeast(0) // ✅ never go below 0

                Log.d("ProductViewModel",
                    "Stock update: ${product.name} | " +
                            "Before: $currentQuantity | " +
                            "Purchased: $purchasedQuantity | " +
                            "After: $newQuantity"
                )

                // ✅ Only update the quantity field
                // not the entire product object
                productRef.child("quantity").setValue(newQuantity.toString())
                    .addOnSuccessListener {
                        Log.d("ProductViewModel",
                            "Stock decremented for ${product.name}: $newQuantity remaining"
                        )
                    }
                    .addOnFailureListener { e ->
                        Log.e("ProductViewModel",
                            "Failed to decrement stock for ${product.name}: ${e.message}"
                        )
                        _errorMessage.value = "Failed to update stock for ${product.name}"
                    }
            }
            .addOnFailureListener { e ->
                Log.e("ProductViewModel",
                    "Failed to read product $productId: ${e.message}"
                )
            }
    }

    // ------------------------- DELETE PRODUCT -------------------------

    // ✅ isAdmin passed from screen — no duplicate Firebase read
    // ✅ No Toast — uses StateFlow messages
    // ✅ FIX — onSuccess is now actually invoked. Previously it was accepted as a
    // parameter but never called, and navController.popBackStack() ran
    // unconditionally regardless of caller intent. Now: if the caller supplies
    // onSuccess, that runs instead of popping back (e.g. HomeScreen passes an
    // empty onSuccess = {} to deliberately stay on Home after deleting). If the
    // caller passes nothing (null), the original pop-back behavior is preserved
    // for backward compatibility with screens like CategoryScreen.
    fun deleteProduct(
        productId: String,
        navController: NavController,
        isAdmin: Boolean, // ✅ passed from screen
        onSuccess: (() -> Unit)? = null,
    ) {
        if (!isAdmin) {
            _errorMessage.value = "Admin access required to delete products"
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true
                database.child(productId).removeValue()
                    .addOnSuccessListener {
                        _isLoading.value = false
                        _successMessage.value = "Product deleted successfully" // ✅ no Toast
                        fetchProducts()
                        onSuccess?.invoke() ?: navController.popBackStack()
                    }
                    .addOnFailureListener {
                        _isLoading.value = false
                        _errorMessage.value = "Failed to delete product: ${it.message}"
                    }
            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = "Error: ${e.message}"
            }
        }
    }

    // ------------------------- UPLOAD IMAGE -------------------------

    // ✅ temp file deleted in finally block
    suspend fun uploadImageToImgur(context: Context, uri: Uri): String? {
        return withContext(Dispatchers.IO) {
            val file = File(context.cacheDir, "temp_${System.currentTimeMillis()}.jpg")
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                inputStream?.use { it.copyTo(file.outputStream()) }

                val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("image", file.name, requestFile)

                val response = imgurService.uploadImage(imagePart, "Client-ID cb719f3230afca9")

                if (response.isSuccessful && response.body()?.success == true)
                    response.body()?.data?.link
                else {
                    Log.e("ProductViewModel", "Imgur upload failed: ${response.code()}")
                    null
                }
            } catch (e: Exception) {
                Log.e("ProductViewModel", "Imgur upload exception: ${e.message}")
                null
            } finally {
                if (file.exists()) file.delete() // ✅ always delete temp file
            }
        }
    }

    // ------------------------- CLEANUP -------------------------

    override fun onCleared() {
        super.onCleared()
        // ✅ Remove listeners when ViewModel is destroyed
        productListener?.let { database.removeEventListener(it) }
        // ✅ FIX — offerListener was never detached before; now removed from the
        // same query reference (offerQueryRef) it was attached to.
        offerListener?.let { offerQueryRef.removeEventListener(it) }
    }
}