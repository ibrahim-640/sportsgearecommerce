package com.example.sportsgear.data

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportsgear.network.MpesaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PaymentViewModel(
    private val mpesaRepository: MpesaRepository = MpesaRepository()
) : ViewModel() {

    // -------------------- STATES --------------------
    private val _selectedPaymentMethod = mutableStateOf("M-Pesa")
    val selectedPaymentMethod: State<String> = _selectedPaymentMethod

    private val _isProcessing = mutableStateOf(false)
    val isProcessing: State<Boolean> = _isProcessing

    private val _paymentSuccess = mutableStateOf(false)
    val paymentSuccess: State<Boolean> = _paymentSuccess

    private val _shippingName = mutableStateOf("")
    val shippingName: State<String> = _shippingName

    private val _shippingAddress = mutableStateOf("")
    val shippingAddress: State<String> = _shippingAddress

    private val _shippingPhone = mutableStateOf("")
    val shippingPhone: State<String> = _shippingPhone

    // -------------------- UPDATE FUNCTIONS --------------------
    fun updateShippingInfo(name: String, address: String, phone: String) {
        _shippingName.value = name
        _shippingAddress.value = address
        _shippingPhone.value = phone
    }

    fun selectPaymentMethod(method: String) {
        _selectedPaymentMethod.value = method
    }

    // -------------------- PAYMENT FUNCTIONS --------------------
    fun initiatePayment(context: Context, orderViewModel: OrderViewModel) {
        val method = _selectedPaymentMethod.value
        _isProcessing.value = true
        _paymentSuccess.value = false

        when (method) {
            "M-Pesa" -> initiateMpesaPayment(context, _shippingPhone.value, orderViewModel)
            "Credit/Debit Card" -> simulateCardPayment(context, method, orderViewModel)
            "PayPal" -> simulatePayPalPayment(context, method, orderViewModel)
            "Bank Transfer" -> simulateBankTransfer(context, method, orderViewModel)
            else -> {
                showToast(context, "Unknown payment method")
                _isProcessing.value = false
            }
        }
    }

    private fun initiateMpesaPayment(
        context: Context,
        phoneNumber: String,
        orderViewModel: OrderViewModel
    ) {
        viewModelScope.launch {
            try {
                val totalAmount = orderViewModel.orders.sumOf { it.total } // fallback, or pass explicitly
                val success = mpesaRepository.initiatePayment(phoneNumber, totalAmount.toInt())

                if (success) {
                    showToast(context, "✅ M-Pesa payment initiated successfully!")
                    completeOrder(context, "M-Pesa", orderViewModel)
                } else {
                    showToast(context, "❌ Payment failed. Please try again.")
                }
            } catch (e: Exception) {
                Log.e("PaymentViewModel", "M-Pesa Error: ${e.message}")
                showToast(context, "⚠️ ${e.message}")
            } finally {
                _isProcessing.value = false
            }
        }
    }

    private fun simulateCardPayment(
        context: Context,
        paymentMethod: String,
        orderViewModel: OrderViewModel
    ) {
        viewModelScope.launch {
            showToast(context, "💳 Processing card payment...")
            kotlinx.coroutines.delay(2000)
            showToast(context, "✅ Card payment successful!")
            completeOrder(context, paymentMethod, orderViewModel)
            _isProcessing.value = false
        }
    }

    private fun simulatePayPalPayment(
        context: Context,
        paymentMethod: String,
        orderViewModel: OrderViewModel
    ) {
        viewModelScope.launch {
            showToast(context, "🌐 Redirecting to PayPal...")
            kotlinx.coroutines.delay(2000)
            showToast(context, "✅ PayPal payment confirmed!")
            completeOrder(context, paymentMethod, orderViewModel)
            _isProcessing.value = false
        }
    }

    private fun simulateBankTransfer(
        context: Context,
        paymentMethod: String,
        orderViewModel: OrderViewModel
    ) {
        viewModelScope.launch {
            showToast(context, "🏦 Please complete bank transfer to finalize order.")
            kotlinx.coroutines.delay(3000)
            showToast(context, "✅ Bank transfer confirmed!")
            completeOrder(context, paymentMethod, orderViewModel)
            _isProcessing.value = false
        }
    }

    // -------------------- ORDER COMPLETION --------------------
    private fun completeOrder(
        context: Context,
        paymentMethod: String,
        orderViewModel: OrderViewModel
    ) {
        val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return
        val totalAmount = orderViewModel.orders.sumOf { it.total } // total from orders or pass explicitly
        val orderNumber = "SG-${(1000..9999).random()}"

        // Create order in Firebase
        orderViewModel.createOrderFromSuccessScreen(
            userId = userId,
            totalAmount = totalAmount,
            paymentMethod = paymentMethod,
            orderNumber = orderNumber
        )

        _paymentSuccess.value = true
        showToast(context, "🛍️ Order placed successfully!")
    }

    // -------------------- UTILS --------------------
    private fun showToast(context: Context, message: String) {
        viewModelScope.launch(Dispatchers.Main) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }
}
