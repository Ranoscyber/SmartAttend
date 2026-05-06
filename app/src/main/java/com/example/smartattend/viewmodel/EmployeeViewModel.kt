package com.example.smartattend.viewmodel

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
    val employee: Employee? = null
)

class EmployeeViewModel(
    private val employeeRepository: EmployeeRepository = EmployeeRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(EmployeeUiState())
    val uiState: StateFlow<EmployeeUiState> = _uiState.asStateFlow()

    fun loadEmployeeProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val result = employeeRepository.getCurrentEmployee()

            result
                .onSuccess { employee ->
                    _uiState.value = EmployeeUiState(
                        isLoading = false,
                        employee = employee
                    )
                }
                .onFailure { error ->
                    _uiState.value = EmployeeUiState(
                        isLoading = false,
                        errorMessage = error.message ?: "Failed to load employee profile"
                    )
                }
        }
    }
}