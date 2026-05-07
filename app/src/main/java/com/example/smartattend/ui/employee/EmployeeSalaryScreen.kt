package com.example.smartattend.ui.employee

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
            .padding(20.dp)
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = "My Salary",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Salary calculated from this month's attendance.",
            modifier = Modifier.padding(top = 6.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

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

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "Final Salary Preview",
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.86f),
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "$${formatMoney(finalSalary)}",
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Calculated for $currentMonth using attendance records.",
                modifier = Modifier.padding(top = 8.dp),
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.86f)
            )
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
                text = "Salary Details",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            ProfileInfoRow("Month", currentMonth)
            ProfileInfoRow("Base Salary", "$${formatMoney(employee.baseSalary)}")
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

private fun currentMonth(): String {
    return SimpleDateFormat("yyyy-MM", Locale.getDefault())
        .format(Date())
}