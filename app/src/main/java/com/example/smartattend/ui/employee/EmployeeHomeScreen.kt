package com.example.smartattend.ui.employee

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.smartattend.viewmodel.EmployeeViewModel

@Composable
fun EmployeeHomeScreen(
    viewModel: EmployeeViewModel,
    onScanClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val employee = uiState.employee

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp)
            .padding(bottom = 32.dp)
    ) {
        when {
            uiState.isLoading -> {
                LoadingBox()
            }

            uiState.errorMessage != null -> {
                ErrorCard(message = uiState.errorMessage ?: "Unknown error")
            }

            employee != null -> {
                EmployeeHomeContent(
                    employee = employee,
                    onScanClick = onScanClick
                )
            }
        }
    }
}

@Composable
private fun EmployeeHomeContent(
    employee: Employee,
    onScanClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        EmployeeAvatar(
            photoUrl = employee.photoUrl,
            size = 56
        )

        Column(
            modifier = Modifier.padding(start = 12.dp)
        ) {
            Text(
                text = "Hello, ${employee.fullName}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = employee.position.ifBlank { "Employee" },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
        }
    }

    Spacer(modifier = Modifier.height(26.dp))

    Text(
        text = "Home",
        fontSize = 30.sp,
        fontWeight = FontWeight.Bold
    )

    Text(
        text = "Your attendance and salary overview.",
        modifier = Modifier.padding(top = 6.dp),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 14.sp
    )

    Spacer(modifier = Modifier.height(22.dp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Column(
            modifier = Modifier.padding(22.dp)
        ) {
            Text(
                text = "Today Attendance",
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.86f),
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Ready to Check In",
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Tap the button below to open QR scanner.",
                modifier = Modifier.padding(top = 8.dp),
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.86f)
            )

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = onScanClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "Scan QR to Check In",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(18.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SmallInfoCard(
            title = "Employee ID",
            value = employee.employeeId,
            emoji = "🪪",
            modifier = Modifier.weight(1f)
        )

        SmallInfoCard(
            title = "Department",
            value = employee.departmentName.ifBlank { "-" },
            emoji = "🏢",
            modifier = Modifier.weight(1f)
        )
    }

    Spacer(modifier = Modifier.height(18.dp))

    SalaryPreviewCard(employee = employee)
}

@Composable
private fun SalaryPreviewCard(
    employee: Employee
) {
    val dailySalary = if (employee.workDaysPerMonth > 0) {
        employee.baseSalary / employee.workDaysPerMonth
    } else {
        0.0
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = "Salary Preview",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            ProfileInfoRow("Base Salary", "$${formatMoney(employee.baseSalary)}")
            ProfileInfoRow("Work Days", employee.workDaysPerMonth.toString())
            ProfileInfoRow("Daily Salary", "$${formatMoney(dailySalary)}")

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Full salary calculation uses attendance records.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
fun EmployeeAvatar(
    photoUrl: String,
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
                contentDescription = "Employee Profile Photo",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "👤",
                    fontSize = (size / 2).sp
                )
            }
        }
    }
}

@Composable
private fun SmallInfoCard(
    title: String,
    value: String,
    emoji: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(132.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = emoji,
                fontSize = 28.sp
            )

            Column {
                Text(
                    text = value,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )

                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun ProfileInfoRow(
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
fun LoadingBox() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(18.dp),
            color = MaterialTheme.colorScheme.onErrorContainer
        )
    }
}

fun formatMoney(value: Double): String {
    return String.format("%.2f", value)
}