package com.example.smartattend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartattend.data.model.AppUser
import com.example.smartattend.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val loggedInUser: AppUser? = null,
    val resetEmailSent: Boolean = false,
    val isLoggedOut: Boolean = false
)

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        val cleanEmail = email.trim()
        val cleanPassword = password.trim()

        if (cleanEmail.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Email is required",
                successMessage = null
            )
            return
        }

        if (cleanPassword.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Password is required",
                successMessage = null
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)

            val result = authRepository.login(cleanEmail, cleanPassword)

            result
                .onSuccess { user ->
                    _uiState.value = AuthUiState(
                        isLoading = false,
                        loggedInUser = user
                    )
                }
                .onFailure { error ->
                    _uiState.value = AuthUiState(
                        isLoading = false,
                        errorMessage = error.message ?: "Login failed"
                    )
                }
        }
    }

    fun forgotPassword(email: String) {
        val cleanEmail = email.trim()

        if (cleanEmail.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Email is required",
                successMessage = null,
                resetEmailSent = false
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)

            val result = authRepository.sendPasswordResetEmail(cleanEmail)

            result
                .onSuccess {
                    _uiState.value = AuthUiState(
                        isLoading = false,
                        resetEmailSent = true,
                        successMessage = "Password reset email sent successfully. Please check your Gmail."
                    )
                }
                .onFailure { error ->
                    _uiState.value = AuthUiState(
                        isLoading = false,
                        errorMessage = error.message ?: "Failed to send reset email",
                        resetEmailSent = false
                    )
                }
        }
    }

    fun checkCurrentUser() {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)

            val result = authRepository.getCurrentUserData()

            result
                .onSuccess { user ->
                    _uiState.value = AuthUiState(
                        isLoading = false,
                        loggedInUser = user
                    )
                }
                .onFailure {
                    _uiState.value = AuthUiState(
                        isLoading = false,
                        loggedInUser = null
                    )
                }
        }
    }

    fun logout() {
        authRepository.logout()

        _uiState.value = AuthUiState(
            isLoggedOut = true,
            loggedInUser = null
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null
        )
    }

    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(
            successMessage = null
        )
    }

    fun clearResetEmailSent() {
        _uiState.value = _uiState.value.copy(
            resetEmailSent = false,
            successMessage = null,
            errorMessage = null
        )
    }

    fun resetAuthStateForForgotPassword() {
        _uiState.value = _uiState.value.copy(
            resetEmailSent = false,
            successMessage = null,
            errorMessage = null,
            isLoading = false
        )
    }
}