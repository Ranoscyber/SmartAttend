package com.example.smartattend.ui.employee

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.EventAvailable
import androidx.compose.material.icons.rounded.MoneyOff
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.WorkHistory
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartattend.data.model.Attendance
import com.example.smartattend.data.model.Employee
import com.example.smartattend.viewmodel.AttendanceViewModel
import com.example.smartattend.viewmodel.EmployeeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun EmployeeSalaryScreen(
    employeeViewModel: EmployeeViewModel,
    attendanceViewModel: AttendanceViewModel
) {
    val employeeUiState by employeeViewModel.uiState.collectAsState()
    val attendanceUiState by attendanceViewModel.uiState.collectAsState()

    val employee = employeeUiState.employee

    LaunchedEffect(Unit) {
        employeeViewModel.loadEmployeeProfile()
        attendanceViewModel.loadAttendanceData()
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
        SalaryHeader()

        Spacer(modifier = Modifier.height(22.dp))

        when {
            employeeUiState.isLoading || attendanceUiState.isLoading -> {
                LoadingBox()
            }

            employeeUiState.errorMessage != null -> {
                ErrorCard(
                    message = employeeUiState.errorMessage ?: "Failed to load employee profile"
                )
            }

            attendanceUiState.errorMessage != null -> {
                ErrorCard(
                    message = attendanceUiState.errorMessage ?: "Failed to load attendance data"
                )
            }

            employee != null -> {
                EmployeeSalaryContent(
                    employee = employee,
                    attendanceHistory = attendanceUiState.attendanceHistory
                )
            }
        }
    }
}

@Composable
private fun SalaryHeader() {
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
                    imageVector = Icons.Rounded.Payments,
                    contentDescription = "Salary",
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
                text = "My Salary",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Salary preview calculated from attendance.",
                modifier = Modifier.padding(top = 4.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun EmployeeSalaryContent(
    employee: Employee,
    attendanceHistory: List<Attendance>
) {
    val currentMonth = currentMonth()

    val currentMonthAttendance = attendanceHistory.filter {
        it.date.startsWith(currentMonth)
    }

    val attendedDays = currentMonthAttendance
        .map { it.date }
        .distinct()
        .size

    val lateDays = currentMonthAttendance.count {
        it.status == "Late"
    }

    val workDays = if (employee.workDaysPerMonth > 0) {
        employee.workDaysPerMonth
    } else {
        30
    }

    val absentDays = (workDays - attendedDays).coerceAtLeast(0)

    val dailySalary = if (workDays > 0) {
        employee.baseSalary / workDays
    } else {
        0.0
    }

    val deduction = absentDays * dailySalary
    val finalSalary = (employee.baseSalary - deduction).coerceAtLeast(0.0)

    FinalSalaryCard(
        currentMonth = currentMonth,
        finalSalary = finalSalary
    )

    Spacer(modifier = Modifier.height(18.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SalaryMiniCard(
            title = "Present",
            value = attendedDays.toString(),
            icon = Icons.Rounded.EventAvailable,
            modifier = Modifier.weight(1f)
        )

        SalaryMiniCard(
            title = "Absent",
            value = absentDays.toString(),
            icon = Icons.Rounded.WorkHistory,
            modifier = Modifier.weight(1f)
        )

        SalaryMiniCard(
            title = "Late",
            value = lateDays.toString(),
            icon = Icons.Rounded.Schedule,
            modifier = Modifier.weight(1f)
        )
    }

    Spacer(modifier = Modifier.height(18.dp))

    SalaryDetailsCard(
        currentMonth = currentMonth,
        baseSalary = employee.baseSalary,
        workDays = workDays,
        attendedDays = attendedDays,
        absentDays = absentDays,
        lateDays = lateDays,
        dailySalary = dailySalary,
        deduction = deduction,
        finalSalary = finalSalary
    )

    Spacer(modifier = Modifier.height(18.dp))

    FormulaCard()
}

@Composable
private fun FinalSalaryCard(
    currentMonth: String,
    finalSalary: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(22.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(52.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.16f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.AccountBalanceWallet,
                            contentDescription = "Final Salary",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = "Final Salary Preview",
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.86f),
                        fontSize = 14.sp
                    )

                    Text(
                        text = "$${formatMoney(finalSalary)}",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Calculated for $currentMonth using attendance records.",
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.82f),
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun SalaryMiniCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(112.dp),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                modifier = Modifier.size(34.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column {
                Text(
                    text = value,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = title,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun SalaryDetailsCard(
    currentMonth: String,
    baseSalary: Double,
    workDays: Int,
    attendedDays: Int,
    absentDays: Int,
    lateDays: Int,
    dailySalary: Double,
    deduction: Double,
    finalSalary: Double
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
                            imageVector = Icons.Rounded.AttachMoney,
                            contentDescription = "Salary Details",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Salary Details",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "Monthly calculation breakdown.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            ProfileInfoRow("Month", currentMonth)
            ProfileInfoRow("Base Salary", "$${formatMoney(baseSalary)}")
            ProfileInfoRow("Work Days Per Month", workDays.toString())
            ProfileInfoRow("Attended Days", attendedDays.toString())
            ProfileInfoRow("Absent Days", absentDays.toString())
            ProfileInfoRow("Late Days", lateDays.toString())
            ProfileInfoRow("Daily Salary", "$${formatMoney(dailySalary)}")
            ProfileInfoRow("Deduction", "$${formatMoney(deduction)}")
            ProfileInfoRow("Final Salary", "$${formatMoney(finalSalary)}")
        }
    }
}

@Composable
private fun FormulaCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.MoneyOff,
                        contentDescription = "Formula",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Salary Formula",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Final Salary = Base Salary - (Absent Days × Daily Salary). Late days are shown but not deducted yet.",
                    modifier = Modifier.padding(top = 5.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            }
        }
    }
}

private fun currentMonth(): String {
    return SimpleDateFormat("yyyy-MM", Locale.getDefault())
        .format(Date())
}