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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.Business
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.ContactPhone
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.GroupAdd
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.Work
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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
    var passwordVisible by remember { mutableStateOf(false) }

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

    fun clearForm() {
        selectedImageUri = null
        fullName = ""
        email = ""
        password = ""
        passwordVisible = false

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
    }

    LaunchedEffect(Unit) {
        viewModel.loadHrData()
    }

    LaunchedEffect(uiState.employeeCreated) {
        if (uiState.employeeCreated) {
            val message = uiState.successMessage ?: "Employee created successfully"

            snackbarHostState.showSnackbar(message)
            delay(700)

            clearForm()
            viewModel.resetEmployeeCreatedState()
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

    if (showJoinDatePicker) {
        AppDatePickerDialog(
            title = "Select Join Date",
            onDismiss = {
                showJoinDatePicker = false
            },
            onDateSelected = { selectedDate ->
                joinDate = selectedDate
                showJoinDatePicker = false
                viewModel.clearMessages()
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 18.dp)
                .padding(bottom = 96.dp)
        ) {
            StaffHeader()

            Spacer(modifier = Modifier.height(20.dp))

            if (uiState.departments.isEmpty()) {
                NoDepartmentCard()
                return@Column
            }

            CreateEmployeeFormCard(
                uiState = uiState,
                selectedImageUri = selectedImageUri,
                onPickImage = {
                    imagePickerLauncher.launch("image/*")
                },
                fullName = fullName,
                onFullNameChange = {
                    fullName = it
                    viewModel.clearMessages()
                },
                email = email,
                onEmailChange = {
                    email = it
                    viewModel.clearMessages()
                },
                password = password,
                onPasswordChange = {
                    password = it
                    viewModel.clearMessages()
                },
                passwordVisible = passwordVisible,
                onPasswordVisibilityChange = {
                    passwordVisible = !passwordVisible
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
                selectedDepartment = selectedDepartment,
                expandedDepartment = expandedDepartment,
                onExpandedDepartmentChange = {
                    expandedDepartment = it
                },
                onDepartmentSelected = {
                    selectedDepartment = it
                    expandedDepartment = false
                    viewModel.clearMessages()
                },
                position = position,
                onPositionChange = {
                    position = it
                    viewModel.clearMessages()
                },
                employmentType = employmentType,
                expandedEmploymentType = expandedEmploymentType,
                onExpandedEmploymentTypeChange = {
                    expandedEmploymentType = it
                },
                onEmploymentTypeSelected = {
                    employmentType = it
                    expandedEmploymentType = false
                    viewModel.clearMessages()
                },
                joinDate = joinDate,
                onJoinDateClick = {
                    showJoinDatePicker = true
                },
                baseSalary = baseSalary,
                onBaseSalaryChange = {
                    baseSalary = it
                    viewModel.clearMessages()
                },
                workDays = workDays,
                onWorkDaysChange = {
                    workDays = it
                    viewModel.clearMessages()
                },
                onSubmit = {
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
                }
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
        )
    }
}

@Composable
private fun StaffHeader() {
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
                    imageVector = Icons.Rounded.Groups,
                    contentDescription = "Staff",
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
                text = "Staff",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Create employee account and assign department.",
                modifier = Modifier.padding(top = 4.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateEmployeeFormCard(
    uiState: com.example.smartattend.viewmodel.HrUiState,
    selectedImageUri: Uri?,
    onPickImage: () -> Unit,

    fullName: String,
    onFullNameChange: (String) -> Unit,

    email: String,
    onEmailChange: (String) -> Unit,

    password: String,
    onPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onPasswordVisibilityChange: () -> Unit,

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

    selectedDepartment: Department?,
    expandedDepartment: Boolean,
    onExpandedDepartmentChange: (Boolean) -> Unit,
    onDepartmentSelected: (Department) -> Unit,

    position: String,
    onPositionChange: (String) -> Unit,

    employmentType: String,
    expandedEmploymentType: Boolean,
    onExpandedEmploymentTypeChange: (Boolean) -> Unit,
    onEmploymentTypeSelected: (String) -> Unit,

    joinDate: String,
    onJoinDateClick: () -> Unit,

    baseSalary: String,
    onBaseSalaryChange: (String) -> Unit,

    workDays: String,
    onWorkDaysChange: (String) -> Unit,

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
            EmployeeImagePicker(
                imageUri = selectedImageUri,
                onPickImage = onPickImage
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Optional profile photo",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            SectionHeader(
                icon = Icons.Rounded.GroupAdd,
                title = "Create Staff Account",
                subtitle = "Account credentials for employee login."
            )

            Spacer(modifier = Modifier.height(4.dp))

            InputField(
                value = fullName,
                onValueChange = onFullNameChange,
                label = "Full Name *",
                icon = Icons.Rounded.Person
            )

            InputField(
                value = email,
                onValueChange = onEmailChange,
                label = "Email *",
                icon = Icons.Rounded.Email,
                keyboardType = KeyboardType.Email
            )

            PasswordField(
                value = password,
                onValueChange = onPasswordChange,
                passwordVisible = passwordVisible,
                onPasswordVisibilityChange = onPasswordVisibilityChange
            )

            Spacer(modifier = Modifier.height(24.dp))

            SectionHeader(
                icon = Icons.Rounded.Work,
                title = "Job Information",
                subtitle = "Department, position, and employment details."
            )

            Spacer(modifier = Modifier.height(4.dp))

            ExposedDropdownMenuBox(
                expanded = expandedDepartment,
                onExpandedChange = {
                    onExpandedDepartmentChange(!expandedDepartment)
                },
                modifier = Modifier.padding(top = 14.dp)
            ) {
                OutlinedTextField(
                    value = selectedDepartment?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    label = {
                        Text("Department *")
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Business,
                            contentDescription = "Department"
                        )
                    },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = expandedDepartment
                        )
                    },
                    shape = RoundedCornerShape(16.dp)
                )

                ExposedDropdownMenu(
                    expanded = expandedDepartment,
                    onDismissRequest = {
                        onExpandedDepartmentChange(false)
                    }
                ) {
                    uiState.departments.forEach { department ->
                        DropdownMenuItem(
                            text = {
                                Text(department.name)
                            },
                            onClick = {
                                onDepartmentSelected(department)
                            }
                        )
                    }
                }
            }

            InputField(
                value = position,
                onValueChange = onPositionChange,
                label = "Position *",
                icon = Icons.Rounded.Badge
            )

            SimpleDropdownField(
                label = "Employment Type Optional",
                value = employmentType,
                expanded = expandedEmploymentType,
                options = listOf("Full-time", "Part-time"),
                icon = Icons.Rounded.Work,
                onExpandedChange = onExpandedEmploymentTypeChange,
                onSelected = onEmploymentTypeSelected
            )

            ReadOnlyDateField(
                value = joinDate,
                label = "Join Date Optional",
                icon = Icons.Rounded.CalendarMonth,
                onClick = onJoinDateClick
            )

            Spacer(modifier = Modifier.height(24.dp))

            SectionHeader(
                icon = Icons.Rounded.Badge,
                title = "Salary Information",
                subtitle = "Used for salary report calculation."
            )

            Spacer(modifier = Modifier.height(4.dp))

            InputField(
                value = baseSalary,
                onValueChange = onBaseSalaryChange,
                label = "Base Salary *",
                icon = Icons.Rounded.Badge,
                keyboardType = KeyboardType.Number
            )

            InputField(
                value = workDays,
                onValueChange = onWorkDaysChange,
                label = "Work Days Per Month *",
                icon = Icons.Rounded.CalendarMonth,
                keyboardType = KeyboardType.Number
            )

            Spacer(modifier = Modifier.height(24.dp))

            SectionHeader(
                icon = Icons.Rounded.AccountCircle,
                title = "Optional Profile",
                subtitle = "Extra employee contact information."
            )

            Spacer(modifier = Modifier.height(4.dp))

            InputField(
                value = phone,
                onValueChange = onPhoneChange,
                label = "Phone Optional",
                icon = Icons.Rounded.Phone,
                keyboardType = KeyboardType.Phone
            )

            SimpleDropdownField(
                label = "Gender Optional",
                value = gender,
                expanded = expandedGender,
                options = listOf("Male", "Female"),
                icon = Icons.Rounded.Person,
                onExpandedChange = onExpandedGenderChange,
                onSelected = onGenderSelected
            )

            ReadOnlyDateField(
                value = dob,
                label = "Date of Birth Optional",
                icon = Icons.Rounded.CalendarMonth,
                onClick = onDobClick
            )

            InputField(
                value = address,
                onValueChange = onAddressChange,
                label = "Address Optional",
                icon = Icons.Rounded.Home
            )

            InputField(
                value = emergencyContact,
                onValueChange = onEmergencyContactChange,
                label = "Emergency Contact Optional",
                icon = Icons.Rounded.ContactPhone,
                keyboardType = KeyboardType.Phone
            )

            uiState.errorMessage?.let {
                Spacer(modifier = Modifier.height(16.dp))
                ErrorBox(message = it)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onSubmit,
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
                    Icon(
                        imageVector = Icons.Rounded.Save,
                        contentDescription = "Create"
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Create Staff Account",
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

@Composable
private fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    passwordVisible: Boolean,
    onPasswordVisibilityChange: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 14.dp),
        label = {
            Text("Temporary Password *")
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Rounded.Lock,
                contentDescription = "Password"
            )
        },
        trailingIcon = {
            IconButton(
                onClick = onPasswordVisibilityChange
            ) {
                Icon(
                    imageVector = if (passwordVisible) {
                        Icons.Rounded.VisibilityOff
                    } else {
                        Icons.Rounded.Visibility
                    },
                    contentDescription = "Toggle Password"
                )
            }
        },
        singleLine = true,
        visualTransformation = if (passwordVisible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
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
            .clickable {
                onPickImage()
            },
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
            Icon(
                imageVector = Icons.Rounded.Person,
                contentDescription = "Employee Photo",
                modifier = Modifier.size(54.dp),
                tint = MaterialTheme.colorScheme.primary
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
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(22.dp)
        ) {
            Text(
                text = "No department found",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Please create a department first before adding staff.",
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