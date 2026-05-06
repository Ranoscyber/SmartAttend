package com.example.smartattend.viewmodel

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartattend.data.repository.AttendanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AttendanceUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class AttendanceViewModel(
    private val attendanceRepository: AttendanceRepository = AttendanceRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AttendanceUiState())
    val uiState: StateFlow<AttendanceUiState> = _uiState.asStateFlow()

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
            _uiState.value = AttendanceUiState(isLoading = true)

            val result = attendanceRepository.checkInWithQr(
                scannedQrValue = scannedQrValue,
                location = location
            )

            result
                .onSuccess { message ->
                    _uiState.value = AttendanceUiState(
                        isLoading = false,
                        successMessage = message
                    )
                }
                .onFailure { error ->
                    _uiState.value = AttendanceUiState(
                        isLoading = false,
                        errorMessage = error.message ?: "Check-in failed"
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
}