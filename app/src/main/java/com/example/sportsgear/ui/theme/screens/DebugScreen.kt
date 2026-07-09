package com.example.sportsgear.ui.theme.screens//package com.yourapp.ui.screens.debug
//
//import android.util.Log
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.unit.dp
//import androidx.lifecycle.viewmodel.compose.viewModel
//import com.google.firebase.ktx.Firebase
//import android.widget.Toast
//import com.example.sportsgear.data.CartViewModel
//import com.google.firebase.auth.ktx.auth
//import com.example.sportsgear.models.Product
//import com.google.firebase.database.ktx.database
//import java.text.SimpleDateFormat
//import java.util.*
//
//@Composable
//fun DebugScreen(
//    onBack: () -> Unit = {}
//) {
//    val context = LocalContext.current
//    val cartViewModel: CartViewModel = viewModel()
//    var debugLog by remember { mutableStateOf("=== DEBUG SCREEN READY ===\n") }
//    var isLoading by remember { mutableStateOf(false) }
//
//    val addToLog: (String) -> Unit = { message ->
//        debugLog += "$message\n"
//        Log.d("FirebaseDebug", message)
//    }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp)
//            .verticalScroll(rememberScrollState())
//    ) {
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.SpaceBetween
//        ) {
//            Text("Debug Screen", style = MaterialTheme.typography.headlineSmall)
//            Button(onClick = onBack) { Text("Back") }
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        Card(modifier = Modifier.fillMaxWidth().height(300.dp)) {
//            Text(
//                text = debugLog,
//                modifier = Modifier.padding(8.dp).fillMaxSize(),
//                style = MaterialTheme.typography.bodySmall
//            )
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        Column(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.spacedBy(8.dp)
//        ) {
//            // Firebase Debug Buttons
//            DebugButton(text = "1. 🔧 Fix Firebase Rules & Data", onClick = {
//                addToLog("\n--- FIXING FIREBASE RULES & DATA ---")
//                fixFirebaseRulesAndData(addToLog, context)
//            })
//
//            DebugButton(text = "2. 🛠️ Complete Admin Setup", onClick = {
//                addToLog("\n--- COMPLETE ADMIN SETUP ---")
//                completeAdminSetup(addToLog, context)
//            })
//
//            DebugButton(text = "3. 🔍 Test All Permissions", onClick = {
//                addToLog("\n--- TESTING ALL PERMISSIONS ---")
//                testAllPermissions(addToLog, context, cartViewModel)
//            })
//
//            // M-Pesa Debug Buttons
//            Text("M-Pesa Payment Debug", style = MaterialTheme.typography.titleMedium)
//
//            DebugButton(text = "4. 🚀 Test ALL M-Pesa Credentials", onClick = {
//                isLoading = true
//                addToLog("\n--- COMPREHENSIVE M-PESA TEST ---")
//                testAllMpesaCredentials(addToLog, context) {
//                    isLoading = false
//                }
//            })
//
//            DebugButton(text = "5. 💳 Quick Payment Test", onClick = {
//                addToLog("\n--- QUICK PAYMENT TEST ---")
//                testMpesaPayment(addToLog, context)
//            })
//
//            DebugButton(text = "6. 🐛 Find Payment Failure Reason", onClick = {
//                addToLog("\n--- PAYMENT FAILURE ANALYSIS ---")
//                findPaymentFailureReason(addToLog, context)
//            })
//
//            // Show loading indicator
//            if (isLoading) {
//                CircularProgressIndicator(modifier = Modifier.size(20.dp))
//                Text("Testing M-Pesa Credentials...")
//            }
//
//            Button(
//                onClick = { debugLog = "=== DEBUG CLEARED ===\n" },
//                modifier = Modifier.fillMaxWidth(),
//                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer)
//            ) {
//                Text("Clear Log")
//            }
//        }
//    }
//}
//
//@Composable
//fun DebugButton(text: String, onClick: () -> Unit) {
//    Button(
//        onClick = onClick,
//        modifier = Modifier.fillMaxWidth()
//    ) {
//        Text(text)
//    }
//}
//
//// Firebase Debug Functions
//private fun fixFirebaseRulesAndData(
//    addToLog: (String) -> Unit,
//    context: android.content.Context
//) {
//    val user = Firebase.auth.currentUser
//    if (user == null) {
//        addToLog("❌ Please login first")
//        showToast(context, "Please login first")
//        return
//    }
//
//    addToLog("🎯 FIXING FIREBASE PERMISSION ISSUES")
//    addToLog("User: ${user.email}")
//    addToLog("UID: ${user.uid}")
//    addToLog("")
//
//    addToLog("📋 REQUIRED FIREBASE RULES:")
//    addToLog("Go to: Firebase Console → Realtime Database → Rules")
//    addToLog("Replace with:")
//    addToLog("```json")
//    addToLog("{")
//    addToLog("  \"rules\": {")
//    addToLog("    \"Admins\": {")
//    addToLog("      \"\$adminId\": {")
//    addToLog("        \".read\": \"auth != null\",")
//    addToLog("        \".write\": \"auth != null && auth.uid === \$adminId\"")
//    addToLog("      }")
//    addToLog("    },")
//    addToLog("    \"Products\": {")
//    addToLog("      \".read\": \"auth != null\",")
//    addToLog("      \".write\": \"auth != null\"")
//    addToLog("    },")
//    addToLog("    \"Users\": {")
//    addToLog("      \"\$userId\": {")
//    addToLog("        \".read\": \"auth != null && auth.uid === \$userId\",")
//    addToLog("        \".write\": \"auth != null && auth.uid === \$userId\"")
//    addToLog("      }")
//    addToLog("    },")
//    addToLog("    \"Cart\": {")
//    addToLog("      \"\$userId\": {")
//    addToLog("        \".read\": \"auth != null && auth.uid === \$userId\",")
//    addToLog("        \".write\": \"auth != null && auth.uid === \$userId\"")
//    addToLog("      }")
//    addToLog("    },")
//    addToLog("    \"Orders\": {")
//    addToLog("      \"\$userId\": {")
//    addToLog("        \".read\": \"auth != null && auth.uid === \$userId\",")
//    addToLog("        \".write\": \"auth != null && auth.uid === \$userId\"")
//    addToLog("      }")
//    addToLog("    }")
//    addToLog("  }")
//    addToLog("}")
//    addToLog("```")
//    addToLog("")
//    addToLog("🔧 These rules will fix:")
//    addToLog("   ✅ 'Permission denied' errors")
//    addToLog("   ✅ Admin icon visibility")
//    addToLog("   ✅ Cart functionality")
//    addToLog("   ✅ User profile loading")
//    addToLog("")
//
//    showToast(context, "Update Firebase rules - check logs for instructions")
//}
//
//private fun completeAdminSetup(
//    addToLog: (String) -> Unit,
//    context: android.content.Context
//) {
//    val user = Firebase.auth.currentUser
//    if (user == null) {
//        addToLog("❌ Please login as admin first")
//        return
//    }
//
//    addToLog("👑 COMPLETE ADMIN SETUP")
//    addToLog("Setting up: ${user.email}")
//    addToLog("")
//
//    val database = Firebase.database
//
//    // Step 1: Setup Admins collection
//    addToLog("1. Setting up /Admins...")
//    val adminRef = database.getReference("Admins").child(user.uid)
//    adminRef.setValue(mapOf(
//        "email" to user.email,
//        "isAdmin" to true,
//        "role" to "admin",
//        "setupAt" to System.currentTimeMillis()
//    )).addOnSuccessListener {
//        addToLog("   ✅ /Admins setup complete")
//
//        // Step 2: Setup Users collection
//        addToLog("2. Setting up /Users...")
//        val userRef = database.getReference("Users").child(user.uid)
//        userRef.setValue(mapOf(
//            "email" to user.email,
//            "isAdmin" to true,
//            "role" to "admin",
//            "firstname" to "Admin",
//            "lastname" to "User",
//            "setupAt" to System.currentTimeMillis()
//        )).addOnSuccessListener {
//            addToLog("   ✅ /Users setup complete")
//
//            // Step 3: Test permissions
//            addToLog("3. Testing permissions...")
//            testUserPermissions(user.uid, addToLog, context)
//        }.addOnFailureListener { e ->
//            addToLog("   ❌ /Users setup failed: ${e.message}")
//        }
//    }.addOnFailureListener { e ->
//        addToLog("   ❌ /Admins setup failed: ${e.message}")
//    }
//}
//
//private fun testUserPermissions(
//    userId: String,
//    addToLog: (String) -> Unit,
//    context: android.content.Context
//) {
//    val database = Firebase.database
//
//    // Test Users read
//    addToLog("   Testing /Users read...")
//    database.getReference("Users").child(userId).get()
//        .addOnSuccessListener {
//            addToLog("      ✅ /Users read: SUCCESS")
//
//            // Test Cart access
//            addToLog("   Testing /Cart access...")
//            database.getReference("Cart").child(userId).child("test").setValue("test")
//                .addOnSuccessListener {
//                    addToLog("      ✅ /Cart write: SUCCESS")
//
//                    // Test Products read
//                    addToLog("   Testing /Products read...")
//                    database.getReference("Products").limitToFirst(1).get()
//                        .addOnSuccessListener {
//                            addToLog("      ✅ /Products read: SUCCESS")
//
//                            addToLog("")
//                            addToLog("🎉 ADMIN SETUP COMPLETE!")
//                            addToLog("✅ All permissions working")
//                            addToLog("✅ Admin data created")
//                            addToLog("")
//                            addToLog("📱 NEXT STEPS:")
//                            addToLog("   1. Update Firebase rules (Step 1)")
//                            addToLog("   2. Force stop and restart app")
//                            addToLog("   3. Login as admin")
//                            addToLog("   4. Admin icon should appear")
//
//                            showToast(context, "Admin setup complete! Update rules & restart app.")
//                        }.addOnFailureListener { e ->
//                            addToLog("      ❌ /Products read failed: ${e.message}")
//                        }
//                }.addOnFailureListener { e ->
//                    addToLog("      ❌ /Cart write failed: ${e.message}")
//                }
//        }.addOnFailureListener { e ->
//            addToLog("      ❌ /Users read failed: ${e.message}")
//        }
//}
//
//private fun testAllPermissions(
//    addToLog: (String) -> Unit,
//    context: android.content.Context,
//    cartViewModel: CartViewModel
//) {
//    val user = Firebase.auth.currentUser
//    if (user == null) {
//        addToLog("❌ Please login first")
//        return
//    }
//
//    addToLog("🧪 TESTING ALL PERMISSIONS")
//    addToLog("User: ${user.email}")
//    addToLog("")
//
//    val database = Firebase.database
//    val userId = user.uid
//
//    // Test 1: Users collection
//    addToLog("1. Testing /Users access...")
//    database.getReference("Users").child(userId).get()
//        .addOnSuccessListener { snapshot ->
//            if (snapshot.exists()) {
//                addToLog("   ✅ /Users read: SUCCESS")
//                val isAdmin = snapshot.child("isAdmin").getValue(Boolean::class.java) ?: false
//                val role = snapshot.child("role").getValue(String::class.java) ?: "user"
//                addToLog("   Admin status: isAdmin=$isAdmin, role=$role")
//            } else {
//                addToLog("   ❌ /Users read: NO DATA (run Admin Setup)")
//            }
//        }.addOnFailureListener { e ->
//            addToLog("   ❌ /Users read: FAILED - ${e.message}")
//        }
//
//    // Test 2: Cart collection
//    addToLog("2. Testing /Cart access...")
//    database.getReference("Cart").child(userId).child("test").setValue(System.currentTimeMillis())
//        .addOnSuccessListener {
//            addToLog("   ✅ /Cart write: SUCCESS")
//            database.getReference("Cart").child(userId).child("test").removeValue()
//        }.addOnFailureListener { e ->
//            addToLog("   ❌ /Cart write: FAILED - ${e.message}")
//        }
//
//    // Test 3: Products collection
//    addToLog("3. Testing /Products access...")
//    database.getReference("Products").limitToFirst(1).get()
//        .addOnSuccessListener { snapshot ->
//            if (snapshot.exists()) {
//                addToLog("   ✅ /Products read: SUCCESS")
//            } else {
//                addToLog("   ℹ️ /Products read: SUCCESS (no products yet)")
//            }
//        }.addOnFailureListener { e ->
//            addToLog("   ❌ /Products read: FAILED - ${e.message}")
//        }
//
//    // Test 4: CartViewModel
//    addToLog("4. Testing CartViewModel...")
//    val testProduct = Product(
//        productId = "test_${System.currentTimeMillis()}",
//        name = "Test Product",
//        price = "10.00",
//        imageUrl = "",
//        quantity = "1",
//        category = "Test",
//        value = "10.00",
//        description = "Test",
//        isOnOffer = false
//    )
//
//    cartViewModel.addToCart(userId, testProduct)
//    addToLog("   📤 CartViewModel.addToCart() called")
//    addToLog("   Check Logcat for 'DebugCart' results")
//
//    addToLog("")
//    addToLog("📊 PERMISSION SUMMARY:")
//    addToLog("If any tests failed, update Firebase rules (Step 1)")
//
//    showToast(context, "Permission test complete - check logs")
//}
//
//// M-Pesa Debug Functions - COMPREHENSIVE TEST
//private fun testAllMpesaCredentials(
//    addToLog: (String) -> Unit,
//    context: android.content.Context,
//    onComplete: () -> Unit
//) {
//    addToLog("🚀 COMPREHENSIVE M-PESA CREDENTIALS TEST")
//    addToLog("")
//
//    // M-Pesa Credentials - UPDATE THESE WITH YOUR ACTUAL VALUES
//    val credentials = MpesaCredentials(
//        consumerKey = "OC7HldYBQ1TKAUrER2XyQ8GftnkHogXq1nWA67US7I2jKG8r",
//        consumerSecret = "zvZXkfOx0dTTl0cBmjtbtVuo5mSGs6dAvnRyfuq5DZzYMJzunmy9YhOoP9WpapHu",
//        businessShortCode = "174379",
//        passkey = "bfb279f9aa9bdbcf158e97dd71a467cd2e0c893059b10f78e6b72ada1ed2c919"
//    )
//
//    Thread {
//        try {
//            // STEP 1: Validate Credentials Format
//            addToLog("🔍 STEP 1: CREDENTIALS VALIDATION")
//            validateCredentialsFormat(credentials, addToLog)
//
//            // STEP 2: Test Authentication
//            addToLog("")
//            addToLog("🔍 STEP 2: AUTHENTICATION TEST")
//            val authResult = testAuthentication(credentials, addToLog)
//
//            if (!authResult.success) {
//                addToLog("❌ AUTHENTICATION FAILED - Stopping test")
//                android.os.Handler(context.mainLooper).post {
//                    showToast(context, "Authentication failed - check credentials")
//                    onComplete()
//                }
//                return@Thread
//            }
//
//            // STEP 3: Test Password Generation
//            addToLog("")
//            addToLog("🔍 STEP 3: PASSWORD GENERATION TEST")
//            val passwordResult = testPasswordGeneration(credentials, addToLog)
//
//            // STEP 4: Test STK Push (Full Payment Test)
//            addToLog("")
//            addToLog("🔍 STEP 4: STK PUSH TEST")
//            if (authResult.accessToken != null && passwordResult.success) {
//                testStkPushComprehensive(authResult.accessToken, credentials, addToLog, context)
//            } else {
//                addToLog("❌ Skipping STK Push - Previous steps failed")
//            }
//
//            // STEP 5: Generate Summary
//            addToLog("")
//            addToLog("📊 TEST SUMMARY")
//            generateTestSummary(authResult.success, passwordResult.success, addToLog)
//
//        } catch (e: Exception) {
//            addToLog("❌ COMPREHENSIVE TEST FAILED: ${e.message}")
//        } finally {
//            android.os.Handler(context.mainLooper).post {
//                onComplete()
//            }
//        }
//    }.start()
//}
//
//data class MpesaCredentials(
//    val consumerKey: String,
//    val consumerSecret: String,
//    val businessShortCode: String,
//    val passkey: String
//)
//
//data class TestResult(
//    val success: Boolean,
//    val accessToken: String? = null,
//    val error: String? = null
//)
//
//private fun validateCredentialsFormat(credentials: MpesaCredentials, addToLog: (String) -> Unit) {
//    addToLog("📋 CREDENTIALS CONFIGURATION:")
//    addToLog("Consumer Key: ${if (credentials.consumerKey.startsWith("YOUR") || credentials.consumerKey.length < 10) "❌ INVALID" else "✅ VALID"} (${credentials.consumerKey.take(8)}...)")
//    addToLog("Consumer Secret: ${if (credentials.consumerSecret.startsWith("YOUR") || credentials.consumerSecret.length < 10) "❌ INVALID" else "✅ VALID"} (${credentials.consumerSecret.take(8)}...)")
//    addToLog("Business Shortcode: ${if (credentials.businessShortCode == "174379") "✅ VALID (Sandbox)" else "❌ INVALID - Should be 174379 for sandbox"}")
//    addToLog("Passkey: ${if (credentials.passkey.startsWith("YOUR") || credentials.passkey.length < 10) "❌ INVALID" else "✅ VALID"} (${credentials.passkey.take(10)}...${credentials.passkey.takeLast(10)})")
//
//    val allValid = !credentials.consumerKey.startsWith("YOUR") &&
//            !credentials.consumerSecret.startsWith("YOUR") &&
//            credentials.businessShortCode == "174379" &&
//            !credentials.passkey.startsWith("YOUR")
//
//    if (allValid) {
//        addToLog("✅ All credentials are properly formatted")
//    } else {
//        addToLog("❌ Some credentials are invalid or use placeholder values")
//    }
//}
//
//private fun testAuthentication(credentials: MpesaCredentials, addToLog: (String) -> Unit): TestResult {
//    return try {
//        addToLog("🔐 Testing authentication with M-Pesa API...")
//
//        val authString = "Basic ${android.util.Base64.encodeToString("${credentials.consumerKey}:${credentials.consumerSecret}".toByteArray(), android.util.Base64.NO_WRAP)}"
//
//        val url = java.net.URL("https://sandbox.safaricom.co.ke/oauth/v1/generate?grant_type=client_credentials")
//        val connection = url.openConnection() as java.net.HttpURLConnection
//        connection.requestMethod = "GET"
//        connection.setRequestProperty("Authorization", authString)
//        connection.connectTimeout = 30000
//        connection.readTimeout = 30000
//
//        val responseCode = connection.responseCode
//        val response = if (responseCode == 200) {
//            connection.inputStream.bufferedReader().use { it.readText() }
//        } else {
//            connection.errorStream.bufferedReader().use { it.readText() }
//        }
//
//        addToLog("📡 Response Code: $responseCode")
//
//        if (responseCode == 200 && response.contains("access_token")) {
//            val accessToken = """access_token":"([^"]+)""".toRegex().find(response)?.groupValues?.get(1) ?: ""
//            addToLog("✅ AUTHENTICATION SUCCESS")
//            addToLog("✅ Access Token: ${accessToken.take(15)}...")
//            TestResult(success = true, accessToken = accessToken)
//        } else {
//            addToLog("❌ AUTHENTICATION FAILED")
//            addToLog("❌ Response: $response")
//            TestResult(success = false, error = "HTTP $responseCode: $response")
//        }
//    } catch (e: Exception) {
//        addToLog("❌ AUTHENTICATION ERROR: ${e.message}")
//        TestResult(success = false, error = e.message)
//    }
//}
//
//private fun testPasswordGeneration(credentials: MpesaCredentials, addToLog: (String) -> Unit): TestResult {
//    return try {
//        addToLog("🔑 Testing password generation...")
//
//        val timestamp = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())
//        val passwordString = "${credentials.businessShortCode}${credentials.passkey}$timestamp"
//        val password = android.util.Base64.encodeToString(passwordString.toByteArray(), android.util.Base64.NO_WRAP)
//
//        addToLog("Timestamp: $timestamp")
//        addToLog("Password String: ${credentials.businessShortCode} + [passkey] + $timestamp")
//        addToLog("Generated Password: ${password.take(20)}... (length: ${password.length})")
//
//        if (password.length > 10) {
//            addToLog("✅ PASSWORD GENERATION SUCCESS")
//            TestResult(success = true)
//        } else {
//            addToLog("❌ PASSWORD GENERATION FAILED")
//            TestResult(success = false, error = "Password too short")
//        }
//    } catch (e: Exception) {
//        addToLog("❌ PASSWORD GENERATION ERROR: ${e.message}")
//        TestResult(success = false, error = e.message)
//    }
//}
//
//private fun testStkPushComprehensive(
//    accessToken: String,
//    credentials: MpesaCredentials,
//    addToLog: (String) -> Unit,
//    context: android.content.Context
//) {
//    try {
//        addToLog("💳 Testing STK Push with real payment...")
//
//        val testPhone = "254757894179" // Sandbox test number
//        val testAmount = 1
//
//        val timestamp = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())
//        val password = android.util.Base64.encodeToString("${credentials.businessShortCode}${credentials.passkey}$timestamp".toByteArray(), android.util.Base64.NO_WRAP)
//
//        val requestBody = """
//            {
//                "BusinessShortCode": "${credentials.businessShortCode}",
//                "Password": "$password",
//                "Timestamp": "$timestamp",
//                "TransactionType": "CustomerPayBillOnline",
//                "Amount": $testAmount,
//                "PartyA": "$testPhone",
//                "PartyB": "${credentials.businessShortCode}",
//                "PhoneNumber": "$testPhone",
//                "CallBackURL": "https://8c8d-102-214-157-197.ngrok-free.app/callback",
//                "AccountReference": "CompTest123",
//                "TransactionDesc": "Comprehensive Test"
//            }
//        """.trimIndent()
//
//        val stkUrl = java.net.URL("https://sandbox.safaricom.co.ke/mpesa/stkpush/v1/processrequest")
//        val stkConnection = stkUrl.openConnection() as java.net.HttpURLConnection
//        stkConnection.requestMethod = "POST"
//        stkConnection.setRequestProperty("Authorization", "Bearer $accessToken")
//        stkConnection.setRequestProperty("Content-Type", "application/json")
//        stkConnection.doOutput = true
//
//        stkConnection.outputStream.bufferedWriter().use { it.write(requestBody) }
//
//        val stkResponseCode = stkConnection.responseCode
//        val stkResponse = if (stkResponseCode == 200) {
//            stkConnection.inputStream.bufferedReader().use { it.readText() }
//        } else {
//            stkConnection.errorStream.bufferedReader().use { it.readText() }
//        }
//
//        addToLog("📡 STK Response Code: $stkResponseCode")
//        addToLog("📦 STK Response: $stkResponse")
//
//        if (stkResponseCode == 200 && stkResponse.contains(""""ResponseCode":"0"""")) {
//            addToLog("✅ STK PUSH SUCCESS!")
//            addToLog("✅ Payment initiated successfully")
//            addToLog("✅ Check phone $testPhone for M-Pesa prompt")
//            android.os.Handler(context.mainLooper).post {
//                showToast(context, "STK Push successful! Check your phone.")
//            }
//        } else {
//            addToLog("❌ STK PUSH FAILED")
//            analyzeStkPushError(stkResponse, addToLog)
//            android.os.Handler(context.mainLooper).post {
//                showToast(context, "STK Push failed - check logs")
//            }
//        }
//    } catch (e: Exception) {
//        addToLog("❌ STK PUSH ERROR: ${e.message}")
//    }
//}
//
//private fun analyzeStkPushError(stkResponse: String, addToLog: (String) -> Unit) {
//    when {
//        stkResponse.contains("Invalid Access Token") -> {
//            addToLog("🔍 ROOT CAUSE: Invalid Access Token")
//            addToLog("📍 Likely Issue: Wrong Business Shortcode or Passkey")
//        }
//        stkResponse.contains("404.001.03") -> {
//            addToLog("🔍 ROOT CAUSE: Invalid Access Token (404.001.03)")
//            addToLog("📍 Likely Issue: Credentials mismatch between auth and STK")
//        }
//        stkResponse.contains("Request cancelled by user") -> {
//            addToLog("🔍 ROOT CAUSE: User cancelled the request")
//            addToLog("📍 Action: Try again and enter PIN when prompted")
//        }
//        stkResponse.contains("insufficient funds") -> {
//            addToLog("🔍 ROOT CAUSE: Insufficient M-Pesa balance")
//            addToLog("📍 Action: Add funds to test phone number")
//        }
//        else -> {
//            addToLog("🔍 ROOT CAUSE: Unknown error")
//            addToLog("📍 Check M-Pesa documentation for error codes")
//        }
//    }
//}
//
//private fun generateTestSummary(
//    authSuccess: Boolean,
//    passwordSuccess: Boolean,
//    addToLog: (String) -> Unit
//) {
//    addToLog("🎯 TEST RESULTS SUMMARY:")
//    addToLog("Authentication: ${if (authSuccess) "✅ SUCCESS" else "❌ FAILED"}")
//    addToLog("Password Generation: ${if (passwordSuccess) "✅ SUCCESS" else "❌ FAILED"}")
//
//    when {
//        authSuccess && passwordSuccess -> {
//            addToLog("")
//            addToLog("🎉 ALL CREDENTIALS ARE WORKING!")
//            addToLog("✅ Your M-Pesa integration is properly configured")
//            addToLog("✅ You should be able to process payments")
//        }
//        authSuccess && !passwordSuccess -> {
//            addToLog("")
//            addToLog("⚠️ PARTIAL SUCCESS")
//            addToLog("✅ Authentication works")
//            addToLog("❌ Password generation failed")
//            addToLog("📍 Likely Issue: Wrong Passkey or Business Shortcode")
//        }
//        !authSuccess && passwordSuccess -> {
//            addToLog("")
//            addToLog("⚠️ PARTIAL SUCCESS")
//            addToLog("❌ Authentication failed")
//            addToLog("✅ Password generation works")
//            addToLog("📍 Likely Issue: Wrong Consumer Key/Secret")
//        }
//        else -> {
//            addToLog("")
//            addToLog("❌ ALL TESTS FAILED")
//            addToLog("📍 Check all credentials in build.gradle")
//            addToLog("📍 Verify you're using sandbox credentials")
//        }
//    }
//}
//
//// Legacy functions for backward compatibility
//private fun testMpesaPayment(
//    addToLog: (String) -> Unit,
//    context: android.content.Context
//) {
//    addToLog("💳 QUICK PAYMENT TEST")
//    addToLog("Note: Use 'Test ALL M-Pesa Credentials' for comprehensive testing")
//
//    // Use the same credentials as the comprehensive test
//    val credentials = MpesaCredentials(
//        consumerKey = "OC7HldYBQ1TKAUrER2XyQ8GftnkHogXq1nWA67US7I2jKG8r",
//        consumerSecret = "zvZXkfOx0dTTl0cBmjtbtVuo5mSGs6dAvnRyfuq5DZzYMJzunmy9YhOoP9WpapHu",
//        businessShortCode = "174379",
//        passkey = "bfb279f9aa9bdbcf158e97dd71a467cd2e0c893059b10f78e6b72ada1ed2c919"
//    )
//
//    Thread {
//        val authResult = testAuthentication(credentials, addToLog)
//        if (authResult.success && authResult.accessToken != null) {
//            testStkPushComprehensive(authResult.accessToken, credentials, addToLog, context)
//        }
//    }.start()
//}
//
//private fun findPaymentFailureReason(
//    addToLog: (String) -> Unit,
//    context: android.content.Context
//) {
//    addToLog("🐛 PAYMENT FAILURE ANALYSIS")
//    addToLog("")
//
//    addToLog("🔍 COMMON FAILURE PATTERNS:")
//    addToLog("1. ❌ 'Invalid Access Token' - Wrong Passkey/Business Shortcode")
//    addToLog("2. ❌ Authentication fails - Wrong Consumer Key/Secret")
//    addToLog("3. ❌ STK Push fails - Network issues or server errors")
//    addToLog("4. ❌ User cancels - No PIN entered on phone")
//    addToLog("5. ❌ Insufficient funds - Test phone has no balance")
//    addToLog("")
//
//    addToLog("🎯 QUICK DIAGNOSIS:")
//    addToLog("• Run 'Test ALL M-Pesa Credentials' for detailed analysis")
//    addToLog("• Check all 4 credentials in build.gradle")
//    addToLog("• Ensure Business Shortcode is 174379 for sandbox")
//    addToLog("• Use test phone: 254708374149")
//    addToLog("• Test amount: 1 KSh")
//    addToLog("")
//
//    addToLog("🚀 RECOMMENDED ACTION:")
//    addToLog("Click 'Test ALL M-Pesa Credentials' button above")
//    addToLog("It will test all credentials and pinpoint the exact issue")
//
//    showToast(context, "Run comprehensive test to find exact failure reason")
//}
//
//private fun showToast(context: android.content.Context, message: String) {
//    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
//}