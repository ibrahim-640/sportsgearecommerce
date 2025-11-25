package com.yourapp.ui.screens.debug
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.ktx.Firebase
import android.widget.Toast
import com.example.sportsgear.data.CartViewModel
import com.google.firebase.auth.ktx.auth
import com.example.sportsgear.models.Product
import com.google.firebase.database.ktx.database
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

@Composable
fun DebugScreen(
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val cartViewModel: CartViewModel = viewModel()
    var debugLog by remember { mutableStateOf("=== FIREBASE PERMISSION FIX ===\n") }

    val addToLog: (String) -> Unit = { message ->
        debugLog += "$message\n"
        Log.d("FirebaseDebug", message)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Firebase Permission Fix", style = MaterialTheme.typography.headlineSmall)
            Button(onClick = onBack) { Text("Back") }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth().height(300.dp)) {
            Text(text = debugLog, modifier = Modifier.padding(8.dp).fillMaxSize(), style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DebugButton(text = "1. 🔧 Fix Firebase Rules & Data", onClick = {
                addToLog("\n--- FIXING FIREBASE RULES & DATA ---")
                fixFirebaseRulesAndData(addToLog, context)
            })

            DebugButton(text = "2. 🛠️ Complete Admin Setup", onClick = {
                addToLog("\n--- COMPLETE ADMIN SETUP ---")
                completeAdminSetup(addToLog, context)
            })

            DebugButton(text = "3. 🔍 Test All Permissions", onClick = {
                addToLog("\n--- TESTING ALL PERMISSIONS ---")
                testAllPermissions(addToLog, context, cartViewModel)
            })

            Button(
                onClick = { debugLog = "=== DEBUG CLEARED ===\n" },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) { Text("Clear Log") }
        }
    }
}

@Composable
fun DebugButton(text: String, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.fillMaxWidth()) { Text(text) }
}

private fun fixFirebaseRulesAndData(
    addToLog: (String) -> Unit,
    context: android.content.Context
) {
    val user = Firebase.auth.currentUser
    if (user == null) {
        addToLog("❌ Please login first")
        showToast(context, "Please login first")
        return
    }

    addToLog("🎯 FIXING FIREBASE PERMISSION ISSUES")
    addToLog("User: ${user.email}")
    addToLog("UID: ${user.uid}")
    addToLog("")

    addToLog("📋 REQUIRED FIREBASE RULES:")
    addToLog("Go to: Firebase Console → Realtime Database → Rules")
    addToLog("Replace with:")
    addToLog("```json")
    addToLog("{")
    addToLog("  \"rules\": {")
    addToLog("    \"Admins\": {")
    addToLog("      \"\$adminId\": {")
    addToLog("        \".read\": \"auth != null\",")
    addToLog("        \".write\": \"auth != null && auth.uid === \$adminId\"")
    addToLog("      }")
    addToLog("    },")
    addToLog("    \"Products\": {")
    addToLog("      \".read\": \"auth != null\",")
    addToLog("      \".write\": \"auth != null\"")
    addToLog("    },")
    addToLog("    \"Users\": {")
    addToLog("      \"\$userId\": {")
    addToLog("        \".read\": \"auth != null && auth.uid === \$userId\",")
    addToLog("        \".write\": \"auth != null && auth.uid === \$userId\"")
    addToLog("      }")
    addToLog("    },")
    addToLog("    \"Cart\": {")
    addToLog("      \"\$userId\": {")
    addToLog("        \".read\": \"auth != null && auth.uid === \$userId\",")
    addToLog("        \".write\": \"auth != null && auth.uid === \$userId\"")
    addToLog("      }")
    addToLog("    },")
    addToLog("    \"Orders\": {")
    addToLog("      \"\$userId\": {")
    addToLog("        \".read\": \"auth != null && auth.uid === \$userId\",")
    addToLog("        \".write\": \"auth != null && auth.uid === \$userId\"")
    addToLog("      }")
    addToLog("    }")
    addToLog("  }")
    addToLog("}")
    addToLog("```")
    addToLog("")
    addToLog("🔧 These rules will fix:")
    addToLog("   ✅ 'Permission denied' errors")
    addToLog("   ✅ Admin icon visibility")
    addToLog("   ✅ Cart functionality")
    addToLog("   ✅ User profile loading")
    addToLog("")

    showToast(context, "Update Firebase rules - check logs for instructions")
}

private fun completeAdminSetup(
    addToLog: (String) -> Unit,
    context: android.content.Context
) {
    val user = Firebase.auth.currentUser
    if (user == null) {
        addToLog("❌ Please login as admin first")
        return
    }

    addToLog("👑 COMPLETE ADMIN SETUP")
    addToLog("Setting up: ${user.email}")
    addToLog("")

    val database = Firebase.database

    // Step 1: Setup Admins collection
    addToLog("1. Setting up /Admins...")
    val adminRef = database.getReference("Admins").child(user.uid)
    adminRef.setValue(mapOf(
        "email" to user.email,
        "isAdmin" to true,
        "role" to "admin",
        "setupAt" to System.currentTimeMillis()
    )).addOnSuccessListener {
        addToLog("   ✅ /Admins setup complete")

        // Step 2: Setup Users collection
        addToLog("2. Setting up /Users...")
        val userRef = database.getReference("Users").child(user.uid)
        userRef.setValue(mapOf(
            "email" to user.email,
            "isAdmin" to true,
            "role" to "admin",
            "firstname" to "Admin",
            "lastname" to "User",
            "setupAt" to System.currentTimeMillis()
        )).addOnSuccessListener {
            addToLog("   ✅ /Users setup complete")

            // Step 3: Test permissions
            addToLog("3. Testing permissions...")
            testUserPermissions(user.uid, addToLog, context)
        }.addOnFailureListener { e ->
            addToLog("   ❌ /Users setup failed: ${e.message}")
        }
    }.addOnFailureListener { e ->
        addToLog("   ❌ /Admins setup failed: ${e.message}")
    }
}

private fun testUserPermissions(
    userId: String,
    addToLog: (String) -> Unit,
    context: android.content.Context
) {
    val database = Firebase.database

    // Test Users read
    addToLog("   Testing /Users read...")
    database.getReference("Users").child(userId).get()
        .addOnSuccessListener {
            addToLog("      ✅ /Users read: SUCCESS")

            // Test Cart access
            addToLog("   Testing /Cart access...")
            database.getReference("Cart").child(userId).child("test").setValue("test")
                .addOnSuccessListener {
                    addToLog("      ✅ /Cart write: SUCCESS")

                    // Test Products read
                    addToLog("   Testing /Products read...")
                    database.getReference("Products").limitToFirst(1).get()
                        .addOnSuccessListener {
                            addToLog("      ✅ /Products read: SUCCESS")

                            addToLog("")
                            addToLog("🎉 ADMIN SETUP COMPLETE!")
                            addToLog("✅ All permissions working")
                            addToLog("✅ Admin data created")
                            addToLog("")
                            addToLog("📱 NEXT STEPS:")
                            addToLog("   1. Update Firebase rules (Step 1)")
                            addToLog("   2. Force stop and restart app")
                            addToLog("   3. Login as admin")
                            addToLog("   4. Admin icon should appear")

                            showToast(context, "Admin setup complete! Update rules & restart app.")
                        }.addOnFailureListener { e ->
                            addToLog("      ❌ /Products read failed: ${e.message}")
                        }
                }.addOnFailureListener { e ->
                    addToLog("      ❌ /Cart write failed: ${e.message}")
                }
        }.addOnFailureListener { e ->
            addToLog("      ❌ /Users read failed: ${e.message}")
        }
}

private fun testAllPermissions(
    addToLog: (String) -> Unit,
    context: android.content.Context,
    cartViewModel: CartViewModel
) {
    val user = Firebase.auth.currentUser
    if (user == null) {
        addToLog("❌ Please login first")
        return
    }

    addToLog("🧪 TESTING ALL PERMISSIONS")
    addToLog("User: ${user.email}")
    addToLog("")

    val database = Firebase.database
    val userId = user.uid

    // Test 1: Users collection
    addToLog("1. Testing /Users access...")
    database.getReference("Users").child(userId).get()
        .addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                addToLog("   ✅ /Users read: SUCCESS")
                val isAdmin = snapshot.child("isAdmin").getValue(Boolean::class.java) ?: false
                val role = snapshot.child("role").getValue(String::class.java) ?: "user"
                addToLog("   Admin status: isAdmin=$isAdmin, role=$role")
            } else {
                addToLog("   ❌ /Users read: NO DATA (run Admin Setup)")
            }
        }.addOnFailureListener { e ->
            addToLog("   ❌ /Users read: FAILED - ${e.message}")
        }

    // Test 2: Cart collection
    addToLog("2. Testing /Cart access...")
    database.getReference("Cart").child(userId).child("test").setValue(System.currentTimeMillis())
        .addOnSuccessListener {
            addToLog("   ✅ /Cart write: SUCCESS")
            database.getReference("Cart").child(userId).child("test").removeValue()
        }.addOnFailureListener { e ->
            addToLog("   ❌ /Cart write: FAILED - ${e.message}")
        }

    // Test 3: Products collection
    addToLog("3. Testing /Products access...")
    database.getReference("Products").limitToFirst(1).get()
        .addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                addToLog("   ✅ /Products read: SUCCESS")
            } else {
                addToLog("   ℹ️ /Products read: SUCCESS (no products yet)")
            }
        }.addOnFailureListener { e ->
            addToLog("   ❌ /Products read: FAILED - ${e.message}")
        }

    // Test 4: CartViewModel
    addToLog("4. Testing CartViewModel...")
    val testProduct = Product(
        productId = "test_${System.currentTimeMillis()}",
        name = "Test Product",
        price = "10.00",
        imageUrl = "",
        quantity = "1",
        category = "Test",
        value = "10.00",
        description = "Test",
        isOnOffer = false
    )

    cartViewModel.addToCart(userId, testProduct)
    addToLog("   📤 CartViewModel.addToCart() called")
    addToLog("   Check Logcat for 'DebugCart' results")

    addToLog("")
    addToLog("📊 PERMISSION SUMMARY:")
    addToLog("If any tests failed, update Firebase rules (Step 1)")

    showToast(context, "Permission test complete - check logs")
}

private fun showToast(context: android.content.Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
}