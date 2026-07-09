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

    private val _selectedPaymentMethod = mutableStateOf("M-Pesa")
    val selectedPaymentMethod: State<String> = _selectedPaymentMethod

    private val _isProcessing = mutableStateOf(false)
    val isProcessing: State<Boolean> = _isProcessing

    private val _paymentSuccess = mutableStateOf(false)
    val paymentSuccess: State<Boolean> = _paymentSuccess

    // ✅ REMOVED — shippingName/Address/Phone + updateShippingInfo. Nothing ever
    // called updateShippingInfo, so _shippingPhone stayed "" forever and every
    // M-Pesa request was silently sent with an empty phone number. Phone is now
    // passed explicitly into initiatePayment() instead of read from this state.

    fun selectPaymentMethod(method: String) {
        _selectedPaymentMethod.value = method
    }

    // ✅ FIX — amount and phone are now REQUIRED explicit parameters, not derived.
    // Previously: phone came from _shippingPhone (always ""), and amount was
    // never passed at all — initiateMpesaPayment instead summed the user's
    // ENTIRE past order history as a "fallback", sending a completely wrong
    // amount to M-Pesa. orderViewModel param removed too — order creation now
    // lives solely in SuccessScreen, so this no longer needs to write to Firebase.
    fun initiatePayment(
        context: Context,
        amount: String,
        phone: String
    ) {
        val method = _selectedPaymentMethod.value
        _isProcessing.value = true
        _paymentSuccess.value = false

        when (method) {
            "M-Pesa" -> initiateMpesaPayment(context, phone, amount)
            "Credit/Debit Card" -> simulateCardPayment(context, amount)
            "PayPal" -> simulatePayPalPayment(context, amount)
            "Bank Transfer" -> simulateBankTransfer(context, amount)
            else -> {
                showToast(context, "Unknown payment method")
                _isProcessing.value = false
            }
        }
    }

    // In initiatePayment() inside PaymentViewModel, change:
    private fun initiateMpesaPayment(context: Context, phoneNumber: String, amount: String) {
        viewModelScope.launch {
            try {
                // ✅ FIX — was amount.toIntOrNull() ?: 0
                // "1500.50".toIntOrNull() returns null → falls back to 0
                // Every payment was being initiated for Ksh 0.
                // toDoubleOrNull().toInt() correctly gives 1500 from "1500.50"
                val amountInt = amount.toDoubleOrNull()?.toInt() ?: 0
                val success = mpesaRepository.initiatePayment(phoneNumber, amountInt)
                if (success) {
                    showToast(context, "✅ M-Pesa payment initiated successfully!")
                    _paymentSuccess.value = true
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

    private fun simulateCardPayment(context: Context, amount: String) {
        viewModelScope.launch {
            showToast(context, "💳 Processing card payment...")
            kotlinx.coroutines.delay(2000)
            showToast(context, "✅ Card payment successful!")
            _paymentSuccess.value = true
            _isProcessing.value = false
        }
    }

    private fun simulatePayPalPayment(context: Context, amount: String) {
        viewModelScope.launch {
            showToast(context, "🌐 Redirecting to PayPal...")
            kotlinx.coroutines.delay(2000)
            showToast(context, "✅ PayPal payment confirmed!")
            _paymentSuccess.value = true
            _isProcessing.value = false
        }
    }

    private fun simulateBankTransfer(context: Context, amount: String) {
        viewModelScope.launch {
            showToast(context, "🏦 Please complete bank transfer to finalize order.")
            kotlinx.coroutines.delay(3000)
            showToast(context, "✅ Bank transfer confirmed!")
            _paymentSuccess.value = true
            _isProcessing.value = false
        }
    }

    // ✅ REMOVED — completeOrder(). It used to (a) derive the wrong total the
    // same way initiateMpesaPayment did, and (b) write a SECOND order record
    // to Firebase right before SuccessScreen wrote its own — every completed
    // purchase was creating two order entries. Order creation now happens
    // exactly once, in SuccessScreen's LaunchedEffect(Unit).

    private fun showToast(context: Context, message: String) {
        viewModelScope.launch(Dispatchers.Main) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }
}