package com.example.sportsgear.data

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.sportsgear.models.CartItem
import com.example.sportsgear.models.Product
import com.google.firebase.database.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CartViewModel : ViewModel() {

    private val database = FirebaseDatabase.getInstance().reference

    companion object {
        const val FREE_SHIPPING_THRESHOLD = 5000.0
        const val SHIPPING_COST = 250.0
        const val TAX_RATE = 0.05
    }

    // Cart items
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun clearMessage() {
        _message.value = null
    }

    // Order summary — this is now the SINGLE source of truth for subtotal/tax/
    // shipping/total. CartScreen displays these directly, and CheckoutScreen
    // now reads them too instead of recalculating independently.
    private val _subtotal = mutableStateOf(0.0)
    val subtotal: State<Double> = _subtotal

    private val _shipping = mutableStateOf(SHIPPING_COST)
    val shipping: State<Double> = _shipping

    private val _tax = mutableStateOf(0.0)
    val tax: State<Double> = _tax

    private val _total = mutableStateOf(0.0)
    val total: State<Double> = _total

    // ✅ REMOVED — selectedPaymentMethod, isProcessing, paymentSuccess,
    // shippingName/Address/Phone state, updateShippingInfo, selectPaymentMethod,
    // initiatePayment, initiateMpesaPayment, simulateCardPayment,
    // simulatePayPalPayment, simulateBankTransfer, completeOrder.
    // All of this was dead code — PaymentScreen only ever talks to
    // PaymentViewModel, never to this class, for anything payment-related.
    // PaymentViewModel is now the single owner of the payment flow, and
    // SuccessScreen is the single place that writes the order record.

    private var cartListener: ValueEventListener? = null
    private var cartRef: DatabaseReference? = null

    // ------------------------- CART MANAGEMENT -------------------------

    fun addToCart(userId: String, product: Product) {
        if (userId.isBlank()) {
            _message.value = "Please log in to add to cart"
            return
        }

        _isLoading.value = true
        val cartItemRef = database.child("Cart").child(userId).child(product.productId)

        cartItemRef.get()
            .addOnSuccessListener { snapshot ->
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
                cartItemRef.setValue(updatedItem)
                    .addOnSuccessListener {
                        _isLoading.value = false
                        _message.value = "${product.name} added to cart"
                    }
                    .addOnFailureListener { e ->
                        _isLoading.value = false
                        _message.value = "Failed to add to cart"
                        Log.e("CartViewModel", "Failed to add: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _message.value = "Failed to add to cart"
                Log.e("CartViewModel", "Failed to read cart: ${e.message}")
            }
    }

    fun removeFromCart(userId: String, productId: String) {
        database.child("Cart").child(userId).child(productId)
            .removeValue()
            .addOnFailureListener { e ->
                _message.value = "Failed to remove item"
                Log.e("CartViewModel", "Remove failed: ${e.message}")
            }
    }

    fun loadCartItems(userId: String) {
        if (userId.isBlank()) return

        cartRef?.let { ref ->
            cartListener?.let { listener ->
                ref.removeEventListener(listener)
            }
        }

        _isLoading.value = true

        val ref = database.child("Cart").child(userId)
        cartRef = ref

        cartListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = snapshot.children.mapNotNull {
                    it.getValue(CartItem::class.java)
                }
                _cartItems.value = items
                calculateTotals()
                _isLoading.value = false
                Log.d("CartViewModel", "Cart loaded: ${items.size} items")
            }

            override fun onCancelled(error: DatabaseError) {
                _isLoading.value = false
                _message.value = "Failed to load cart"
                Log.e("CartViewModel", "Load failed: ${error.message}")
            }
        }

        ref.addValueEventListener(cartListener!!)
    }

    fun clearCart(userId: String) {
        database.child("Cart").child(userId).removeValue()
            .addOnSuccessListener {
                _cartItems.value = emptyList()
                calculateTotals()
            }
            .addOnFailureListener { e ->
                _message.value = "Failed to clear cart"
                Log.e("CartViewModel", "Clear failed: ${e.message}")
            }
    }

    fun updateQuantity(userId: String, productId: String, newQuantity: Int) {
        if (newQuantity < 1) return
        database.child("Cart").child(userId).child(productId)
            .child("quantity").setValue(newQuantity)
            .addOnSuccessListener {
                _cartItems.value = _cartItems.value.map { item ->
                    if (item.productId == productId) item.copy(quantity = newQuantity) else item
                }
                calculateTotals()
            }
            .addOnFailureListener { e ->
                _message.value = "Failed to update quantity"
                Log.e("CartViewModel", "Update failed: ${e.message}")
            }
    }

    fun updateCartProduct(
        userId: String,
        productId: String,
        updatedProduct: CartItem,
        onSuccess: () -> Unit = {}
    ) {
        database.child("Cart").child(userId).child(productId)
            .setValue(updatedProduct)
            .addOnSuccessListener {
                _cartItems.value = _cartItems.value.map { item ->
                    if (item.productId == productId) updatedProduct else item
                }
                calculateTotals()
                _message.value = "Cart updated successfully"
                onSuccess()
            }
            .addOnFailureListener { e ->
                _message.value = "Failed to update item"
                Log.e("CartViewModel", "Update failed: ${e.message}")
            }
    }
    // Add this function — exposes the existing cleanup logic
// so AppNavHost can trigger it explicitly on logout
    fun detachListener() {
        cartRef?.let { ref ->
            cartListener?.let { listener ->
                ref.removeEventListener(listener)
            }
        }
        cartRef = null
        cartListener = null
        _cartItems.value = emptyList()
        _isLoading.value = false
    }

    // ------------------------- ORDER SUMMARY -------------------------

    private fun calculateTotals() {
        val sub = _cartItems.value.sumOf {
            (it.price.toDoubleOrNull() ?: 0.0) * it.quantity
        }
        val taxAmount = sub * TAX_RATE
        val shippingCost = if (sub > FREE_SHIPPING_THRESHOLD) 0.0 else SHIPPING_COST
        val totalAmount = sub + taxAmount + shippingCost

        _subtotal.value = sub
        _tax.value = taxAmount
        _shipping.value = shippingCost
        _total.value = totalAmount
    }

    // ------------------------- CLEANUP -------------------------

    override fun onCleared() {
        super.onCleared()
        cartRef?.let { ref ->
            cartListener?.let { listener ->
                ref.removeEventListener(listener)
            }
        }
    }
}