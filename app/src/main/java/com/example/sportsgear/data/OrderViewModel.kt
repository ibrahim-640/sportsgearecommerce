package com.example.sportsgear.data

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportsgear.models.CartItem
import com.example.sportsgear.models.Order
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OrderViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    private val _orders = mutableStateListOf<Order>()
    val orders: List<Order> = _orders

    init {
        checkAndFetchOrders() // ✅ Automatically fetch correct orders based on user role
    }

    /** ✅ Decide whether to fetch all orders (admin) or only user orders */
    private fun checkAndFetchOrders() {
        val uid = auth.currentUser?.uid ?: return
        database.child("users").child(uid).child("isAdmin")
            .get()
            .addOnSuccessListener { snapshot ->
                val isAdmin = snapshot.getValue(Boolean::class.java) ?: false
                if (isAdmin) {
                    fetchAllOrders()
                } else {
                    fetchUserOrders()
                }
            }
            .addOnFailureListener {
                Log.e("OrderViewModel", "❌ Failed to determine user role: ${it.message}")
            }
    }

    /** ✅ Fetch all orders for the logged-in user */
    private fun fetchUserOrders() {
        val uid = auth.currentUser?.uid ?: return
        val userOrdersRef = database.child("Orders").child(uid)

        userOrdersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                _orders.clear()
                for (orderSnap in snapshot.children) {
                    val order = orderSnap.getValue(Order::class.java)
                    if (order != null) _orders.add(order)
                }
                Log.d("OrderViewModel", "✅ User orders fetched successfully")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("OrderViewModel", "❌ Failed to fetch user orders: ${error.message}")
            }
        })
    }

    /** ✅ Fetch all orders for admin dashboard */
    private fun fetchAllOrders() {
        val allOrdersRef = database.child("Orders")

        allOrdersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                _orders.clear()
                for (userSnap in snapshot.children) {
                    for (orderSnap in userSnap.children) {
                        val order = orderSnap.getValue(Order::class.java)
                        if (order != null) _orders.add(order)
                    }
                }
                Log.d("OrderViewModel", "✅ Admin fetched all orders successfully")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("OrderViewModel", "❌ Failed to fetch all orders: ${error.message}")
            }
        })
    }

    /** ✅ Create new order (normal users only, after payment success) */
    fun createOrderFromSuccessScreen(
        userId: String,
        totalAmount: Double,
        paymentMethod: String,
        orderNumber: String
    ) {
        if (userId.isBlank()) return

        val currentDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        // Create order object
        val order = Order(
            orderId = orderNumber,
            userId = userId,
            total = totalAmount,
            orderDate = currentDate,
            timestamp = System.currentTimeMillis(),
            status = "Pending",
            paymentMethod = paymentMethod,
            items = emptyList() // Optional: attach cart items if needed
        )

        // Save to Firebase
        val orderRef = FirebaseDatabase.getInstance().reference
            .child("Orders")
            .child(userId)
            .child(orderNumber)

        orderRef.setValue(order)
            .addOnSuccessListener {
                Log.d("OrderViewModel", "✅ Order saved successfully: $orderNumber")
            }
            .addOnFailureListener { e ->
                Log.e("OrderViewModel", "❌ Failed to save order: ${e.message}")
            }
    }

    /** ✅ Notify all admins about new order */
    private fun notifyAdmins(order: Order) {
        val usersRef = database.child("users")
        usersRef.get().addOnSuccessListener { snapshot ->
            for (userSnap in snapshot.children) {
                val isAdmin = userSnap.child("isAdmin").getValue(Boolean::class.java) ?: false
                if (isAdmin) {
                    val adminId = userSnap.key ?: continue
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
            }
        }.addOnFailureListener { e ->
            Log.e("OrderViewModel", "❌ Failed to fetch admins: ${e.message}")
        }
    }
}
