package com.example.smartattend.ui.employee

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.Business
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.ContactPhone
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartattend.data.model.Employee
import com.example.smartattend.ui.common.AppearanceSettingsCard
import com.example.smartattend.viewmodel.AppSettingsViewModel
import com.example.smartattend.viewmodel.EmployeeViewModel

@Composable
fun EmployeeProfileScreen(
    viewModel: EmployeeViewModel,
    appSettingsViewModel: AppSettingsViewModel,
    onRequestUpdateClick: () -> Unit,
    onLogoutConfirmed: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDarkMode by appSettingsViewModel.isDarkMode.collectAsState()
    val employee = uiState.employee

    var showLogoutDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp)
            .padding(bottom = 24.dp)
    ) {
        Text(
            text = "Profile",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = "View your account information and settings.",
            modifier = Modifier.padding(top = 6.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        when {
            uiState.isLoading -> {
                LoadingProfileBox()
            }

            uiState.errorMessage != null -> {
                ProfileErrorCard(
                    message = uiState.errorMessage ?: "Failed to load profile"
                )
            }

            employee != null -> {
                EmployeeProfileHeaderCard(employee = employee)

                Spacer(modifier = Modifier.height(16.dp))

                EmployeeAccountInfoCard(employee = employee)

                Spacer(modifier = Modifier.height(16.dp))

                EmployeeJobInfoCard(employee = employee)

                Spacer(modifier = Modifier.height(16.dp))

                ProfileActionCard(
                    onRequestUpdateClick = onRequestUpdateClick
                )

                Spacer(modifier = Modifier.height(16.dp))

                AppearanceSettingsCard(
                    isDarkMode = isDarkMode,
                    onDarkModeChange = { enabled ->
                        appSettingsViewModel.setDarkMode(enabled)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                LogoutCard(
                    onLogoutClick = {
                        showLogoutDialog = true
                    }
                )
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
                    }
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
private fun EmployeeProfileHeaderCard(
    employee: Employee
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            EmployeeAvatar(
                photoUrl = employee.photoUrl,
                size = 92
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = employee.fullName.ifBlank { "Employee" },
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )

            Text(
                text = employee.employeeId.ifBlank { "-" },
                modifier = Modifier.padding(top = 4.dp),
                color = MaterialTheme.colorScheme.primary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = employee.position.ifBlank { "Employee" },
                modifier = Modifier.padding(top = 2.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun EmployeeAccountInfoCard(
    employee: Employee
) {
    InfoSectionCard(
        title = "Account Information",
        subtitle = "Basic employee profile details.",
        icon = Icons.Rounded.AccountCircle
    ) {
        IconInfoRow("Email", employee.email.ifBlank { "-" }, Icons.Rounded.Email)
        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

        IconInfoRow("Phone", employee.phone.ifBlank { "-" }, Icons.Rounded.Phone)
        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

        IconInfoRow("Gender", employee.gender.ifBlank { "-" }, Icons.Rounded.Person)
        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

        IconInfoRow("Date of Birth", employee.dob.ifBlank { "-" }, Icons.Rounded.CalendarMonth)
        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

        IconInfoRow("Address", employee.address.ifBlank { "-" }, Icons.Rounded.Home)
        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

        IconInfoRow(
            label = "Emergency Contact",
            value = employee.emergencyContact.ifBlank { "-" },
            icon = Icons.Rounded.ContactPhone
        )
    }
}

@Composable
private fun EmployeeJobInfoCard(
    employee: Employee
) {
    InfoSectionCard(
        title = "Job Information",
        subtitle = "Department and employment details.",
        icon = Icons.Rounded.Work
    ) {
        IconInfoRow("Department", employee.departmentName.ifBlank { "-" }, Icons.Rounded.Business)
        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

        IconInfoRow("Position", employee.position.ifBlank { "-" }, Icons.Rounded.Badge)
        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

        IconInfoRow("Employment Type", employee.employmentType.ifBlank { "-" }, Icons.Rounded.Work)
        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

        IconInfoRow("Join Date", employee.joinDate.ifBlank { "-" }, Icons.Rounded.CalendarMonth)
        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

        IconInfoRow("Status", employee.status.ifBlank { "active" }, Icons.Rounded.Info)
    }
}

@Composable
private fun InfoSectionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
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

                Spacer(modifier = Modifier.size(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = title,
                        fontSize = 18.sp,
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

            Spacer(modifier = Modifier.height(18.dp))

            content()
        }
    }
}

@Composable
private fun IconInfoRow(
    label: String,
    value: String,
    icon: ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(21.dp)
        )

        Spacer(modifier = Modifier.size(10.dp))

        Text(
            text = label,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp
        )

        Text(
            text = value,
            modifier = Modifier.weight(1.15f),
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun ProfileActionCard(
    onRequestUpdateClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
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
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = "Profile Actions",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.size(12.dp))

                Column {
                    Text(
                        text = "Profile Actions",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "Request HR approval for profile changes.",
                        modifier = Modifier.padding(top = 3.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onRequestUpdateClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Edit,
                    contentDescription = "Request Update"
                )

                Spacer(modifier = Modifier.size(8.dp))

                Text(
                    text = "Request Profile Update",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun LogoutCard(
    onLogoutClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Security,
                            contentDescription = "Session",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.size(12.dp))

                Column {
                    Text(
                        text = "Session",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "Logout from the current employee account.",
                        modifier = Modifier.padding(top = 3.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onLogoutClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Logout,
                    contentDescription = "Logout",
                    tint = MaterialTheme.colorScheme.error
                )

                Spacer(modifier = Modifier.size(8.dp))

                Text(
                    text = "Logout",
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun LoadingProfileBox() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ProfileErrorCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.ErrorOutline,
                contentDescription = "Error",
                tint = MaterialTheme.colorScheme.onErrorContainer
            )

            Spacer(modifier = Modifier.size(10.dp))

            Text(
                text = message,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}