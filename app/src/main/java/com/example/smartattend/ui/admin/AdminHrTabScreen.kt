package com.example.smartattend.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.smartattend.data.model.Employee
import com.example.smartattend.data.model.HRProfile
import com.example.smartattend.viewmodel.AdminViewModel

private enum class AdminPeopleTab {
    HR,
    EMPLOYEE
}

@Composable
fun AdminHrTabScreen(
    viewModel: AdminViewModel,
    onAddHrClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(AdminPeopleTab.HR) }

    LaunchedEffect(Unit) {
        viewModel.loadPeopleData()
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
            text = "People Management",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "View HR accounts and employee profiles.",
            modifier = Modifier.padding(top = 6.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(22.dp))

        PeopleSwitchCard(
            selectedTab = selectedTab,
            hrCount = uiState.hrProfiles.size,
            employeeCount = uiState.employeeProfiles.size,
            onTabSelected = {
                selectedTab = it
            }
        )

        Spacer(modifier = Modifier.height(18.dp))

        if (selectedTab == AdminPeopleTab.HR) {
            CreateHrCard(
                onAddHrClick = onAddHrClick
            )

            Spacer(modifier = Modifier.height(22.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (selectedTab == AdminPeopleTab.HR) {
                    "HR List"
                } else {
                    "Employee List"
                },
                modifier = Modifier.weight(1f),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            TextButton(
                onClick = {
                    viewModel.loadPeopleData()
                }
            ) {
                Text("Refresh")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        when {
            uiState.isLoading -> {
                LoadingPeopleCard()
            }

            uiState.errorMessage != null -> {
                ErrorPeopleCard(
                    message = uiState.errorMessage ?: "Failed to load data"
                )
            }

            selectedTab == AdminPeopleTab.HR -> {
                if (uiState.hrProfiles.isEmpty()) {
                    EmptyPeopleCard(
                        title = "No HR accounts yet",
                        subtitle = "Create your first HR account to show it here."
                    )
                } else {
                    uiState.hrProfiles.forEach { hr ->
                        HrListItem(hr = hr)
                    }
                }
            }

            selectedTab == AdminPeopleTab.EMPLOYEE -> {
                if (uiState.employeeProfiles.isEmpty()) {
                    EmptyPeopleCard(
                        title = "No employees yet",
                        subtitle = "Employees created by HR will show here."
                    )
                } else {
                    uiState.employeeProfiles.forEach { employee ->
                        EmployeeListItem(employee = employee)
                    }
                }
            }
        }
    }
}

@Composable
private fun PeopleSwitchCard(
    selectedTab: AdminPeopleTab,
    hrCount: Int,
    employeeCount: Int,
    onTabSelected: (AdminPeopleTab) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = "View Accounts",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Switch between HR and Employee accounts.",
                modifier = Modifier.padding(top = 4.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilterChip(
                    selected = selectedTab == AdminPeopleTab.HR,
                    onClick = {
                        onTabSelected(AdminPeopleTab.HR)
                    },
                    label = {
                        Text("HR ($hrCount)")
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Groups,
                            contentDescription = "HR"
                        )
                    },
                    modifier = Modifier.weight(1f)
                )

                FilterChip(
                    selected = selectedTab == AdminPeopleTab.EMPLOYEE,
                    onClick = {
                        onTabSelected(AdminPeopleTab.EMPLOYEE)
                    },
                    label = {
                        Text("Employees ($employeeCount)")
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Work,
                            contentDescription = "Employees"
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun CreateHrCard(
    onAddHrClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Column(
            modifier = Modifier.padding(22.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.PersonAdd,
                    contentDescription = "Create HR",
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "Create New HR",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "Add a new HR account with email and temporary password.",
                modifier = Modifier.padding(top = 8.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = onAddHrClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text(
                    text = "Create HR Account",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun HrListItem(
    hr: HRProfile
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PeopleAvatar(
                photoUrl = hr.photoUrl,
                fallbackText = hr.fullName.firstOrNull()?.uppercase() ?: "H"
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = hr.fullName.ifBlank { "Unnamed HR" },
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = hr.email.ifBlank { "-" },
                    modifier = Modifier.padding(top = 3.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )

                if (hr.phone.isNotBlank()) {
                    Text(
                        text = hr.phone,
                        modifier = Modifier.padding(top = 3.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            }

            AssistChip(
                onClick = {},
                label = {
                    Text(hr.status.ifBlank { "active" })
                }
            )
        }
    }
}

@Composable
private fun EmployeeListItem(
    employee: Employee
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PeopleAvatar(
                    photoUrl = employee.photoUrl,
                    fallbackText = employee.fullName.firstOrNull()?.uppercase() ?: "E"
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = employee.fullName.ifBlank { "Unnamed Employee" },
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = employee.email.ifBlank { "-" },
                        modifier = Modifier.padding(top = 3.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )

                    Text(
                        text = employee.employeeId.ifBlank { "-" },
                        modifier = Modifier.padding(top = 3.dp),
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                AssistChip(
                    onClick = {},
                    label = {
                        Text(employee.status.ifBlank { "active" })
                    }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            PeopleInfoRow(
                label = "Department",
                value = employee.departmentName.ifBlank { "-" }
            )

            PeopleInfoRow(
                label = "Position",
                value = employee.position.ifBlank { "-" }
            )

            PeopleInfoRow(
                label = "Employment Type",
                value = employee.employmentType.ifBlank { "-" }
            )

            PeopleInfoRow(
                label = "Base Salary",
                value = "$${formatMoney(employee.baseSalary)}"
            )
        }
    }
}

@Composable
private fun PeopleAvatar(
    photoUrl: String,
    fallbackText: String
) {
    Surface(
        modifier = Modifier.size(56.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        if (photoUrl.isNotBlank()) {
            AsyncImage(
                model = photoUrl,
                contentDescription = "Profile Photo",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = fallbackText,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
            }
        }
    }
}

@Composable
private fun PeopleInfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
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
private fun LoadingPeopleCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun EmptyPeopleCard(
    title: String,
    subtitle: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(22.dp)
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            Text(
                text = subtitle,
                modifier = Modifier.padding(top = 5.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorPeopleCard(
    message: String
) {
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

private fun formatMoney(value: Double): String {
    return String.format("%.2f", value)
}