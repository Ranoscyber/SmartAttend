package com.example.smartattend.ui.hr

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
import androidx.compose.material3.*
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.smartattend.data.model.Department
import com.example.smartattend.viewmodel.HrViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HrEmployeesScreen(
    viewModel: HrViewModel
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var phone by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var emergencyContact by remember { mutableStateOf("") }

    var selectedDepartment by remember { mutableStateOf<Department?>(null) }
    var position by remember { mutableStateOf("") }
    var employmentType by remember { mutableStateOf("") }
    var joinDate by remember { mutableStateOf("") }

    var baseSalary by remember { mutableStateOf("") }
    var workDays by remember { mutableStateOf("30") }

    var expandedDepartment by remember { mutableStateOf(false) }
    var expandedGender by remember { mutableStateOf(false) }
    var expandedEmploymentType by remember { mutableStateOf(false) }

    var showDobPicker by remember { mutableStateOf(false) }
    var showJoinDatePicker by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        selectedImageUri = uri
    }

    LaunchedEffect(Unit) {
        viewModel.loadHrData()
    }

    LaunchedEffect(uiState.employeeCreated) {
        if (uiState.employeeCreated) {
            val message = uiState.successMessage ?: "Employee created successfully"

            snackbarHostState.showSnackbar(message)
            delay(800)

            selectedImageUri = null

            fullName = ""
            email = ""
            password = ""

            phone = ""
            gender = ""
            dob = ""
            address = ""
            emergencyContact = ""

            selectedDepartment = null
            position = ""
            employmentType = ""
            joinDate = ""

            baseSalary = ""
            workDays = "30"

            expandedDepartment = false
            expandedGender = false
            expandedEmploymentType = false

            showDobPicker = false
            showJoinDatePicker = false

            viewModel.resetEmployeeCreatedState()
        }
    }

    if (showDobPicker) {
        AppDatePickerDialog(
            title = "Select Date of Birth",
            onDismiss = { showDobPicker = false },
            onDateSelected = { selectedDate ->
                dob = selectedDate
                showDobPicker = false
                viewModel.clearMessages()
            }
        )
    }

    if (showJoinDatePicker) {
        AppDatePickerDialog(
            title = "Select Join Date",
            onDismiss = { showJoinDatePicker = false },
            onDateSelected = { selectedDate ->
                joinDate = selectedDate
                showJoinDatePicker = false
                viewModel.clearMessages()
            }
        )
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(paddingValues)
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = "Employees",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Create employee account and assign department.",
                modifier = Modifier.padding(top = 6.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(22.dp))

            if (uiState.departments.isEmpty()) {
                NoDepartmentCard()
                return@Column
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    EmployeeImagePicker(
                        imageUri = selectedImageUri,
                        onPickImage = {
                            imagePickerLauncher.launch("image/*")
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Optional profile photo",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    FormSectionTitle("Account Information")

                    InputField(
                        value = fullName,
                        onValueChange = {
                            fullName = it
                            viewModel.clearMessages()
                        },
                        label = "Full Name *"
                    )

                    InputField(
                        value = email,
                        onValueChange = {
                            email = it
                            viewModel.clearMessages()
                        },
                        label = "Email *",
                        keyboardType = KeyboardType.Email
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            viewModel.clearMessages()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 14.dp),
                        label = { Text("Temporary Password *") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = RoundedCornerShape(16.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    FormSectionTitle("Job Information")

                    ExposedDropdownMenuBox(
                        expanded = expandedDepartment,
                        onExpandedChange = { expandedDepartment = !expandedDepartment }
                    ) {
                        OutlinedTextField(
                            value = selectedDepartment?.name ?: "",
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            label = { Text("Department *") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(
                                    expanded = expandedDepartment
                                )
                            },
                            shape = RoundedCornerShape(16.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = expandedDepartment,
                            onDismissRequest = { expandedDepartment = false }
                        ) {
                            uiState.departments.forEach { department ->
                                DropdownMenuItem(
                                    text = { Text(department.name) },
                                    onClick = {
                                        selectedDepartment = department
                                        expandedDepartment = false
                                        viewModel.clearMessages()
                                    }
                                )
                            }
                        }
                    }

                    InputField(
                        value = position,
                        onValueChange = {
                            position = it
                            viewModel.clearMessages()
                        },
                        label = "Position *"
                    )

                    SimpleDropdownField(
                        label = "Employment Type Optional",
                        value = employmentType,
                        expanded = expandedEmploymentType,
                        options = listOf("Full-time", "Part-time"),
                        onExpandedChange = { expandedEmploymentType = it },
                        onSelected = {
                            employmentType = it
                            expandedEmploymentType = false
                            viewModel.clearMessages()
                        }
                    )

                    ReadOnlyDateField(
                        value = joinDate,
                        label = "Join Date Optional",
                        onClick = {
                            showJoinDatePicker = true
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    FormSectionTitle("Salary Information")

                    InputField(
                        value = baseSalary,
                        onValueChange = {
                            baseSalary = it
                            viewModel.clearMessages()
                        },
                        label = "Base Salary *",
                        keyboardType = KeyboardType.Number
                    )

                    InputField(
                        value = workDays,
                        onValueChange = {
                            workDays = it
                            viewModel.clearMessages()
                        },
                        label = "Work Days Per Month *",
                        keyboardType = KeyboardType.Number
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    FormSectionTitle("Optional Profile")

                    InputField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = "Phone Optional",
                        keyboardType = KeyboardType.Phone
                    )

                    SimpleDropdownField(
                        label = "Gender Optional",
                        value = gender,
                        expanded = expandedGender,
                        options = listOf("Male", "Female"),
                        onExpandedChange = { expandedGender = it },
                        onSelected = {
                            gender = it
                            expandedGender = false
                            viewModel.clearMessages()
                        }
                    )

                    ReadOnlyDateField(
                        value = dob,
                        label = "Date of Birth Optional",
                        onClick = {
                            showDobPicker = true
                        }
                    )

                    InputField(
                        value = address,
                        onValueChange = { address = it },
                        label = "Address Optional"
                    )

                    InputField(
                        value = emergencyContact,
                        onValueChange = { emergencyContact = it },
                        label = "Emergency Contact Optional",
                        keyboardType = KeyboardType.Phone
                    )

                    uiState.errorMessage?.let {
                        Spacer(modifier = Modifier.height(16.dp))
                        ErrorBox(message = it)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            viewModel.createEmployee(
                                context = context,
                                fullName = fullName,
                                email = email,
                                password = password,
                                phone = phone,
                                gender = gender,
                                dob = dob,
                                address = address,
                                emergencyContact = emergencyContact,
                                department = selectedDepartment,
                                position = position,
                                employmentType = employmentType,
                                joinDate = joinDate,
                                baseSalaryText = baseSalary,
                                workDaysText = workDays,
                                imageUri = selectedImageUri
                            )
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
                                text = "Create Employee Account",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FormSectionTitle(text: String) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun InputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 14.dp),
        label = { Text(label) },
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
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            shape = RoundedCornerShape(16.dp)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = { onSelected(option) }
                )
            }
        }
    }
}

@Composable
private fun ReadOnlyDateField(
    value: String,
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 14.dp)
            .clickable { onClick() }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            label = { Text(label) },
            readOnly = true,
            enabled = false,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledContainerColor = MaterialTheme.colorScheme.surface
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
private fun EmployeeImagePicker(
    imageUri: Uri?,
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
            .clickable { onPickImage() },
        contentAlignment = Alignment.Center
    ) {
        if (imageUri != null) {
            AsyncImage(
                model = imageUri,
                contentDescription = "Selected Employee Image",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(
                text = "👤",
                fontSize = 48.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
private fun NoDepartmentCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Column(
            modifier = Modifier.padding(22.dp)
        ) {
            Text(
                text = "No department found",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Please create a department first before adding employees.",
                modifier = Modifier.padding(top = 8.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
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