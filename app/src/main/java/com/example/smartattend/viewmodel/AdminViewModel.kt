package com.example.smartattend.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartattend.data.repository.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AdminUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val hrCreated: Boolean = false,
    val totalHr: Int = 0,
    val totalEmployees: Int = 0
)

class AdminViewModel(
    private val adminRepository: AdminRepository = AdminRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val hrResult = adminRepository.getHrCount()
            val employeeResult = adminRepository.getEmployeeCount()

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                totalHr = hrResult.getOrDefault(0),
                totalEmployees = employeeResult.getOrDefault(0)
            )
        }
    }

    fun createHr(
        fullName: String,
        email: String,
        password: String,
        phone: String,
        imageUri: Uri?
    ) {
        val cleanName = fullName.trim()
        val cleanEmail = email.trim()
        val cleanPassword = password.trim()
        val cleanPhone = phone.trim()

        if (cleanName.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "HR name is required")
            return
        }

        if (cleanEmail.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "HR email is required")
            return
        }

        if (cleanPassword.length < 6) {
            _uiState.value = _uiState.value.copy(errorMessage = "Password must be at least 6 characters")
            return
        }

        viewModelScope.launch {
            _uiState.value = AdminUiState(isLoading = true)

            val result = adminRepository.createHrAccount(
                fullName = cleanName,
                email = cleanEmail,
                password = cleanPassword,
                phone = cleanPhone,
                imageUri = imageUri
            )

            result
                .onSuccess {
                    _uiState.value = AdminUiState(
                        isLoading = false,
                        hrCreated = true,
                        successMessage = "HR account created successfully. Please login again as Admin."
                    )
                }
                .onFailure { error ->
                    _uiState.value = AdminUiState(
                        isLoading = false,
                        errorMessage = error.message ?: "Failed to create HR account"
                    )
                }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }

    fun resetCreateHrState() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null,
            hrCreated = false,
            isLoading = false
        )
    }
}