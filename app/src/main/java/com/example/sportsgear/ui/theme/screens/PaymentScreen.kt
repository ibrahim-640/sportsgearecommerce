package com.example.sportsgear.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.sportsgear.data.PaymentViewModel
import com.example.sportsgear.navigation.ROUTE_SUCCESS
import com.example.sportsgear.ui.theme.Maroon
import com.example.sportsgear.ui.theme.MaroonDark

// ✅ Moved to Color.kt — defined here only as a local fallback
private val MpesaGreen = Color(0xFF34B233)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    navController: NavController,
    amount: String,
    phone: String,
    paymentViewModel: PaymentViewModel = viewModel()
) {
    val isProcessing by paymentViewModel.isProcessing
    val paymentSuccess by paymentViewModel.paymentSuccess
    val stkPushSent by paymentViewModel.stkPushSent
    val paymentError by paymentViewModel.paymentError

    val snackbarHostState = remember { SnackbarHostState() }

    // ✅ Fix — only navigate to SuccessScreen when paymentSuccess is true
    // paymentSuccess is now only set to true when user manually confirms
    // they completed the M-Pesa payment — not when STK push is sent
    LaunchedEffect(paymentSuccess) {
        if (paymentSuccess) {
            navController.navigate("$ROUTE_SUCCESS/$amount/M-Pesa") {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    // ✅ Show payment errors via Snackbar — no more Toast
    LaunchedEffect(paymentError) {
        paymentError?.let {
            snackbarHostState.showSnackbar(it)
            paymentViewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Secure",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("M-Pesa Payment", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Maroon)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            when {
                // ✅ State 1 — Sending STK push
                isProcessing -> {
                    SendingPushState(amount = amount, phone = phone)
                }

                // ✅ State 2 — STK push sent, waiting for user to pay
                // This is the NEW state that prevents premature order creation
                stkPushSent -> {
                    WaitingForPaymentState(
                        amount = amount,
                        phone = phone,
                        onConfirmPayment = {
                            // ✅ User confirms they entered PIN and paid
                            // Only NOW do we set paymentSuccess = true
                            paymentViewModel.confirmPaymentCompleted()
                        },
                        onRetry = {
                            // User says they didn't get the prompt — resend
                            paymentViewModel.resetPayment()
                        }
                    )
                }

                // ✅ State 3 — Payment confirmed, about to navigate
                paymentSuccess -> {
                    PaymentSuccessState()
                }

                // ✅ State 4 — Initial state — show payment form
                else -> {
                    PaymentFormState(
                        amount = amount,
                        phone = phone,
                        onPay = {
                            paymentViewModel.selectPaymentMethod("M-Pesa")
                            paymentViewModel.initiatePayment(
                                context = it,
                                amount = amount,
                                phone = phone
                            )
                        }
                    )
                }
            }
        }
    }
}

// ✅ State 1 — Sending STK push
@Composable
fun SendingPushState(amount: String, phone: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = MpesaGreen,
            modifier = Modifier.size(60.dp),
            strokeWidth = 4.dp
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "Sending M-Pesa Request...",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaroonDark
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text("Amount: Ksh $amount", fontSize = 16.sp, color = Color.Gray)
        Text("Phone: $phone", fontSize = 16.sp, color = Color.Gray)
    }
}

// ✅ State 2 — NEW: Waiting for user to complete payment on their phone
// This is the critical state that was missing before
@Composable
fun WaitingForPaymentState(
    amount: String,
    phone: String,
    onConfirmPayment: () -> Unit,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Phone icon pulsing indicator
        Icon(
            Icons.Default.Phone,
            contentDescription = null,
            tint = MpesaGreen,
            modifier = Modifier.size(72.dp)
        )

        Text(
            text = "Check Your Phone",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaroonDark
        )

        // Instructions card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MpesaGreen.copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "M-Pesa STK Push Sent",
                    fontWeight = FontWeight.Bold,
                    color = MpesaGreen,
                    fontSize = 16.sp
                )
                Text("• Amount: Ksh $amount", color = Color.Gray)
                Text("• To: $phone", color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "An M-Pesa payment request has been sent to your phone. " +
                            "Please enter your M-Pesa PIN to complete the payment.",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ✅ This is the key button — user confirms they have paid
        // Only after tapping this does the order get created
        Button(
            onClick = onConfirmPayment,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MpesaGreen),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "I Have Completed Payment",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Retry button if user didn't receive the prompt
        OutlinedButton(
            onClick = onRetry,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("I Didn't Receive the Prompt — Retry", color = MaroonDark)
        }

        Text(
            text = "Only tap 'I Have Completed Payment' after successfully " +
                    "entering your M-Pesa PIN and receiving confirmation.",
            fontSize = 12.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}

// State 3 — Payment confirmed
@Composable
fun PaymentSuccessState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.CheckCircle,
            contentDescription = null,
            tint = MpesaGreen,
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "Payment Confirmed!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaroonDark
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Creating your order...",
            fontSize = 16.sp,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(8.dp))
        CircularProgressIndicator(color = Maroon, modifier = Modifier.size(24.dp))
    }
}

// State 4 — Initial payment form
@Composable
fun PaymentFormState(
    amount: String,
    phone: String,
    onPay: (android.content.Context) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Payment summary
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Payment Summary",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaroonDark
                )
                Spacer(modifier = Modifier.height(12.dp))
                PaymentSummaryRow("Total Amount", "Ksh $amount")
                PaymentSummaryRow("Payment Method", "M-Pesa")
                PaymentSummaryRow("Phone Number", phone.ifBlank { "Not provided" })
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text(
                    "You will receive an STK Push on the number above",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
        }

        // Instructions
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MpesaGreen.copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "How to Complete Payment",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MpesaGreen
                )
                Spacer(modifier = Modifier.height(8.dp))
                PaymentStep("1", "Tap the Pay button below")
                PaymentStep("2", "Check your phone for M-Pesa prompt")
                PaymentStep("3", "Enter your M-Pesa PIN")
                PaymentStep("4", "Tap 'I Have Completed Payment'")
                PaymentStep("5", "Your order will be created")
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { onPay(context) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MpesaGreen),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                "Pay Ksh $amount via M-Pesa",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Text(
            "🔒 Secure M-Pesa Transaction",
            color = Color.Gray,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun PaymentSummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 14.sp, color = Color.Gray)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaroonDark)
    }
}

@Composable
fun PaymentStep(number: String, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = number,
            fontWeight = FontWeight.Bold,
            color = MpesaGreen,
            modifier = Modifier.width(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, fontSize = 14.sp, color = Color.Gray, modifier = Modifier.weight(1f))
    }
}