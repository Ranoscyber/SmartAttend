package com.example.smartattend.ui.employee

import com.example.smartattend.ui.common.AppearanceSettingsCard
import com.example.smartattend.viewmodel.AppSettingsViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartattend.data.model.Employee
import com.example.smartattend.viewmodel.EmployeeViewModel

@Composable
fun EmployeeProfileScreen(
    viewModel: EmployeeViewModel,
    appSettingsViewModel: AppSettingsViewModel,
    onRequestUpdateClick: () -> Unit,
    onLogoutConfirmed: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val employee = uiState.employee

    var showLogoutDialog by remember { mutableStateOf(false) }

    val isDarkMode by appSettingsViewModel.isDarkMode.collectAsState()

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
            text = "Profile",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "View and manage your employee information.",
            modifier = Modifier.padding(top = 6.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        when {
            uiState.isLoading -> {
                LoadingBox()
            }

            uiState.errorMessage != null -> {
                ErrorCard(
                    message = uiState.errorMessage ?: "Unknown error"
                )
            }

            employee != null -> {
                EmployeeProfileContent(employee = employee)

                Spacer(modifier = Modifier.height(18.dp))

                ProfileActionCard(
                    onRequestUpdateClick = onRequestUpdateClick
                )

                Spacer(modifier = Modifier.height(18.dp))

                AppearanceSettingsCard(
                    isDarkMode = isDarkMode,
                    onDarkModeChange = { enabled ->
                        appSettingsViewModel.setDarkMode(enabled)
                    }
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        showLogoutDialog = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Logout,
                        contentDescription = "Logout"
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Logout",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = {
                showLogoutDialog = false
            },
            title = {
                Text(
                    text = "Logout?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to logout from SmartAttend?",
                    textAlign = TextAlign.Start
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onLogoutConfirmed()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Logout")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun EmployeeProfileContent(
    employee: Employee
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            EmployeeAvatar(
                photoUrl = employee.photoUrl,
                size = 96
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = employee.fullName,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = employee.employeeId,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(22.dp))

            ProfileInfoRow("Email", employee.email)
            ProfileInfoRow("Phone", employee.phone.ifBlank { "-" })
            ProfileInfoRow("Gender", employee.gender.ifBlank { "-" })
            ProfileInfoRow("Date of Birth", employee.dob.ifBlank { "-" })
            ProfileInfoRow("Address", employee.address.ifBlank { "-" })
            ProfileInfoRow("Emergency Contact", employee.emergencyContact.ifBlank { "-" })
        }
    }

    Spacer(modifier = Modifier.height(18.dp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = "Job Information",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            ProfileInfoRow("Department", employee.departmentName.ifBlank { "-" })
            ProfileInfoRow("Position", employee.position.ifBlank { "-" })
            ProfileInfoRow("Employment Type", employee.employmentType.ifBlank { "-" })
            ProfileInfoRow("Join Date", employee.joinDate.ifBlank { "-" })
            ProfileInfoRow("Status", employee.status.ifBlank { "-" })
        }
    }
}

@Composable
private fun ProfileActionCard(
    onRequestUpdateClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = "Account Actions",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Request changes to your profile information.",
                modifier = Modifier.padding(top = 6.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onRequestUpdateClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Edit,
                    contentDescription = "Request Update"
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Request Profile Update",
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = {
                    // Later: connect to Change Password screen if you want
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(18.dp),
                enabled = false
            ) {
                Icon(
                    imageVector = Icons.Rounded.Lock,
                    contentDescription = "Change Password"
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Change Password"
                )
            }
        }
    }
}