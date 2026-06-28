package com.example.data.remote

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import android.util.Log

class AuthManager(private val context: Context) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    
    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    init {
        auth.addAuthStateListener { firebaseAuth ->
            _currentUser.value = firebaseAuth.currentUser
        }
    }

    suspend fun signInAnonymously(): Result<FirebaseUser> {
        return try {
            val result = auth.signInAnonymously().await()
            val user = result.user ?: throw Exception("User is null")
            setupNewProfile(user)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithGoogle(webClientId: String): Result<FirebaseUser> {
        return try {
            val credentialManager = CredentialManager.create(context)
            
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(true)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(context, request)
            val credential = result.credential
            
            if (credential is GoogleIdTokenCredential) {
                val idToken = credential.idToken
                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(firebaseCredential).await()
                val user = authResult.user ?: throw Exception("User is null")
                setupNewProfile(user)
                Result.success(user)
            } else {
                throw Exception("Unexpected credential type")
            }
        } catch (e: Exception) {
            Log.e("AuthManager", "Google Sign In Failed", e)
            Result.failure(e)
        }
    }
    
    private suspend fun setupNewProfile(user: FirebaseUser) {
        val userRef = firestore.collection("users").document(user.uid)
        val snapshot = userRef.get().await()
        if (!snapshot.exists()) {
            val initialData = hashMapOf(
                "displayName" to (user.displayName ?: "Player_${user.uid.take(5)}"),
                "avatarUrl" to (user.photoUrl?.toString() ?: ""),
                "globalRank" to 0,
                "weeklyRank" to 0,
                "monthlyRank" to 0,
                "totalScore" to 0,
                "achievements" to emptyList<String>(),
                "friends" to emptyList<String>()
            )
            userRef.set(initialData).await()
        }
    }

    fun signOut() {
        auth.signOut()
    }
}
