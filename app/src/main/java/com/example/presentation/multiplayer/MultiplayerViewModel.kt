package com.example.presentation.multiplayer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.remote.AuthManager
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class MultiplayerUiState {
    object Idle : MultiplayerUiState()
    object Loading : MultiplayerUiState()
    data class Authenticated(val user: FirebaseUser, val profile: UserProfile) : MultiplayerUiState()
    data class Error(val message: String) : MultiplayerUiState()
}

data class UserProfile(
    val displayName: String = "",
    val avatarUrl: String = "",
    val totalScore: Int = 0,
    val globalRank: Int = 0,
    val weeklyRank: Int = 0,
    val monthlyRank: Int = 0
)

class MultiplayerViewModel(private val authManager: AuthManager) : ViewModel() {

    private val _uiState = MutableStateFlow<MultiplayerUiState>(MultiplayerUiState.Idle)
    val uiState: StateFlow<MultiplayerUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authManager.currentUser.collect { user ->
                if (user != null) {
                    loadUserProfile(user)
                } else {
                    _uiState.value = MultiplayerUiState.Idle
                }
            }
        }
    }

    private suspend fun loadUserProfile(user: FirebaseUser) {
        _uiState.value = MultiplayerUiState.Loading
        try {
            val snapshot = authManager.firestore.collection("users").document(user.uid).get().await()
            val profile = UserProfile(
                displayName = snapshot.getString("displayName") ?: "Anonymous",
                avatarUrl = snapshot.getString("avatarUrl") ?: "",
                totalScore = snapshot.getLong("totalScore")?.toInt() ?: 0,
                globalRank = snapshot.getLong("globalRank")?.toInt() ?: 0,
                weeklyRank = snapshot.getLong("weeklyRank")?.toInt() ?: 0,
                monthlyRank = snapshot.getLong("monthlyRank")?.toInt() ?: 0
            )
            _uiState.value = MultiplayerUiState.Authenticated(user, profile)
        } catch (e: Exception) {
            _uiState.value = MultiplayerUiState.Error("Failed to load profile: ${e.message}")
        }
    }

    fun signInAnonymously() {
        viewModelScope.launch {
            _uiState.value = MultiplayerUiState.Loading
            val result = authManager.signInAnonymously()
            if (result.isFailure) {
                _uiState.value = MultiplayerUiState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }

    fun signInWithGoogle(webClientId: String) {
        viewModelScope.launch {
            _uiState.value = MultiplayerUiState.Loading
            val result = authManager.signInWithGoogle(webClientId)
            if (result.isFailure) {
                _uiState.value = MultiplayerUiState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }
    
    fun signOut() {
        authManager.signOut()
    }
}
