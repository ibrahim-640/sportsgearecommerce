package com.example.sportsgear.data

import android.content.Context
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportsgear.network.MpesaRepository
import kotlinx.coroutines.delay
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

    // ✅ NEW — tracks whether STK push was sent and we are waiting
    // for the user to confirm on their phone
    private val _stkPushSent = mutableStateOf(false)
    val stkPushSent: State<Boolean> = _stkPushSent

    // ✅ NEW — holds any error message to show the user
    private val _paymentError = mutableStateOf<String?>(null)
    val paymentError: State<String?> = _paymentError

    fun selectPaymentMethod(method: String) {
        _selectedPaymentMethod.value = method
    }

    fun clearError() {
        _paymentError.value = null
    }

    fun initiatePayment(
        context: Context,
        amount: String,
        phone: String
    ) {
        val method = _selectedPaymentMethod.value
        _isProcessing.value = true
        _paymentSuccess.value = false
        _paymentError.value = null
        _stkPushSent.value = false

        when (method) {
            "M-Pesa" -> initiateMpesaPayment(phone, amount)
            else -> {
                _paymentError.value = "Unknown payment method"
                _isProcessing.value = false
            }
        }
    }

    private fun initiateMpesaPayment(phoneNumber: String, amount: String) {
        viewModelScope.launch {
            try {
                val amountInt = amount.toDoubleOrNull()?.toInt() ?: 0

                // ✅ Step 1 — Send STK push to user's phone
                val stkSent = mpesaRepository.initiatePayment(phoneNumber, amountInt)

                if (!stkSent) {
                    // STK push failed to send — show error immediately
                    _paymentError.value = "Failed to send M-Pesa request. Check your phone number and try again."
                    _isProcessing.value = false
                    return@launch
                }

                // ✅ Step 2 — STK push sent successfully
                // Now we wait for user to enter PIN on their phone
                // We set stkPushSent = true so UI shows "waiting" state
                _stkPushSent.value = true
                _isProcessing.value = false

                // ✅ Step 3 — In a real implementation Safaricom calls your
                // backend callback URL here. Since we have no backend,
                // we poll Firebase or wait for the user to confirm manually.
                // For now we show a "I have paid" button in the UI and only
                // set paymentSuccess = true when the user taps it.
                // This prevents orders from being created before payment.

                Log.d("PaymentViewModel", "STK push sent to $phoneNumber for Ksh $amount")

            } catch (e: Exception) {
                Log.e("PaymentViewModel", "M-Pesa Error: ${e.message}")
                _paymentError.value = "Payment error: ${e.message}"
                _isProcessing.value = false
                _stkPushSent.value = false
            }
        }
    }

    // ✅ NEW — called when user taps "I have completed payment"
    // This is the manual confirmation step that replaces the
    // automatic (incorrect) success detection
    fun confirmPaymentCompleted() {
        _paymentSuccess.value = true
        _stkPushSent.value = false
    }

    // ✅ NEW — called if user wants to cancel or retry
    fun resetPayment() {
        _paymentSuccess.value = false
        _stkPushSent.value = false
        _isProcessing.value = false
        _paymentError.value = null
    }
}