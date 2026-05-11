package com.example.smartattend.ui.employee

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ContactPhone
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.smartattend.data.model.Employee
import com.example.smartattend.viewmodel.EmployeeViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeProfileUpdateRequestScreen(
    viewModel: EmployeeViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val employee = uiState.employee

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var emergencyContact by remember { mutableStateOf("") }

    var expandedGender by remember { mutableStateOf(false) }
    var showDobPicker by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        selectedImageUri = uri
    }

    LaunchedEffect(employee?.employeeId) {
        if (employee != null) {
            fullName = employee.fullName
            phone = employee.phone
            gender = employee.gender
            dob = employee.dob
            address = employee.address
            emergencyContact = employee.emergencyContact
        }
    }

    LaunchedEffect(uiState.profileRequestSent) {
        if (uiState.profileRequestSent) {
            delay(1200)
            viewModel.resetProfileRequestState()
            onBack()
        }
    }

    if (showDobPicker) {
        AppDatePickerDialog(
            title = "Select Date of Birth",
            onDismiss = {
                showDobPicker = false
            },
            onDateSelected = { selectedDate ->
                dob = selectedDate
                showDobPicker = false
                viewModel.clearMessages()
            }
        )
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
        RequestHeader(
            onBack = onBack
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (employee == null) {
            if (uiState.isLoading) {
                LoadingRequestCard()
            } else {
                ErrorRequestBox(
                    message = uiState.errorMessage ?: "Employee profile not loaded"
                )
            }
            return@Column
        }

        RequestInfoCard()

        Spacer(modifier = Modifier.height(16.dp))

        UpdateFormCard(
            employee = employee,
            selectedImageUri = selectedImageUri,
            onPickImage = {
                imagePickerLauncher.launch("image/*")
            },
            fullName = fullName,
            onFullNameChange = {
                fullName = it
                viewModel.clearMessages()
            },
            phone = phone,
            onPhoneChange = {
                phone = it
                viewModel.clearMessages()
            },
            gender = gender,
            expandedGender = expandedGender,
            onExpandedGenderChange = {
                expandedGender = it
            },
            onGenderSelected = {
                gender = it
                expandedGender = false
                viewModel.clearMessages()
            },
            dob = dob,
            onDobClick = {
                showDobPicker = true
            },
            address = address,
            onAddressChange = {
                address = it
                viewModel.clearMessages()
            },
            emergencyContact = emergencyContact,
            onEmergencyContactChange = {
                emergencyContact = it
                viewModel.clearMessages()
            },
            isLoading = uiState.isLoading,
            errorMessage = uiState.errorMessage,
            successMessage = uiState.successMessage,
            onSubmit = {
                viewModel.requestProfileUpdate(
                    context = context,
                    fullName = fullName,
                    phone = phone,
                    gender = gender,
                    dob = dob,
                    address = address,
                    emergencyContact = emergencyContact,
                    imageUri = selectedImageUri
                )
            }
        )
    }
}

@Composable
private fun RequestHeader(
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            modifier = Modifier.size(54.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.Edit,
                    contentDescription = "Request Update",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Request Update",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Send profile changes to HR for approval.",
                modifier = Modifier.padding(top = 4.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
        }

        TextButton(
            onClick = onBack
        ) {
            Text("Back")
        }
    }
}

@Composable
private fun RequestInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.VerifiedUser,
                        contentDescription = "Approval",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(23.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "HR Approval Required",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Text(
                    text = "Your profile will not change immediately. HR must approve your request first.",
                    modifier = Modifier.padding(top = 5.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.82f),
                    fontSize = 13.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UpdateFormCard(
    employee: Employee,
    selectedImageUri: Uri?,
    onPickImage: () -> Unit,

    fullName: String,
    onFullNameChange: (String) -> Unit,

    phone: String,
    onPhoneChange: (String) -> Unit,

    gender: String,
    expandedGender: Boolean,
    onExpandedGenderChange: (Boolean) -> Unit,
    onGenderSelected: (String) -> Unit,

    dob: String,
    onDobClick: () -> Unit,

    address: String,
    onAddressChange: (String) -> Unit,

    emergencyContact: String,
    onEmergencyContactChange: (String) -> Unit,

    isLoading: Boolean,
    errorMessage: String?,
    successMessage: String?,
    onSubmit: () -> Unit
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
            modifier = Modifier.padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ProfileImagePicker(
                employee = employee,
                selectedImageUri = selectedImageUri,
                onPickImage = onPickImage
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Tap photo to request a new profile image",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            SectionHeader(
                icon = Icons.Rounded.AccountCircle,
                title = "Profile Information",
                subtitle = "Edit the information you want HR to review."
            )

            Spacer(modifier = Modifier.height(4.dp))

            InputField(
                value = fullName,
                onValueChange = onFullNameChange,
                label = "Full Name *",
                icon = Icons.Rounded.Person
            )

            InputField(
                value = phone,
                onValueChange = onPhoneChange,
                label = "Phone",
                icon = Icons.Rounded.Phone,
                keyboardType = KeyboardType.Phone
            )

            SimpleDropdownField(
                label = "Gender",
                value = gender,
                expanded = expandedGender,
                options = listOf("Male", "Female"),
                icon = Icons.Rounded.Person,
                onExpandedChange = onExpandedGenderChange,
                onSelected = onGenderSelected
            )

            ReadOnlyDateField(
                value = dob,
                label = "Date of Birth",
                icon = Icons.Rounded.CalendarMonth,
                onClick = onDobClick
            )

            InputField(
                value = address,
                onValueChange = onAddressChange,
                label = "Address",
                icon = Icons.Rounded.Home
            )

            InputField(
                value = emergencyContact,
                onValueChange = onEmergencyContactChange,
                label = "Emergency Contact",
                icon = Icons.Rounded.ContactPhone,
                keyboardType = KeyboardType.Phone
            )

            errorMessage?.let {
                Spacer(modifier = Modifier.height(16.dp))
                ErrorRequestBox(message = it)
            }

            successMessage?.let {
                Spacer(modifier = Modifier.height(16.dp))
                SuccessRequestBox(message = it)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onSubmit,
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
                        imageVector = Icons.Rounded.Send,
                        contentDescription = "Send Request"
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Send Request to HR",
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
        modifier = Modifier.fillMaxWidth(),
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
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = subtitle,
                modifier = Modifier.padding(top = 3.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun ProfileImagePicker(
    employee: Employee,
    selectedImageUri: Uri?,
    onPickImage: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(116.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                shape = CircleShape
            )
            .clickable {
                onPickImage()
            },
        contentAlignment = Alignment.Center
    ) {
        when {
            selectedImageUri != null -> {
                AsyncImage(
                    model = selectedImageUri,
                    contentDescription = "Selected Profile Photo",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            employee.photoUrl.isNotBlank() -> {
                AsyncImage(
                    model = employee.photoUrl,
                    contentDescription = "Current Profile Photo",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            else -> {
                Icon(
                    imageVector = Icons.Rounded.Person,
                    contentDescription = "Profile Photo",
                    modifier = Modifier.size(54.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(38.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "+",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun InputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 14.dp),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SimpleDropdownField(
    label: String,
    value: String,
    expanded: Boolean,
    options: List<String>,
    icon: ImageVector,
    onExpandedChange: (Boolean) -> Unit,
    onSelected: (String) -> Unit
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChange,
        modifier = Modifier.padding(top = 14.dp)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            label = {
                Text(label)
            },
            leadingIcon = {
                Icon(
                    imageVector = icon,
                    contentDescription = label
                )
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            shape = RoundedCornerShape(16.dp)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                onExpandedChange(false)
            }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(option)
                    },
                    onClick = {
                        onSelected(option)
                    }
                )
            }
        }
    }
}

@Composable
private fun ReadOnlyDateField(
    value: String,
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 14.dp)
            .clickable {
                onClick()
            }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text(label)
            },
            leadingIcon = {
                Icon(
                    imageVector = icon,
                    contentDescription = label
                )
            },
            readOnly = true,
            enabled = false,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledContainerColor = MaterialTheme.colorScheme.surface,
                disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppDatePickerDialog(
    title: String,
    onDismiss: () -> Unit,
    onDateSelected: (String) -> Unit
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val millis = datePickerState.selectedDateMillis
                    if (millis != null) {
                        onDateSelected(formatDate(millis))
                    }
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    ) {
        Column {
            Text(
                text = title,
                modifier = Modifier.padding(start = 24.dp, top = 20.dp),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            DatePicker(state = datePickerState)
        }
    }
}

private fun formatDate(millis: Long): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return formatter.format(Date(millis))
}

@Composable
private fun LoadingRequestCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(30.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun SuccessRequestBox(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = "Success",
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = message,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun ErrorRequestBox(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.errorContainer
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.ErrorOutline,
                contentDescription = "Error",
                tint = MaterialTheme.colorScheme.onErrorContainer
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = message,
                color = MaterialTheme.colorScheme.onErrorContainer,
                fontSize = 14.sp
            )
        }
    }
}