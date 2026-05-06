package com.example.smartattend.ui.hr

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartattend.data.model.Workplace
import com.example.smartattend.util.QrCodeGenerator
import com.example.smartattend.viewmodel.HrViewModel

@Composable
fun HrWorkplaceScreen(
    viewModel: HrViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val workplace = uiState.workplace

    var name by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }
    var allowedRadius by remember { mutableStateOf("100") }
    var startTime by remember { mutableStateOf("08:00") }
    var lateAfterTime by remember { mutableStateOf("08:15") }

    LaunchedEffect(workplace?.workplaceId) {
        if (workplace != null) {
            name = workplace.name
            latitude = workplace.latitude.toString()
            longitude = workplace.longitude.toString()
            allowedRadius = workplace.allowedRadius.toString()
            startTime = workplace.startTime
            lateAfterTime = workplace.lateAfterTime
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
            text = "Workplace",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Set office location and permanent QR code.",
            modifier = Modifier.padding(top = 6.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(22.dp))

        WorkplaceFormCard(
            name = name,
            onNameChange = {
                name = it
                viewModel.clearMessages()
            },
            latitude = latitude,
            onLatitudeChange = {
                latitude = it
                viewModel.clearMessages()
            },
            longitude = longitude,
            onLongitudeChange = {
                longitude = it
                viewModel.clearMessages()
            },
            allowedRadius = allowedRadius,
            onAllowedRadiusChange = {
                allowedRadius = it
                viewModel.clearMessages()
            },
            startTime = startTime,
            onStartTimeChange = {
                startTime = it
                viewModel.clearMessages()
            },
            lateAfterTime = lateAfterTime,
            onLateAfterTimeChange = {
                lateAfterTime = it
                viewModel.clearMessages()
            },
            isLoading = uiState.isLoading,
            onSaveClick = {
                viewModel.saveWorkplace(
                    name = name,
                    latitudeText = latitude,
                    longitudeText = longitude,
                    allowedRadiusText = allowedRadius,
                    startTime = startTime,
                    lateAfterTime = lateAfterTime
                )
            }
        )

        uiState.errorMessage?.let {
            Spacer(modifier = Modifier.height(14.dp))
            ErrorBox(message = it)
        }

        uiState.successMessage?.let {
            Spacer(modifier = Modifier.height(14.dp))
            SuccessBox(message = it)
        }

        Spacer(modifier = Modifier.height(22.dp))

        if (workplace != null) {
            WorkplaceQrCard(workplace = workplace)
        } else {
            EmptyWorkplaceCard()
        }
    }
}

@Composable
private fun WorkplaceFormCard(
    name: String,
    onNameChange: (String) -> Unit,
    latitude: String,
    onLatitudeChange: (String) -> Unit,
    longitude: String,
    onLongitudeChange: (String) -> Unit,
    allowedRadius: String,
    onAllowedRadiusChange: (String) -> Unit,
    startTime: String,
    onStartTimeChange: (String) -> Unit,
    lateAfterTime: String,
    onLateAfterTimeChange: (String) -> Unit,
    isLoading: Boolean,
    onSaveClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Column(
            modifier = Modifier.padding(22.dp)
        ) {
            Text(
                text = "Workplace Setting",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Use Google Maps to copy latitude and longitude.",
                modifier = Modifier.padding(top = 6.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            InputField(
                value = name,
                onValueChange = onNameChange,
                label = "Workplace Name *"
            )

            InputField(
                value = latitude,
                onValueChange = onLatitudeChange,
                label = "Latitude *",
                keyboardType = KeyboardType.Number
            )

            InputField(
                value = longitude,
                onValueChange = onLongitudeChange,
                label = "Longitude *",
                keyboardType = KeyboardType.Number
            )

            InputField(
                value = allowedRadius,
                onValueChange = onAllowedRadiusChange,
                label = "Allowed Radius Meter *",
                keyboardType = KeyboardType.Number
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InputField(
                    value = startTime,
                    onValueChange = onStartTimeChange,
                    label = "Start Time",
                    modifier = Modifier.weight(1f)
                )

                InputField(
                    value = lateAfterTime,
                    onValueChange = onLateAfterTimeChange,
                    label = "Late After",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            Button(
                onClick = onSaveClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading,
                shape = RoundedCornerShape(18.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = "Save Workplace",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkplaceQrCard(
    workplace: Workplace
) {
    val qrBitmap = remember(workplace.qrCodeValue) {
        QrCodeGenerator.generateQrBitmap(workplace.qrCodeValue)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Permanent QR Code",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = workplace.name,
                modifier = Modifier.padding(top = 4.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(18.dp))

            Surface(
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Image(
                    bitmap = qrBitmap.asImageBitmap(),
                    contentDescription = "Workplace QR Code",
                    modifier = Modifier
                        .size(230.dp)
                        .padding(14.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = workplace.qrCodeValue,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(14.dp))

            WorkplaceInfoRow("Workplace ID", workplace.workplaceId)
            WorkplaceInfoRow("Allowed Radius", "${workplace.allowedRadius} m")
            WorkplaceInfoRow("Start Time", workplace.startTime)
            WorkplaceInfoRow("Late After", workplace.lateAfterTime)

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Print or screenshot this QR and place it at the workplace.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun EmptyWorkplaceCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Text(
            text = "No workplace yet. Save workplace settings to generate the permanent QR code.",
            modifier = Modifier.padding(18.dp),
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun WorkplaceInfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp
        )

        Text(
            text = value,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun InputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.padding(top = 14.dp),
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun ErrorBox(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.errorContainer
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(14.dp),
            color = MaterialTheme.colorScheme.onErrorContainer
        )
    }
}

@Composable
private fun SuccessBox(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(14.dp),
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}