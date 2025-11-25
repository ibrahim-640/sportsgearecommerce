package com.example.sportsgear.data

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.sportsgear.models.UserModel
import com.example.sportsgear.navigation.ROUTE_LOGIN
import com.example.sportsgear.navigation.ROUTE_STARTER
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val dbRef = FirebaseDatabase.getInstance().getReference("Users")
    private val adminRef = FirebaseDatabase.getInstance().getReference("Admins")

    // --------------------------
    // FLOWS
    // --------------------------
    private val _currentUser = MutableStateFlow(auth.currentUser)
    val currentUser: StateFlow<com.google.firebase.auth.FirebaseUser?> = _currentUser

    private val _isAdmin = MutableStateFlow<Boolean?>(null)
    val isAdmin: StateFlow<Boolean?> = _isAdmin

    private val _fullName = MutableStateFlow<String?>(null)
    val fullName: StateFlow<String?> = _fullName

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // ----------------------------------------------------------
    // REAL-TIME AUTH STATE LISTENER
    // ----------------------------------------------------------
    init {
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            _currentUser.value = user

            if (user == null) {
                _isAdmin.value = false
                _fullName.value = null
            } else {
                checkAdminStatus(user.uid)
                fetchUserFullName(user.uid)
            }
        }
    }

    // ----------------------------------------------------------
    // SIGNUP
    // ----------------------------------------------------------
    fun signup(
        firstname: String,
        lastname: String,
        email: String,
        password: String,
        navController: NavController,
        context: Context
    ) {
        if (firstname.isBlank() || lastname.isBlank() ||
            email.isBlank() || password.isBlank()
        ) {
            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_LONG).show()
            return
        }

        _isLoading.value = true

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                _isLoading.value = false
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid ?: return@addOnCompleteListener
                    val newUser = UserModel(
                        firstname = firstname,
                        lastname = lastname,
                        email = email,
                        password = password,
                        userId = uid,
                        isAdmin = false
                    )
                    dbRef.child(uid).setValue(newUser)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Registered Successfully", Toast.LENGTH_LONG).show()
                            navController.navigate(ROUTE_LOGIN)
                        }
                        .addOnFailureListener {
                            _errorMessage.value = it.message
                        }
                } else {
                    _errorMessage.value = task.exception?.message
                    Toast.makeText(context, "Registration failed", Toast.LENGTH_LONG).show()
                }
            }
    }

    // ----------------------------------------------------------
    // LOGIN
    // ----------------------------------------------------------
    fun login(email: String, password: String, navController: NavController, context: Context) {
        if (email.isBlank() || password.isBlank()) {
            Toast.makeText(context, "Email & password required", Toast.LENGTH_SHORT).show()
            return
        }

        _isLoading.value = true

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                _isLoading.value = false
                if (!task.isSuccessful) {
                    _errorMessage.value = task.exception?.message
                    Toast.makeText(context, "Login failed", Toast.LENGTH_LONG).show()
                    return@addOnCompleteListener
                }

                val user = auth.currentUser!!
                _currentUser.value = user

                checkAdminStatus(user.uid) {
                    fetchUserFullName(user.uid) {
                        Toast.makeText(context, "Welcome back!", Toast.LENGTH_SHORT).show()
                        navController.navigate(ROUTE_STARTER) {
                            popUpTo(ROUTE_LOGIN) { inclusive = true }
                        }
                    }
                }
            }
    }

    // ----------------------------------------------------------
    // CHECK ADMIN STATUS
    // ----------------------------------------------------------
    fun checkAdminStatus(uid: String, onDone: (() -> Unit)? = null) {
        if (uid.isBlank()) {
            _isAdmin.value = false
            onDone?.invoke()
            return
        }

        viewModelScope.launch {
            try {
                _isAdmin.value = null // loading state

                val snapshot = adminRef.child(uid).get().await()
                val rawValue: String? = when {
                    !snapshot.exists() -> null
                    snapshot.child("isAdmin").exists() -> snapshot.child("isAdmin").value?.toString()
                    else -> snapshot.value?.toString()
                }

                val normalized = rawValue?.trim()?.lowercase()
                val resolved = normalized == "true" || normalized == "1" || normalized == "yes"

                _isAdmin.value = resolved
                Log.d("AuthVM", "checkAdminStatus: raw='$rawValue' resolved=$resolved")

            } catch (e: Exception) {
                Log.e("AuthVM", "Error checking admin", e)
                _isAdmin.value = false
            } finally {
                onDone?.invoke()
            }
        }
    }

    // ----------------------------------------------------------
    // FETCH USER FULL NAME
    // ----------------------------------------------------------
    fun fetchUserFullName(uid: String, onDone: (() -> Unit)? = null) {
        viewModelScope.launch {
            try {
                val snapshot = dbRef.child(uid).get().await()
                if (snapshot.exists()) {
                    val first = snapshot.child("firstname").value?.toString() ?: ""
                    val last = snapshot.child("lastname").value?.toString() ?: ""
                    _fullName.value = "$first $last".trim().ifBlank { "User" }
                } else {
                    _fullName.value = "User"
                }
            } catch (e: Exception) {
                Log.e("AuthVM", "Failed to fetch full name", e)
                _fullName.value = "User"
            } finally {
                onDone?.invoke()
            }
        }
    }

    // ----------------------------------------------------------
    // LOGOUT
    // ----------------------------------------------------------
    fun logout(context: Context) {
        auth.signOut()

        _currentUser.value = null
        _isAdmin.value = false
        _fullName.value = null

        Toast.makeText(context, "Logged out", Toast.LENGTH_SHORT).show()
    }

    // ----------------------------------------------------------
    // UPDATE PROFILE
    // ----------------------------------------------------------
    fun updateUserProfile(
        newFirstName: String,
        newLastName: String,
        context: Context,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val user = auth.currentUser ?: return onError("User not logged in")
        val fullName = "$newFirstName $newLastName".trim()

        val updateReq = UserProfileChangeRequest.Builder()
            .setDisplayName(fullName)
            .build()

        user.updateProfile(updateReq)
            .addOnSuccessListener {
                dbRef.child(user.uid).apply {
                    child("firstname").setValue(newFirstName)
                    child("lastname").setValue(newLastName)
                }
                _fullName.value = fullName
                Toast.makeText(context, "Profile updated", Toast.LENGTH_SHORT).show()
                onSuccess()
            }
            .addOnFailureListener { onError("Auth error: ${it.message}") }
    }

    // ----------------------------------------------------------
    // FETCH PROFILE
    // ----------------------------------------------------------
    fun getCurrentUserProfile(onResult: (String?, String?, String?) -> Unit) {
        val uid = auth.currentUser?.uid ?: return onResult(null, null, null)
        dbRef.child(uid).get()
            .addOnSuccessListener { snapshot ->
                val first = snapshot.child("firstname").getValue(String::class.java)
                val last = snapshot.child("lastname").getValue(String::class.java)
                val email = snapshot.child("email").getValue(String::class.java)
                onResult(first, last, email)
            }
            .addOnFailureListener {
                onResult(null, null, null)
            }
    }
}
