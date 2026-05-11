package com.example.smartattend.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartattend.data.model.Employee
import com.example.smartattend.data.repository.EmployeeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EmployeeUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val employee: Employee? = null,
    val profileRequestSent: Boolean = false
)

class EmployeeViewModel(
    private val employeeRepository: EmployeeRepository = EmployeeRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(EmployeeUiState())
    val uiState: StateFlow<EmployeeUiState> = _uiState.asStateFlow()

    fun loadEmployeeProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            val result = employeeRepository.getCurrentEmployee()

            result
                .onSuccess { employee ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        employee = employee
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Failed to load employee profile"
                    )
                }
        }
    }

    fun requestProfileUpdate(
        context: Context,
        fullName: String,
        phone: String,
        gender: String,
        dob: String,
        address: String,
        emergencyContact: String,
        imageUri: Uri?
    ) {
        val employee = _uiState.value.employee

        if (employee == null) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Employee profile not loaded"
            )
            return
        }

        if (fullName.trim().isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Full name is required"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                successMessage = null,
                profileRequestSent = false
            )

            val result = employeeRepository.createProfileUpdateRequest(
                context = context,
                employee = employee,
                fullName = fullName,
                phone = phone,
                gender = gender,
                dob = dob,
                address = address,
                emergencyContact = emergencyContact,
                imageUri = imageUri
            )

            result
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Profile update request sent to HR",
                        profileRequestSent = true
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Failed to send request"
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

    fun resetProfileRequestState() {
        _uiState.value = _uiState.value.copy(
            profileRequestSent = false,
            successMessage = null,
            errorMessage = null
        )
    }
}