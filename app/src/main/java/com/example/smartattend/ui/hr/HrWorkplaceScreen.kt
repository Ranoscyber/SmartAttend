package com.example.smartattend.ui.hr

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Domain
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
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
            .padding(horizontal = 20.dp, vertical = 18.dp)
            .padding(bottom = 32.dp)
    ) {
        WorkHeader()

        Spacer(modifier = Modifier.height(22.dp))

        WorkplaceStatusCard(workplace = workplace)

        Spacer(modifier = Modifier.height(18.dp))

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

        Spacer(modifier = Modifier.height(18.dp))

        if (workplace != null) {
            WorkplaceQrCard(workplace = workplace)
        } else {
            EmptyWorkplaceCard()
        }
    }
}

@Composable
private fun WorkHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(54.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.Domain,
                    contentDescription = "Workplace",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Column(
            modifier = Modifier.padding(start = 12.dp)
        ) {
            Text(
                text = "Workplace",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Office location and QR check-in setup.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun WorkplaceStatusCard(
    workplace: Workplace?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (workplace == null) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.primary
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(50.dp),
                shape = CircleShape,
                color = if (workplace == null) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.16f)
                }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (workplace == null) {
                            Icons.Rounded.LocationOn
                        } else {
                            Icons.Rounded.QrCode2
                        },
                        contentDescription = "Workplace Status",
                        tint = if (workplace == null) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onPrimary
                        },
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = if (workplace == null) {
                        "No workplace configured"
                    } else {
                        workplace.name
                    },
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (workplace == null) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onPrimary
                    }
                )

                Text(
                    text = if (workplace == null) {
                        "Save workplace settings to generate a QR code."
                    } else {
                        "Permanent QR is ready for employee check-in."
                    },
                    modifier = Modifier.padding(top = 5.dp),
                    color = if (workplace == null) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.82f)
                    },
                    fontSize = 13.sp
                )
            }
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
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(22.dp)
        ) {
            SectionHeader(
                icon = Icons.Rounded.MyLocation,
                title = "Workplace Setting",
                subtitle = "Set office location, radius, and attendance time."
            )

            Spacer(modifier = Modifier.height(18.dp))

            InputField(
                value = name,
                onValueChange = onNameChange,
                label = "Workplace Name *",
                icon = Icons.Rounded.Domain
            )

            InputField(
                value = latitude,
                onValueChange = onLatitudeChange,
                label = "Latitude *",
                icon = Icons.Rounded.LocationOn,
                keyboardType = KeyboardType.Number
            )

            InputField(
                value = longitude,
                onValueChange = onLongitudeChange,
                label = "Longitude *",
                icon = Icons.Rounded.LocationOn,
                keyboardType = KeyboardType.Number
            )

            InputField(
                value = allowedRadius,
                onValueChange = onAllowedRadiusChange,
                label = "Allowed Radius Meter *",
                icon = Icons.Rounded.Security,
                keyboardType = KeyboardType.Number
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InputField(
                    value = startTime,
                    onValueChange = onStartTimeChange,
                    label = "Start Time",
                    icon = Icons.Rounded.Schedule,
                    modifier = Modifier.weight(1f)
                )

                InputField(
                    value = lateAfterTime,
                    onValueChange = onLateAfterTimeChange,
                    label = "Late After",
                    icon = Icons.Rounded.AccessTime,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            TipBox(
                text = "Tip: Open Google Maps, long press your workplace, then copy latitude and longitude."
            )

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
                    Icon(
                        imageVector = Icons.Rounded.Save,
                        contentDescription = "Save"
                    )

                    Spacer(modifier = Modifier.width(8.dp))

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
private fun SectionHeader(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(44.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = subtitle,
                modifier = Modifier.padding(top = 4.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )
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
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SectionHeader(
                icon = Icons.Rounded.QrCode2,
                title = "Permanent QR Code",
                subtitle = "Place this QR at the workplace for check-in."
            )

            Spacer(modifier = Modifier.height(22.dp))

            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 2.dp
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

            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = workplace.qrCodeValue,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            WorkplaceInfoRow("Workplace ID", workplace.workplaceId)
            WorkplaceInfoRow("Allowed Radius", "${workplace.allowedRadius} m")
            WorkplaceInfoRow("Start Time", workplace.startTime)
            WorkplaceInfoRow("Late After", workplace.lateAfterTime)

            Spacer(modifier = Modifier.height(14.dp))

            TipBox(
                text = "Print or screenshot this QR code and place it near the workplace entrance."
            )
        }
    }
}

@Composable
private fun EmptyWorkplaceCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.QrCode2,
                        contentDescription = "No QR",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "QR code not available",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Save workplace settings to generate QR code.",
                    modifier = Modifier.padding(top = 4.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            }
        }
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
            color = MaterialTheme.colorScheme.onSurface,
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
    icon: ImageVector,
    modifier: Modifier = Modifier.fillMaxWidth(),
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.padding(top = 14.dp),
        label = {
            Text(label)
        },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = label
            )
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun TipBox(
    text: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Rounded.Info,
                contentDescription = "Info",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = text,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontSize = 13.sp
            )
        }
    }
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