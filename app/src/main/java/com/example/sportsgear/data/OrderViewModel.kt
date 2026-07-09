package com.example.sportsgear.data

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.sportsgear.models.CartItem
import com.example.sportsgear.models.Order
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OrderViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    private val _orders = mutableStateListOf<Order>()
    val orders: List<Order> = _orders

    // ✅ NEW — was missing entirely. OrderHistoryScreen had no way to show a
    // spinner, so it briefly flashed "No past orders" before the Firebase
    // listener returned real data — same class of bug fixed earlier in CartScreen.
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // ✅ NEW — stored so listeners can actually be detached. Previously
    // fetchUserOrders/fetchAllOrders attached listeners with no reference
    // kept anywhere, and this class had no onCleared() at all — permanent leak.
    private var ordersRef: DatabaseReference? = null
    private var ordersListener: ValueEventListener? = null

    // ✅ FIX — removed the old init { checkAndFetchOrders() } block entirely.
    // It queried database.child("users")... (lowercase) while AuthViewModel
    // writes to "Users" (capitalized) — a path that never had data, so the
    // check always silently resolved to false. Worse, even with correct
    // casing it would've been checking the WRONG field: the rest of the app's
    // admin status comes from the dedicated Admin/{uid} node via
    // AuthViewModel.isAdmin, not from a Users/{uid}/isAdmin field. Rather than
    // re-deriving admin status here (a second, disagreeing source of truth),
    // the caller now passes it in directly.
    fun loadOrders(isAdmin: Boolean) {
        if (isAdmin) fetchAllOrders() else fetchUserOrders()
    }

    private fun fetchUserOrders() {
        val uid = auth.currentUser?.uid ?: return
        detachListener()
        _isLoading.value = true

        val ref = database.child("Orders").child(uid)
        ordersRef = ref
        ordersListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                _orders.clear()
                for (orderSnap in snapshot.children) {
                    orderSnap.getValue(Order::class.java)?.let { _orders.add(it) }
                }
                _isLoading.value = false
                Log.d("OrderViewModel", "✅ User orders fetched successfully")
            }

            override fun onCancelled(error: DatabaseError) {
                _isLoading.value = false
                Log.e("OrderViewModel", "❌ Failed to fetch user orders: ${error.message}")
            }
        }
        ref.addValueEventListener(ordersListener!!)
    }

    private fun fetchAllOrders() {
        detachListener()
        _isLoading.value = true

        val ref = database.child("Orders")
        ordersRef = ref
        ordersListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                _orders.clear()
                for (userSnap in snapshot.children) {
                    for (orderSnap in userSnap.children) {
                        orderSnap.getValue(Order::class.java)?.let { _orders.add(it) }
                    }
                }
                _isLoading.value = false
                Log.d("OrderViewModel", "✅ Admin fetched all orders successfully")
            }

            override fun onCancelled(error: DatabaseError) {
                _isLoading.value = false
                Log.e("OrderViewModel", "❌ Failed to fetch all orders: ${error.message}")
            }
        }
        ref.addValueEventListener(ordersListener!!)
    }

    private fun detachListener() {
        ordersRef?.let { ref -> ordersListener?.let { ref.removeEventListener(it) } }
    }

    /** Create new order, after payment success */
    fun createOrderFromSuccessScreen(
        userId: String,
        totalAmount: Double,
        paymentMethod: String,
        orderNumber: String,
        items: List<CartItem> = emptyList()
        // ✅ FIX — was hardcoded to emptyList() at the call site with a
        // "// Optional: attach cart items if needed" comment. Every order's
        // item list was permanently empty, so OrderHistoryScreen's
        // "Items:" section never showed anything. Now accepts the real
        // cart snapshot — see SuccessScreen.kt, which now passes it in
        // before clearing the cart.
    ) {
        if (userId.isBlank()) return

        val currentDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        val order = Order(
            orderId = orderNumber,
            userId = userId,
            total = totalAmount,
            orderDate = currentDate,
            timestamp = System.currentTimeMillis(),
            status = "Pending",
            paymentMethod = paymentMethod,
            items = items
        )

        val orderRef = database.child("Orders").child(userId).child(orderNumber)
        orderRef.setValue(order)
            .addOnSuccessListener {
                Log.d("OrderViewModel", "✅ Order saved successfully: $orderNumber")
            }
            .addOnFailureListener { e ->
                Log.e("OrderViewModel", "❌ Failed to save order: ${e.message}")
            }
    }

    /**
     * Notify all admins about a new order.
     * ✅ Fixed to read from the canonical "Admin" node instead of scanning
     * "users" for an isAdmin == true field (same wrong-scheme problem as the
     * old checkAndFetchOrders). NOTE: still not called anywhere — it was dead
     * code before this fix too. Wiring it up would start writing real
     * notification records and is a behavior change beyond what was asked
     * for this round, so I've left it disconnected. Call notifyAdmins(order)
     * inside createOrderFromSuccessScreen's onSuccess block if/when you want
     * this live.
     */
    private fun notifyAdmins(order: Order) {
        val adminRef = database.child("Admin")
        adminRef.get().addOnSuccessListener { snapshot ->
            for (adminSnap in snapshot.children) {
                val adminId = adminSnap.key ?: continue
                val notifRef = database.child("notifications").child(adminId).push()
                val notification = mapOf(
                    "title" to "🛍️ New Order Alert",
                    "message" to "A new order (${order.orderId}) worth KES ${order.total} has been placed.",
                    "timestamp" to System.currentTimeMillis()
                )
                notifRef.setValue(notification)
                    .addOnSuccessListener {
                        Log.d("OrderViewModel", "✅ Admin $adminId notified of order ${order.orderId}")
                    }
                    .addOnFailureListener { e ->
                        Log.e("OrderViewModel", "❌ Failed to notify admin $adminId: ${e.message}")
                    }
            }
        }.addOnFailureListener { e ->
            Log.e("OrderViewModel", "❌ Failed to fetch admins: ${e.message}")
        }
    }

    override fun onCleared() {
        super.onCleared()
        detachListener()
    }
}