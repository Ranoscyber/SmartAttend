package com.example.smartattend.viewmodel
import com.example.smartattend.data.model.Attendance
import com.example.smartattend.data.model.FakeLocationAlert

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
import com.example.smartattend.data.model.Workplace

data class HrUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val departments: List<Department> = emptyList(),
    val totalDepartments: Int = 0,
    val totalEmployees: Int = 0,
    val employeeCreated: Boolean = false,
    val workplace: Workplace? = null,
    val attendanceReports: List<Attendance> = emptyList(),
    val fakeLocationAlerts: List<FakeLocationAlert> = emptyList()
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
            val workplaceResult = hrRepository.getActiveWorkplace()

            val departments = departmentsResult.getOrDefault(emptyList())

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                departments = departments,
                totalDepartments = departments.size,
                totalEmployees = employeeCountResult.getOrDefault(0),
                workplace = workplaceResult.getOrDefault(null)
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

    fun saveWorkplace(
        name: String,
        latitudeText: String,
        longitudeText: String,
        allowedRadiusText: String,
        startTime: String,
        lateAfterTime: String
    ) {
        val cleanName = name.trim()
        val latitude = latitudeText.trim().toDoubleOrNull()
        val longitude = longitudeText.trim().toDoubleOrNull()
        val allowedRadius = allowedRadiusText.trim().toDoubleOrNull()

        if (cleanName.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Workplace name is required")
            return
        }

        if (latitude == null) {
            _uiState.value = _uiState.value.copy(errorMessage = "Valid latitude is required")
            return
        }

        if (longitude == null) {
            _uiState.value = _uiState.value.copy(errorMessage = "Valid longitude is required")
            return
        }

        if (allowedRadius == null || allowedRadius <= 0.0) {
            _uiState.value = _uiState.value.copy(errorMessage = "Valid allowed radius is required")
            return
        }

        if (startTime.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Start time is required")
            return
        }

        if (lateAfterTime.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Late after time is required")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val result = hrRepository.saveWorkplace(
                name = cleanName,
                latitude = latitude,
                longitude = longitude,
                allowedRadius = allowedRadius,
                startTime = startTime.trim(),
                lateAfterTime = lateAfterTime.trim()
            )

            result
                .onSuccess { workplaceId ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Workplace saved successfully: $workplaceId"
                    )

                    loadHrData()
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Failed to save workplace"
                    )
                }
        }
    }

    fun loadReportData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val attendanceResult = hrRepository.getAttendanceReports()
            val alertsResult = hrRepository.getFakeLocationAlerts()

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                attendanceReports = attendanceResult.getOrDefault(emptyList()),
                fakeLocationAlerts = alertsResult.getOrDefault(emptyList())
            )
        }
    }

    fun markAlertAsRead(alertId: String) {
        if (alertId.isBlank()) return

        viewModelScope.launch {
            val result = hrRepository.markFakeLocationAlertAsRead(alertId)

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
}