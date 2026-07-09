package com.example.sportsgear.ui.screens
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.sportsgear.data.PaymentViewModel
import com.example.sportsgear.navigation.ROUTE_SUCCESS
import com.example.sportsgear.ui.theme.Maroon

val MpesaGreen = Color(0xFF34B233)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    navController: NavHostController,
    amount: String,
    phone: String,
    paymentViewModel: PaymentViewModel = viewModel()
    // ✅ FIX — orderViewModel parameter removed. It's no longer needed here;
    // SuccessScreen (wired separately in AppNavHost) owns order creation now.
) {
    val context = LocalContext.current

    val isProcessing by paymentViewModel.isProcessing
    val paymentSuccess by paymentViewModel.paymentSuccess

    LaunchedEffect(paymentSuccess) {
        if (paymentSuccess) {
            Toast.makeText(context, "Payment Successful!", Toast.LENGTH_SHORT).show()
            // ✅ FIX — was "$ROUTE_SUCCESS/$amount", missing the required {method}
            // segment AppNavHost's route declares. This crashed immediately
            // after every successful payment.
            navController.navigate("$ROUTE_SUCCESS/$amount/M-Pesa") {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Secure",
                            tint = Color.Green,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("M-Pesa Payment", color = Color.White)
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
            if (isProcessing) {
                ProcessingPaymentState(amount = amount, phone = phone)
            } else if (paymentSuccess) {
                PaymentSuccessState()
            } else {
                PaymentFormState(
                    amount = amount,
                    phone = phone,
                    paymentViewModel = paymentViewModel,
                    context = context
                )
            }
        }
    }
}

@Composable
fun ProcessingPaymentState(amount: String, phone: String) {
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
            "Processing M-Pesa Payment",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Maroon
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Amount: Ksh $amount",
            fontSize = 16.sp,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Phone: $phone",
            fontSize = 16.sp,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Check your phone for STK push prompt",
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

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
            Icons.Default.Lock,
            contentDescription = "Payment Successful",
            tint = Color.Green,
            modifier = Modifier.size(60.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "Payment Successful!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Maroon
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Redirecting to order confirmation...",
            fontSize = 16.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun PaymentFormState(
    amount: String,
    phone: String,
    paymentViewModel: PaymentViewModel,
    context: android.content.Context
    // ✅ FIX — orderViewModel param removed (unused now)
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        PaymentSummaryCard(amount = amount, phone = phone)
        MpesaInstructionsCard()
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = {
                paymentViewModel.selectPaymentMethod("M-Pesa")
                // ✅ FIX — amount and phone now passed explicitly, fixing both
                // the wrong-total bug and the always-empty-phone bug.
                paymentViewModel.initiatePayment(context, amount, phone)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MpesaGreen),
            shape = RoundedCornerShape(12.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Text(
                "Pay Ksh $amount via M-Pesa",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Text(
            text = "🔒 Secure M-Pesa Transaction • Your payment is protected",
            color = Color.Gray,
            fontSize = 12.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun PaymentSummaryCard(amount: String, phone: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Payment Summary",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Maroon
            )
            Spacer(modifier = Modifier.height(16.dp))
            SummaryRow("Total Amount", "Ksh $amount")
            SummaryRow("Payment Method", "M-Pesa")
            SummaryRow("Phone Number", phone)
            Divider(
                modifier = Modifier.padding(vertical = 12.dp),
                thickness = 1.dp,
                color = Maroon.copy(alpha = 0.2f)
            )
            Text(
                "You will receive an STK Push on the phone number above",
                fontSize = 14.sp,
                color = Color.Gray,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun MpesaInstructionsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MpesaGreen.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "How to Complete Payment",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MpesaGreen
            )
            Spacer(modifier = Modifier.height(12.dp))
            InstructionStep(number = "1", text = "Tap the 'Pay via M-Pesa' button below")
            InstructionStep(number = "2", text = "Check your phone for STK push prompt")
            InstructionStep(number = "3", text = "Enter your M-Pesa PIN when prompted")
            InstructionStep(number = "4", text = "Wait for payment confirmation")
        }
    }
}

@Composable
fun InstructionStep(number: String, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = number,
            fontWeight = FontWeight.Bold,
            color = MpesaGreen,
            modifier = Modifier.width(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 14.sp,
            color = Color.Gray,
            lineHeight = 18.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            color = Color.Black
        )
        Text(
            value,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = Maroon
        )
    }
}