package com.example.smartattend.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartattend.data.repository.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.smartattend.data.model.Attendance
import com.example.smartattend.data.model.FakeLocationAlert
import com.example.smartattend.data.model.SalaryReport
import com.example.smartattend.data.model.HRProfile
import com.example.smartattend.data.model.Employee

data class AdminUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val hrCreated: Boolean = false,
    val totalHr: Int = 0,
    val totalEmployees: Int = 0,
    val hrProfiles: List<HRProfile> = emptyList(),
    val attendanceReports: List<Attendance> = emptyList(),
    val fakeLocationAlerts: List<FakeLocationAlert> = emptyList(),
    val salaryReports: List<SalaryReport> = emptyList(),

    val employeeProfiles: List<Employee> = emptyList(),
)

class AdminViewModel(
    private val adminRepository: AdminRepository = AdminRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val hrResult = adminRepository.getHrProfiles()
            val employeeResult = adminRepository.getEmployeeCount()

            val hrProfiles = hrResult.getOrDefault(emptyList())

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                hrProfiles = hrProfiles,
                totalHr = hrProfiles.size,
                totalEmployees = employeeResult.getOrDefault(0)
            )
        }
    }

    fun createHr(
        context: Context,
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
                context = context,
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

    fun loadReportData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val attendanceResult = adminRepository.getAttendanceReports()
            val alertsResult = adminRepository.getFakeLocationAlerts()
            val salaryResult = adminRepository.getSalaryReports()

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                attendanceReports = attendanceResult.getOrDefault(emptyList()),
                fakeLocationAlerts = alertsResult.getOrDefault(emptyList()),
                salaryReports = salaryResult.getOrDefault(emptyList())
            )
        }
    }

    fun markAlertAsRead(alertId: String) {
        if (alertId.isBlank()) return

        viewModelScope.launch {
            val result = adminRepository.markFakeLocationAlertAsRead(alertId)

            result
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        successMessage = "Alert marked as read"
                    )
                    loadReportData()
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = error.message ?: "Failed to update alert"
                    )
                }
        }
    }

    fun loadHrProfiles() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val result = adminRepository.getHrProfiles()

            result
                .onSuccess { hrProfiles ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        hrProfiles = hrProfiles,
                        totalHr = hrProfiles.size
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Failed to load HR list"
                    )
                }
        }
    }

    fun loadPeopleData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            val hrResult = adminRepository.getHrProfiles()
            val employeeResult = adminRepository.getEmployeeProfiles()

            val hrProfiles = hrResult.getOrDefault(emptyList())
            val employeeProfiles = employeeResult.getOrDefault(emptyList())

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                hrProfiles = hrProfiles,
                employeeProfiles = employeeProfiles,
                totalHr = hrProfiles.size,
                totalEmployees = employeeProfiles.size
            )
        }
    }
}