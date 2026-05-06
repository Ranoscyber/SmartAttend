package com.example.smartattend.viewmodel

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartattend.data.model.Attendance
import com.example.smartattend.data.repository.AttendanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AttendanceUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val todayAttendance: Attendance? = null,
    val attendanceHistory: List<Attendance> = emptyList()
)

class AttendanceViewModel(
    private val attendanceRepository: AttendanceRepository = AttendanceRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AttendanceUiState())
    val uiState: StateFlow<AttendanceUiState> = _uiState.asStateFlow()

    fun loadAttendanceData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val todayResult = attendanceRepository.getTodayAttendance()
            val historyResult = attendanceRepository.getMyAttendanceHistory()

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                todayAttendance = todayResult.getOrDefault(null),
                attendanceHistory = historyResult.getOrDefault(emptyList())
            )
        }
    }

    fun checkIn(
        scannedQrValue: String,
        location: Location
    ) {
        if (scannedQrValue.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "QR code is empty"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, successMessage = null)

            val result = attendanceRepository.checkInWithQr(
                scannedQrValue = scannedQrValue,
                location = location
            )

            result
                .onSuccess { message ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = message
                    )
                    loadAttendanceData()
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Check-in failed"
                    )
                }
        }
    }

    fun checkOut(location: Location) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, successMessage = null)

            val result = attendanceRepository.checkOut(location)

            result
                .onSuccess { message ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = message
                    )
                    loadAttendanceData()
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Check-out failed"
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

    fun showError(message: String) {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            errorMessage = message,
            successMessage = null
        )
    }
}