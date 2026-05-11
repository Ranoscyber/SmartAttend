package com.example.smartattend.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.Business
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
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
    startWithEmployees: Boolean = false,
    onAddHrClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    var selectedTab by remember(startWithEmployees) {
        mutableStateOf(
            if (startWithEmployees) AdminPeopleTab.EMPLOYEE else AdminPeopleTab.HR
        )
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedHr by remember { mutableStateOf<HRProfile?>(null) }
    var selectedEmployee by remember { mutableStateOf<Employee?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadPeopleData()
    }

    val filteredHr = uiState.hrProfiles.filter { hr ->
        searchQuery.isBlank() ||
                hr.fullName.contains(searchQuery, ignoreCase = true) ||
                hr.email.contains(searchQuery, ignoreCase = true) ||
                hr.phone.contains(searchQuery, ignoreCase = true)
    }

    val filteredEmployees = uiState.employeeProfiles.filter { employee ->
        searchQuery.isBlank() ||
                employee.fullName.contains(searchQuery, ignoreCase = true) ||
                employee.email.contains(searchQuery, ignoreCase = true) ||
                employee.employeeId.contains(searchQuery, ignoreCase = true) ||
                employee.departmentName.contains(searchQuery, ignoreCase = true) ||
                employee.position.contains(searchQuery, ignoreCase = true)
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
        Text(
            text = "People Management",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = "View HR accounts and employee profiles.",
            modifier = Modifier.padding(top = 6.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        PeopleSwitchCard(
            selectedTab = selectedTab,
            hrCount = uiState.hrProfiles.size,
            employeeCount = uiState.employeeProfiles.size,
            onTabSelected = {
                selectedTab = it
                searchQuery = ""
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        SearchBox(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
            },
            placeholder = if (selectedTab == AdminPeopleTab.HR) {
                "Search HR by name, email, phone"
            } else {
                "Search employee by name, ID, department"
            }
        )

        Spacer(modifier = Modifier.height(18.dp))

        if (selectedTab == AdminPeopleTab.HR) {
            CreateHrMiniCard(onAddHrClick = onAddHrClick)

            Spacer(modifier = Modifier.height(18.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (selectedTab == AdminPeopleTab.HR) "HR List" else "Employee List",
                modifier = Modifier.weight(1f),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            TextButton(
                onClick = {
                    viewModel.loadPeopleData()
                }
            ) {
                Text("Refresh")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

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
                if (filteredHr.isEmpty()) {
                    EmptyPeopleCard(
                        title = "No HR accounts found",
                        subtitle = "Create or search another HR account."
                    )
                } else {
                    filteredHr.forEach { hr ->
                        HrCompactCard(
                            hr = hr,
                            onClick = {
                                selectedHr = hr
                            }
                        )
                    }
                }
            }

            selectedTab == AdminPeopleTab.EMPLOYEE -> {
                if (filteredEmployees.isEmpty()) {
                    EmptyPeopleCard(
                        title = "No employees found",
                        subtitle = "Employees created by HR will show here."
                    )
                } else {
                    EmployeeGroupedByDepartment(
                        employees = filteredEmployees,
                        onEmployeeClick = {
                            selectedEmployee = it
                        }
                    )
                }
            }
        }
    }

    selectedHr?.let { hr ->
        HrDetailDialog(
            hr = hr,
            onDismiss = {
                selectedHr = null
            }
        )
    }

    selectedEmployee?.let { employee ->
        EmployeeDetailDialog(
            employee = employee,
            onDismiss = {
                selectedEmployee = null
            }
        )
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
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "View Accounts",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Switch between HR and employees.",
                modifier = Modifier.padding(top = 4.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
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
                        Text("Staff ($employeeCount)")
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Work,
                            contentDescription = "Staff"
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SearchBox(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        leadingIcon = {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = "Search"
            )
        },
        placeholder = {
            Text(placeholder)
        },
        singleLine = true,
        shape = RoundedCornerShape(18.dp)
    )
}

@Composable
private fun CreateHrMiniCard(
    onAddHrClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.PersonAdd,
                        contentDescription = "Create HR",
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
                    text = "Create HR Account",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Text(
                    text = "Add HR with secure access.",
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f),
                    fontSize = 13.sp
                )
            }

            Button(
                onClick = onAddHrClick,
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Create")
            }
        }
    }
}

@Composable
private fun HrCompactCard(
    hr: HRProfile,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PeopleAvatar(
                photoUrl = hr.photoUrl,
                fallbackText = hr.fullName.firstOrNull()?.uppercase() ?: "H",
                size = 48
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = hr.fullName.ifBlank { "Unnamed HR" },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )

                Text(
                    text = hr.email.ifBlank { "-" },
                    modifier = Modifier.padding(top = 2.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    maxLines = 1
                )
            }

            AssistChip(
                onClick = {},
                label = {
                    Text(
                        text = hr.status.ifBlank { "active" },
                        fontSize = 12.sp
                    )
                }
            )
        }
    }
}

@Composable
private fun EmployeeGroupedByDepartment(
    employees: List<Employee>,
    onEmployeeClick: (Employee) -> Unit
) {
    val groupedEmployees = employees
        .groupBy { employee ->
            employee.departmentName.ifBlank { "No Department" }
        }
        .toSortedMap()

    groupedEmployees.forEach { (departmentName, departmentEmployees) ->
        DepartmentHeader(
            departmentName = departmentName,
            count = departmentEmployees.size
        )

        Spacer(modifier = Modifier.height(8.dp))

        departmentEmployees.forEach { employee ->
            EmployeeCompactCard(
                employee = employee,
                onClick = {
                    onEmployeeClick(employee)
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun DepartmentHeader(
    departmentName: String,
    count: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Rounded.Business,
            contentDescription = "Department",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = departmentName,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = "$count",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun EmployeeCompactCard(
    employee: Employee,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PeopleAvatar(
                photoUrl = employee.photoUrl,
                fallbackText = employee.fullName.firstOrNull()?.uppercase() ?: "E",
                size = 48
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = employee.fullName.ifBlank { "Unnamed Employee" },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )

                Text(
                    text = "${employee.employeeId.ifBlank { "-" }} • ${employee.position.ifBlank { "-" }}",
                    modifier = Modifier.padding(top = 2.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    maxLines = 1
                )
            }

            AssistChip(
                onClick = {},
                label = {
                    Text(
                        text = employee.status.ifBlank { "active" },
                        fontSize = 12.sp
                    )
                }
            )
        }
    }
}

@Composable
private fun PeopleAvatar(
    photoUrl: String,
    fallbackText: String,
    size: Int
) {
    Surface(
        modifier = Modifier.size(size.dp),
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
                    fontSize = (size / 2.4).sp
                )
            }
        }
    }
}

@Composable
private fun HrDetailDialog(
    hr: HRProfile,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        DetailSurface {
            DetailHeader(
                photoUrl = hr.photoUrl,
                fallbackText = hr.fullName.firstOrNull()?.uppercase() ?: "H",
                title = hr.fullName.ifBlank { "Unnamed HR" },
                subtitle = "HR Account",
                status = hr.status.ifBlank { "active" },
                onDismiss = onDismiss
            )

            Spacer(modifier = Modifier.height(18.dp))

            DetailRow("Email", hr.email.ifBlank { "-" }, Icons.Rounded.Email)
            DetailRow("Phone", hr.phone.ifBlank { "-" }, Icons.Rounded.Phone)
            DetailRow("Gender", hr.gender.ifBlank { "-" }, Icons.Rounded.Person)
            DetailRow("Address", hr.address.ifBlank { "-" }, Icons.Rounded.Business)
        }
    }
}

@Composable
private fun EmployeeDetailDialog(
    employee: Employee,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        DetailSurface {
            DetailHeader(
                photoUrl = employee.photoUrl,
                fallbackText = employee.fullName.firstOrNull()?.uppercase() ?: "E",
                title = employee.fullName.ifBlank { "Unnamed Employee" },
                subtitle = employee.employeeId.ifBlank { "Employee" },
                status = employee.status.ifBlank { "active" },
                onDismiss = onDismiss
            )

            Spacer(modifier = Modifier.height(18.dp))

            DetailRow("Email", employee.email.ifBlank { "-" }, Icons.Rounded.Email)
            DetailRow("Phone", employee.phone.ifBlank { "-" }, Icons.Rounded.Phone)
            DetailRow("Department", employee.departmentName.ifBlank { "-" }, Icons.Rounded.Business)
            DetailRow("Position", employee.position.ifBlank { "-" }, Icons.Rounded.Work)
            DetailRow("Employment Type", employee.employmentType.ifBlank { "-" }, Icons.Rounded.Badge)
            DetailRow("Join Date", employee.joinDate.ifBlank { "-" }, Icons.Rounded.Badge)

            Divider(modifier = Modifier.padding(vertical = 14.dp))

            DetailTextRow("Base Salary", "$${formatMoney(employee.baseSalary)}")
            DetailTextRow("Work Days", employee.workDaysPerMonth.toString())
            DetailTextRow("Gender", employee.gender.ifBlank { "-" })
            DetailTextRow("Date of Birth", employee.dob.ifBlank { "-" })
            DetailTextRow("Address", employee.address.ifBlank { "-" })
            DetailTextRow("Emergency Contact", employee.emergencyContact.ifBlank { "-" })
        }
    }
}

@Composable
private fun DetailSurface(
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(22.dp),
            content = content
        )
    }
}

@Composable
private fun DetailHeader(
    photoUrl: String,
    fallbackText: String,
    title: String,
    subtitle: String,
    status: String,
    onDismiss: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        PeopleAvatar(
            photoUrl = photoUrl,
            fallbackText = fallbackText,
            size = 64
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )

            Text(
                text = subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                maxLines = 1
            )

            AssistChip(
                onClick = {},
                label = {
                    Text(status)
                },
                modifier = Modifier.padding(top = 6.dp)
            )
        }

        IconButton(
            onClick = onDismiss
        ) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = "Close"
            )
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )

            Text(
                text = value,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
private fun DetailTextRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp
        )

        Text(
            text = value,
            modifier = Modifier.weight(1.2f),
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun LoadingPeopleCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp)
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
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
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