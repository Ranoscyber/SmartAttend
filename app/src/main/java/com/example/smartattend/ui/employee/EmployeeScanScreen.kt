package com.example.smartattend.ui.employee

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.smartattend.viewmodel.AttendanceViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

@Composable
fun EmployeeScanScreen(
    attendanceViewModel: AttendanceViewModel
) {
    val context = LocalContext.current
    val uiState by attendanceViewModel.uiState.collectAsState()

    var scannedQrValue by remember { mutableStateOf("") }

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val qrScannerLauncher = rememberLauncherForActivityResult(
        contract = ScanContract()
    ) { result ->
        val qrValue = result.contents

        if (qrValue.isNullOrBlank()) {
            attendanceViewModel.clearMessages()
            return@rememberLauncherForActivityResult
        }

        scannedQrValue = qrValue

        getCurrentLocation(
            context = context,
            onLocationReady = { location ->
                attendanceViewModel.checkIn(
                    scannedQrValue = qrValue,
                    location = location
                )
            },
            onError = { error ->
                attendanceViewModel.clearMessages()
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val cameraGranted = permissions[Manifest.permission.CAMERA] == true
        val locationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (cameraGranted && locationGranted) {
            launchQrScanner(qrScannerLauncher)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .statusBarsPadding()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Scan QR",
            modifier = Modifier.fillMaxWidth(),
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Scan workplace QR to check in.",
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(28.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "📷",
                    fontSize = 56.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Ready to Check In",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Camera and GPS location are required. Fake GPS will be rejected.",
                    modifier = Modifier.padding(top = 8.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        attendanceViewModel.clearMessages()

                        if (hasCameraAndLocationPermission(context)) {
                            launchQrScanner(qrScannerLauncher)
                        } else {
                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.CAMERA,
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !uiState.isLoading,
                    shape = RoundedCornerShape(18.dp)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(
                            text = "Scan QR to Check In",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        if (scannedQrValue.isNotBlank()) {
            InfoCard(
                title = "Last Scanned QR",
                message = scannedQrValue
            )
        }

        uiState.successMessage?.let {
            Spacer(modifier = Modifier.height(14.dp))
            SuccessCard(message = it)
        }

        uiState.errorMessage?.let {
            Spacer(modifier = Modifier.height(14.dp))
            ErrorCardBox(message = it)
        }
    }
}

private fun launchQrScanner(
    qrScannerLauncher: androidx.activity.result.ActivityResultLauncher<ScanOptions>
) {
    val options = ScanOptions()
    options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
    options.setPrompt("Scan SmartAttend workplace QR")
    options.setCameraId(0)
    options.setBeepEnabled(true)
    options.setBarcodeImageEnabled(false)
    options.setOrientationLocked(false)

    qrScannerLauncher.launch(options)
}

private fun hasCameraAndLocationPermission(context: android.content.Context): Boolean {
    val camera = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    val fineLocation = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    val coarseLocation = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    return camera && (fineLocation || coarseLocation)
}

@SuppressLint("MissingPermission")
private fun getCurrentLocation(
    context: android.content.Context,
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

@Composable
private fun InfoCard(
    title: String,
    message: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = message,
                modifier = Modifier.padding(top = 5.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SuccessCard(message: String) {
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
private fun ErrorCardBox(message: String) {
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