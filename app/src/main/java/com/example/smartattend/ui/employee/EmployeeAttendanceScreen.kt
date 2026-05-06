package com.example.smartattend.ui.employee

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.smartattend.data.model.Attendance
import com.example.smartattend.viewmodel.AttendanceViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

@Composable
fun EmployeeAttendanceScreen(
    attendanceViewModel: AttendanceViewModel
) {
    val context = LocalContext.current
    val uiState by attendanceViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        attendanceViewModel.loadAttendanceData()
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val locationGranted =
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (locationGranted) {
            getCurrentLocationForCheckout(
                context = context,
                onLocationReady = { location ->
                    attendanceViewModel.checkOut(location)
                },
                onError = { error ->
                    // For simple version, error is not stored in ViewModel.
                }
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = "My Attendance",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Check your daily attendance and check out.",
            modifier = Modifier.padding(top = 6.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        TodayAttendanceCard(
            todayAttendance = uiState.todayAttendance,
            isLoading = uiState.isLoading,
            onCheckOutClick = {
                attendanceViewModel.clearMessages()

                if (hasLocationPermission(context)) {
                    getCurrentLocationForCheckout(
                        context = context,
                        onLocationReady = { location ->
                            attendanceViewModel.checkOut(location)
                        },
                        onError = {
                            // optional later
                        }
                    )
                } else {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            }
        )

        uiState.successMessage?.let {
            Spacer(modifier = Modifier.height(14.dp))
            SuccessBox(message = it)
        }

        uiState.errorMessage?.let {
            Spacer(modifier = Modifier.height(14.dp))
            ErrorBox(message = it)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Attendance History",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (uiState.attendanceHistory.isEmpty()) {
            EmptyHistoryCard()
        } else {
            uiState.attendanceHistory.forEach { attendance ->
                AttendanceHistoryItem(attendance = attendance)
            }
        }
    }
}

@Composable
private fun TodayAttendanceCard(
    todayAttendance: Attendance?,
    isLoading: Boolean,
    onCheckOutClick: () -> Unit
) {
    val title = when {
        todayAttendance == null -> "Not Checked In"
        todayAttendance.checkOutTime.isBlank() -> "Checked In"
        else -> "Completed"
    }

    val subtitle = when {
        todayAttendance == null -> "Scan QR first to check in."
        todayAttendance.checkOutTime.isBlank() -> "You can check out when leaving workplace."
        else -> "You already checked out today."
    }

    val canCheckOut = todayAttendance != null && todayAttendance.checkOutTime.isBlank()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Column(
            modifier = Modifier.padding(22.dp)
        ) {
            Text(
                text = "Today",
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = subtitle,
                modifier = Modifier.padding(top = 6.dp),
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
            )

            if (todayAttendance != null) {
                Spacer(modifier = Modifier.height(16.dp))

                AttendanceInfoRow("Status", todayAttendance.status)
                AttendanceInfoRow("Check-in", todayAttendance.checkInTime.ifBlank { "-" })
                AttendanceInfoRow("Check-out", todayAttendance.checkOutTime.ifBlank { "-" })
                AttendanceInfoRow("Workplace", todayAttendance.workplaceName)
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = onCheckOutClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                enabled = canCheckOut && !isLoading,
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                    disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Check Out",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun AttendanceHistoryItem(
    attendance: Attendance
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = attendance.date,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )

                    Text(
                        text = attendance.workplaceName,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }

                AssistChip(
                    onClick = {},
                    label = {
                        Text(attendance.status)
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            ProfileInfoRow("Check-in", attendance.checkInTime.ifBlank { "-" })
            ProfileInfoRow("Check-out", attendance.checkOutTime.ifBlank { "-" })
            ProfileInfoRow("Distance", "${attendance.distanceMeter.toInt()} m")
        }
    }
}

@Composable
private fun AttendanceInfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
        )

        Text(
            text = value,
            color = MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun EmptyHistoryCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp)
    ) {
        Text(
            text = "No attendance history yet.",
            modifier = Modifier.padding(18.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SuccessBox(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun ErrorBox(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.errorContainer
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.onErrorContainer
        )
    }
}

private fun hasLocationPermission(context: Context): Boolean {
    val fineLocation = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    val coarseLocation = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    return fineLocation || coarseLocation
}

@SuppressLint("MissingPermission")
private fun getCurrentLocationForCheckout(
    context: Context,
    onLocationReady: (Location) -> Unit,
    onError: (String) -> Unit
) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    fusedLocationClient
        .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
        .addOnSuccessListener { location ->
            if (location != null) {
                onLocationReady(location)
            } else {
                onError("Cannot get current location. Please enable GPS.")
            }
        }
        .addOnFailureListener {
            onError(it.message ?: "Failed to get location")
        }
}