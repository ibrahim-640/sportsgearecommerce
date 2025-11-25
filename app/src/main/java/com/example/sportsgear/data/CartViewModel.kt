package com.example.sportsgear.data

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportsgear.models.CartItem
import com.example.sportsgear.models.Product
import com.example.sportsgear.network.MpesaRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class CartViewModel : ViewModel() {
    private val database = FirebaseDatabase.getInstance().reference
    private val mpesaRepository = MpesaRepository()

    private val _cartItems = mutableStateOf<List<CartItem>>(emptyList())
    val cartItems: State<List<CartItem>> = _cartItems

    // Order summary states
    private val _subtotal = mutableStateOf(0.0)
    val subtotal: State<Double> = _subtotal

    private val _shipping = mutableStateOf(250.0)
    val shipping: State<Double> = _shipping

    private val _tax = mutableStateOf(0.0)
    val tax: State<Double> = _tax

    private val _total = mutableStateOf(0.0)
    val total: State<Double> = _total

    // Payment and checkout states
    private val _selectedPaymentMethod = mutableStateOf("M-Pesa")
    val selectedPaymentMethod: State<String> = _selectedPaymentMethod

    private val _isProcessing = mutableStateOf(false)
    val isProcessing: State<Boolean> = _isProcessing

    private val _paymentSuccess = mutableStateOf(false)
    val paymentSuccess: State<Boolean> = _paymentSuccess

    // Shipping information
    private val _shippingName = mutableStateOf("")
    val shippingName: State<String> = _shippingName

    private val _shippingAddress = mutableStateOf("")
    val shippingAddress: State<String> = _shippingAddress

    private val _shippingPhone = mutableStateOf("")
    val shippingPhone: State<String> = _shippingPhone

    // === ADD THIS DEBUG FUNCTION ===
    fun debugCartSetup() {
        val user = FirebaseAuth.getInstance().currentUser
        Log.d("DebugCart", "=== CART VIEWMODEL DEBUG ===")
        Log.d("DebugCart", "📱 Using: Firebase Realtime Database")
        Log.d("DebugCart", "👤 Current User: ${user?.uid ?: "NULL"}")
        Log.d("DebugCart", "📊 Database URL: ${FirebaseDatabase.getInstance().reference}")

        if (user != null) {
            // Test database connection - ✅ FIXED PATH
            database.child("Cart").child(user.uid).child("debug_connection").setValue(
                mapOf("test" to true, "timestamp" to System.currentTimeMillis())
            ).addOnSuccessListener {
                Log.d("DebugCart", "✅ Database connection: SUCCESS")
            }.addOnFailureListener { e ->
                Log.e("DebugCart", "❌ Database connection: FAILED - ${e.message}")
            }
        }
    }
    // === END DEBUG FUNCTION ===

    // ------------------------- CART MANAGEMENT -------------------------

    fun addToCart(userId: String, product: Product) {
        Log.d("DebugCart", "🔄 addToCart called:")
        Log.d("DebugCart", "   User: $userId")
        Log.d("DebugCart", "   Product: ${product.productId} - ${product.name}")

        // ✅ FIXED PATH: "carts" → "Cart"
        val cartRef = database.child("Cart").child(userId).child(product.productId)
        cartRef.get().addOnSuccessListener { snapshot ->
            val currentItem = snapshot.getValue(CartItem::class.java)
            val newQuantity = (currentItem?.quantity ?: 0) + 1
            val updatedItem = CartItem(
                productId = product.productId,
                name = product.name,
                imageUrl = product.imageUrl,
                price = product.price,
                quantity = newQuantity,
                category = product.category
            )

            Log.d("DebugCart", "📝 Writing to cart:")
            Log.d("DebugCart", "   Path: Cart/$userId/${product.productId}") // ✅ Updated log
            Log.d("DebugCart", "   Data: $updatedItem")

            cartRef.setValue(updatedItem)
                .addOnSuccessListener {
                    Log.d("DebugCart", "✅ Successfully added to cart")
                }
                .addOnFailureListener { e ->
                    Log.e("DebugCart", "❌ Failed to add to cart: ${e.message}")
                }
        }
            .addOnFailureListener { e ->
                Log.e("DebugCart", "❌ Failed to read cart: ${e.message}")
            }
    }

    fun removeFromCart(userId: String, productId: String) {
        // ✅ FIXED PATH: "carts" → "Cart"
        database.child("Cart").child(userId).child(productId).removeValue()
    }

    fun loadCartItems(userId: String) {
        // ✅ FIXED PATH: "carts" → "Cart"
        val ref = database.child("Cart").child(userId)
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = snapshot.children.mapNotNull { it.getValue(CartItem::class.java) }
                _cartItems.value = items
                calculateTotals()
                Log.d("DebugCart", "📥 Cart loaded: ${items.size} items")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("CartViewModel", "Failed to load cart: ${error.message}")
            }
        })
    }

    fun clearCart(userId: String) {
        // ✅ FIXED PATH: "carts" → "Cart"
        val userCartRef = database.child("Cart").child(userId)
        userCartRef.removeValue()
            .addOnSuccessListener { _cartItems.value = emptyList(); calculateTotals() }
            .addOnFailureListener { e ->
                Log.e("CartViewModel", "❌ Failed to clear cart: ${e.message}")
            }
    }

    fun updateQuantity(userId: String, productId: String, newQuantity: Int) {
        if (newQuantity < 1) return
        // ✅ FIXED PATH: "carts" → "Cart"
        val ref = database.child("Cart").child(userId).child(productId)

        ref.child("quantity").setValue(newQuantity)
            .addOnSuccessListener {
                _cartItems.value = _cartItems.value.map { item ->
                    if (item.productId == productId) item.copy(quantity = newQuantity) else item
                }
                calculateTotals()
            }
            .addOnFailureListener { e ->
                Log.e("CartViewModel", "❌ Failed to update quantity: ${e.message}")
            }
    }

    // ✅ New function to update a cart product fully
    fun updateCartProduct(userId: String, productId: String, updatedProduct: CartItem) {
        // ✅ FIXED PATH: "carts" → "Cart"
        val ref = database.child("Cart").child(userId).child(productId)
        ref.setValue(updatedProduct)
            .addOnSuccessListener {
                _cartItems.value = _cartItems.value.map { item ->
                    if (item.productId == productId) updatedProduct else item
                }
                calculateTotals()
            }
            .addOnFailureListener { e ->
                Log.e("CartViewModel", "❌ Failed to update product: ${e.message}")
            }
    }

    // ------------------------- ORDER SUMMARY CALCULATION -------------------------

    private fun calculateTotals() {
        val sub = _cartItems.value.sumOf {
            (it.price.toDoubleOrNull() ?: 0.0) * it.quantity
        }
        val taxAmount = sub * 0.05 // 5% tax
        val shippingCost = if (sub > 5000) 0.0 else 250.0
        val totalAmount = sub + taxAmount + shippingCost

        _subtotal.value = sub
        _tax.value = taxAmount
        _shipping.value = shippingCost
        _total.value = totalAmount
    }

    // ------------------------- SHIPPING INFO -------------------------

    fun updateShippingInfo(name: String, address: String, phone: String) {
        _shippingName.value = name
        _shippingAddress.value = address
        _shippingPhone.value = phone
    }

    // ------------------------- PAYMENT SELECTION -------------------------

    fun selectPaymentMethod(method: String) {
        _selectedPaymentMethod.value = method
    }

    // ------------------------- PAYMENT HANDLING -------------------------

    fun initiatePayment(context: Context, orderViewModel: OrderViewModel) {
        val method = _selectedPaymentMethod.value
        _isProcessing.value = true
        _paymentSuccess.value = false

        when (method) {
            "M-Pesa" -> initiateMpesaPayment(context, _shippingPhone.value, _total.value.toString(), orderViewModel)
            "Credit/Debit Card" -> simulateCardPayment(context, _total.value.toString(), method, orderViewModel)
            "PayPal" -> simulatePayPalPayment(context, _total.value.toString(), method, orderViewModel)
            "Bank Transfer" -> simulateBankTransfer(context, _total.value.toString(), method, orderViewModel)
        }
    }

    fun initiateMpesaPayment(
        context: Context,
        phoneNumber: String,
        amount: String,
        orderViewModel: OrderViewModel
    ) {
        viewModelScope.launch {
            try {
                val success = mpesaRepository.initiatePayment(phoneNumber, amount.toIntOrNull() ?: 1)

                if (success) {
                    showToast(context, "✅ M-Pesa payment initiated successfully!")
                    completeOrder(context, amount, "M-Pesa", orderViewModel)
                } else {
                    showToast(context, "❌ Payment failed. Please try again.")
                }
            } catch (e: Exception) {
                Log.e("CartViewModel", "M-Pesa Error: ${e.message}")
                showToast(context, "⚠️ ${e.message}")
            } finally {
                _isProcessing.value = false
            }
        }
    }

    private fun simulateCardPayment(
        context: Context,
        amount: String,
        paymentMethod: String,
        orderViewModel: OrderViewModel
    ) {
        viewModelScope.launch {
            showToast(context, "💳 Processing card payment...")
            kotlinx.coroutines.delay(2000)
            showToast(context, "✅ Card payment successful!")
            completeOrder(context, amount, paymentMethod, orderViewModel)
        }
    }

    private fun simulatePayPalPayment(
        context: Context,
        amount: String,
        paymentMethod: String,
        orderViewModel: OrderViewModel
    ) {
        viewModelScope.launch {
            showToast(context, "🌐 Redirecting to PayPal...")
            kotlinx.coroutines.delay(2000)
            showToast(context, "✅ PayPal payment confirmed!")
            completeOrder(context, amount, paymentMethod, orderViewModel)
        }
    }

    private fun simulateBankTransfer(
        context: Context,
        amount: String,
        paymentMethod: String,
        orderViewModel: OrderViewModel
    ) {
        viewModelScope.launch {
            showToast(context, "🏦 Please complete bank transfer to finalize order.")
            kotlinx.coroutines.delay(3000)
            showToast(context, "✅ Bank transfer confirmed!")
            completeOrder(context, amount, paymentMethod, orderViewModel)
        }
    }

    // ------------------------- COMPLETE ORDER -------------------------

    private fun completeOrder(
        context: Context,
        amount: String,
        paymentMethod: String,
        orderViewModel: OrderViewModel
    ) {
        val cartItemsList = _cartItems.value
        if (cartItemsList.isNotEmpty()) {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val totalAmount = amount.toDoubleOrNull() ?: 0.0
            val orderNumber = "SG-${(1000..9999).random()}" // Generate random order number

            // Call OrderViewModel function
            orderViewModel.createOrderFromSuccessScreen(
                userId = userId,
                totalAmount = totalAmount,
                paymentMethod = paymentMethod,
                orderNumber = orderNumber
            )

            // Clear cart after order - ✅ FIXED PATH: "carts" → "Cart"
            clearCart(userId)

            _paymentSuccess.value = true
            showToast(context, "🛍️ Order placed successfully!")
        } else {
            showToast(context, "⚠️ Cart is empty!")
        }
    }

    // ------------------------- UTILS -------------------------

    private fun showToast(context: Context, message: String) {
        viewModelScope.launch(Dispatchers.Main) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }
}