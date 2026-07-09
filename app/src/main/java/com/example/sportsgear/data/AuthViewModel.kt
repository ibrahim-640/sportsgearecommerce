package com.example.sportsgear.data

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.sportsgear.models.UserModel
import com.example.sportsgear.navigation.ROUTE_LOGIN
import com.example.sportsgear.navigation.ROUTE_REGISTER
import com.example.sportsgear.navigation.ROUTE_STARTER
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val dbRef = FirebaseDatabase.getInstance().getReference("Users")
    private val adminRef = FirebaseDatabase.getInstance().getReference("Admin")

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
    val errorMessage = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    fun clearError() { _errorMessage.value = null }
    fun clearSuccess() { _successMessage.value = null }

    // ----------------------------------------------------------
    // REAL-TIME AUTH STATE LISTENER
    // ✅ FIX — the listener is now a named, stored property instead of an
    // inline anonymous lambda. Previously there was no way to detach it,
    // and this class had no onCleared() override at all, so every
    // AuthViewModel instance (and every screen used to create its own —
    // HomeScreen, CartScreen, CategoryScreen, etc.) permanently leaked one
    // of these. Now it's removed in onCleared() below. The real fix is
    // sharing ONE instance app-wide (see AppNavHost.kt), but this is
    // correct defense-in-depth regardless.
    // ----------------------------------------------------------
    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
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

    init {
        auth.addAuthStateListener(authStateListener)
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
            _errorMessage.value = "Please fill all fields"
            return
        }

        _isLoading.value = true

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                _isLoading.value = false
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid ?: run {
                        _errorMessage.value = "Authentication error. Please try again."
                        return@addOnCompleteListener
                    }
                    val newUser = UserModel(
                        firstname = firstname,
                        lastname = lastname,
                        email = email,
                        userId = uid,
                        isAdmin = false
                    )
                    dbRef.child(uid).setValue(newUser)
                        .addOnSuccessListener {
                            _successMessage.value = "Registered successfully"
                            // ✅ FIX — sign out before sending to Login. The user is
                            // technically already authenticated at this point (Firebase
                            // auto-signs-in on createUserWithEmailAndPassword), so without
                            // this they'd be "logged in" while still staring at the login
                            // form, and anything reactive to currentUser elsewhere
                            // (e.g. AppNavHost loading their cart) would fire early.
                            auth.signOut()
                            navController.navigate(ROUTE_LOGIN) {
                                popUpTo(ROUTE_REGISTER) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                        .addOnFailureListener {
                            _errorMessage.value = it.message
                        }
                } else {
                    _errorMessage.value = task.exception?.message ?: "Registration failed"
                }
            }
    }

    // ----------------------------------------------------------
    // LOGIN
    // ----------------------------------------------------------
    fun login(
        email: String,
        password: String,
        navController: NavController,
        context: Context
    ) {
        if (email.isBlank() || password.isBlank()) {
            _errorMessage.value = "Email and password required"
            return
        }

        _isLoading.value = true

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                _isLoading.value = false
                if (!task.isSuccessful) {
                    _errorMessage.value = task.exception?.message ?: "Login failed"
                    return@addOnCompleteListener
                }

                val user = auth.currentUser!!
                _currentUser.value = user

                checkAdminStatus(user.uid) {
                    fetchUserFullName(user.uid) {
                        _successMessage.value = "Welcome back!"
                        navController.navigate(ROUTE_STARTER) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
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
                _isAdmin.value = null

                val snapshot = adminRef.child(uid).get().await()

                Log.d("AuthVM", "Admin snapshot exists: ${snapshot.exists()}")
                Log.d("AuthVM", "Admin snapshot value: ${snapshot.value}")

                val rawValue: String? = when {
                    !snapshot.exists() -> null
                    snapshot.child("isAdmin").exists() ->
                        snapshot.child("isAdmin").value?.toString()
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
    fun logout() {
        auth.signOut()
        _currentUser.value = null
        _isAdmin.value = false
        _fullName.value = null
        _successMessage.value = "Logged out successfully"
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
                dbRef.child(user.uid)
                    .updateChildren(mapOf(
                        "firstname" to newFirstName,
                        "lastname" to newLastName
                    ))
                    .addOnSuccessListener {
                        _fullName.value = fullName
                        _successMessage.value = "Profile updated successfully"
                        onSuccess()
                    }
                    .addOnFailureListener {
                        onError("DB error: ${it.message}")
                    }
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

    // ----------------------------------------------------------
    // CLEANUP
    // ✅ NEW — detaches the auth state listener when this ViewModel is
    // cleared. Without this, every instance ever created (one per screen,
    // before the shared-instance fix) leaked its listener permanently.
    // ----------------------------------------------------------
    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authStateListener)
    }
}