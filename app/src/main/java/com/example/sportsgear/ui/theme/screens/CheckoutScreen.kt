package com.example.sportsgear.ui.theme.screens
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Money
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.sportsgear.data.CartViewModel
import com.example.sportsgear.data.OrderViewModel
import com.example.sportsgear.navigation.ROUTE_PAYMENT
import com.example.sportsgear.ui.theme.Maroon
import kotlinx.coroutines.launch

val MpesaGreen = Color(0xFF34B233)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    userId: String,
    cartViewModel: CartViewModel,
    navController: NavHostController,
    orderViewModel: OrderViewModel
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() } // ✅ FIX — replaces Toast

    val cartItems by cartViewModel.cartItems.collectAsState()

    // ✅ FIX — now reads the SAME totals CartScreen already shows the user,
    // instead of recalculating independently with a different tax rate (16%
    // vs CartViewModel's 5%) and a different shipping formula (city-based vs
    // flat-rate + free-shipping threshold). One number, everywhere — Cart,
    // Checkout, and the amount actually sent to M-Pesa, all match.
    val subtotal by cartViewModel.subtotal
    val tax by cartViewModel.tax
    val shipping by cartViewModel.shipping
    val total by cartViewModel.total

    // Form States
    var fullName by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Checkout", color = Color.White) },
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
            // Shipping Information
            Text("Shipping Information", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Address") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = city,
                onValueChange = { city = it },
                label = { Text("City / Town") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone Number") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(20.dp))

            // Payment Method - M-Pesa Only
            Text("Payment Method", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Spacer(Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MpesaGreen.copy(alpha = 0.1f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Money,
                        contentDescription = "M-Pesa",
                        tint = MpesaGreen
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "M-Pesa",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(Modifier.weight(1f))
                    RadioButton(
                        selected = true,
                        onClick = { /* M-Pesa is the only option */ }
                    )
                }
            }

            Text(
                text = "You will receive an M-Pesa prompt on your phone to complete the payment",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Spacer(Modifier.height(24.dp))

            // Order Summary — now sourced from CartViewModel
            OrderSummarySection(
                subtotal = subtotal,
                shipping = shipping,
                tax = tax,
                total = total,
                itemCount = cartItems.sumOf { it.quantity },
                city = city
            )

            Spacer(Modifier.height(24.dp))

            // Proceed to Payment Button
            Button(
                onClick = {
                    if (fullName.isBlank() || address.isBlank() || city.isBlank() || phone.isBlank()) {
                        // ✅ FIX — Toast replaced with Snackbar, matching every other screen
                        scope.launch {
                            snackbarHostState.showSnackbar("Please fill in all required fields")
                        }
                    } else {
                        val amountValue = "%.2f".format(total) // ✅ now the real cart total
                        val phoneValue = phone.trim()
                        navController.navigate("$ROUTE_PAYMENT/$amountValue/$phoneValue")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MpesaGreen),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text("Continue to M-Pesa Payment", color = Color.White, fontSize = 16.sp)
            }

            Text(
                text = "You'll complete the M-Pesa payment in the next step",
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun OrderSummarySection(
    subtotal: Double,
    shipping: Double,
    tax: Double,
    total: Double,
    itemCount: Int,
    city: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Order Summary", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(12.dp))

            SummaryRow("Items", "$itemCount item${if (itemCount != 1) "s" else ""}")
            SummaryRow("Subtotal", "Ksh ${"%.2f".format(subtotal)}")

            val shippingText = if (city.isNotBlank()) "Shipping to $city" else "Shipping"
            SummaryRow(shippingText, "Ksh ${"%.2f".format(shipping)}")

            // ✅ FIX — hardcoded to match CartViewModel.TAX_RATE (5%). The old
            // (tax / subtotal) * 100 calculation would show "Tax (NaN%)" any
            // time subtotal was 0 (e.g. briefly while the cart is still loading).
            SummaryRow("Tax (5%)", "Ksh ${"%.2f".format(tax)}")
            Divider(Modifier.padding(vertical = 8.dp))
            SummaryRow("Total", "Ksh ${"%.2f".format(total)}", bold = true)
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String, bold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            fontSize = if (bold) 16.sp else 14.sp
        )
        Text(
            value,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            fontSize = if (bold) 16.sp else 14.sp,
            color = if (bold) Maroon else Color.Black
        )
    }
}