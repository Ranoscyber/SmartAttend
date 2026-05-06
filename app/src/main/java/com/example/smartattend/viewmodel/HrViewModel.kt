package com.example.smartattend.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartattend.data.model.Department
import com.example.smartattend.data.repository.HrRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HrUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val departments: List<Department> = emptyList(),
    val totalDepartments: Int = 0,
    val totalEmployees: Int = 0,
    val employeeCreated: Boolean = false
)

class HrViewModel(
    private val hrRepository: HrRepository = HrRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HrUiState())
    val uiState: StateFlow<HrUiState> = _uiState.asStateFlow()

    fun loadHrData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val departmentsResult = hrRepository.getDepartments()
            val employeeCountResult = hrRepository.getEmployeeCount()

            val departments = departmentsResult.getOrDefault(emptyList())

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                departments = departments,
                totalDepartments = departments.size,
                totalEmployees = employeeCountResult.getOrDefault(0)
            )
        }
    }

    fun createDepartment(
        name: String,
        description: String
    ) {
        val cleanName = name.trim()
        val cleanDescription = description.trim()

        if (cleanName.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Department name is required")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val result = hrRepository.createDepartment(
                name = cleanName,
                description = cleanDescription
            )

            result
                .onSuccess { departmentId ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Department created successfully: $departmentId"
                    )
                    loadHrData()
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Failed to create department"
                    )
                }
        }
    }

    fun createEmployee(
        context: Context,
        fullName: String,
        email: String,
        password: String,
        phone: String,
        gender: String,
        dob: String,
        address: String,
        emergencyContact: String,
        department: Department?,
        position: String,
        employmentType: String,
        joinDate: String,
        baseSalaryText: String,
        workDaysText: String,
        imageUri: Uri?
    ) {
        val cleanName = fullName.trim()
        val cleanEmail = email.trim()
        val cleanPassword = password.trim()
        val cleanPosition = position.trim()

        if (cleanName.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Employee name is required")
            return
        }

        if (cleanEmail.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Employee email is required")
            return
        }

        if (cleanPassword.length < 6) {
            _uiState.value = _uiState.value.copy(errorMessage = "Password must be at least 6 characters")
            return
        }

        if (department == null) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please select department")
            return
        }

        if (cleanPosition.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Position is required")
            return
        }

        val baseSalary = baseSalaryText.toDoubleOrNull()
        if (baseSalary == null || baseSalary <= 0.0) {
            _uiState.value = _uiState.value.copy(errorMessage = "Valid base salary is required")
            return
        }

        val workDays = workDaysText.toIntOrNull()
        if (workDays == null || workDays <= 0) {
            _uiState.value = _uiState.value.copy(errorMessage = "Valid work days per month is required")
            return
        }

        viewModelScope.launch {
            _uiState.value = HrUiState(isLoading = true, departments = _uiState.value.departments)

            val result = hrRepository.createEmployee(
                context = context,
                fullName = cleanName,
                email = cleanEmail,
                password = cleanPassword,
                phone = phone.trim(),
                gender = gender.trim(),
                dob = dob.trim(),
                address = address.trim(),
                emergencyContact = emergencyContact.trim(),
                department = department,
                position = cleanPosition,
                employmentType = employmentType.trim(),
                joinDate = joinDate.trim(),
                baseSalary = baseSalary,
                workDaysPerMonth = workDays,
                imageUri = imageUri
            )

            result
                .onSuccess { employeeId ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        employeeCreated = true,
                        successMessage = "Employee created successfully: $employeeId"
                    )

                    loadHrData()
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Failed to create employee"
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

    fun resetEmployeeCreatedState() {
        _uiState.value = _uiState.value.copy(
            employeeCreated = false,
            successMessage = null,
            errorMessage = null,
            isLoading = false
        )
    }
}