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
import com.example.smartattend.data.model.Employee
import com.example.smartattend.viewmodel.EmployeeViewModel

@Composable
fun EmployeeSalaryScreen(
    viewModel: EmployeeViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val employee = uiState.employee

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
            text = "Salary preview based on your profile.",
            modifier = Modifier.padding(top = 6.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        when {
            uiState.isLoading -> LoadingBox()

            uiState.errorMessage != null -> ErrorCard(
                message = uiState.errorMessage ?: "Unknown error"
            )

            employee != null -> EmployeeSalaryContent(employee = employee)
        }
    }
}

@Composable
private fun EmployeeSalaryContent(
    employee: Employee
) {
    val dailySalary = if (employee.workDaysPerMonth > 0) {
        employee.baseSalary / employee.workDaysPerMonth
    } else {
        0.0
    }

    val absentDays = 0
    val deduction = absentDays * dailySalary
    val finalSalary = employee.baseSalary - deduction

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
                text = "Attendance deduction will be calculated after check-in module.",
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

            ProfileInfoRow("Base Salary", "$${formatMoney(employee.baseSalary)}")
            ProfileInfoRow("Work Days Per Month", employee.workDaysPerMonth.toString())
            ProfileInfoRow("Daily Salary", "$${formatMoney(dailySalary)}")
            ProfileInfoRow("Absent Days", absentDays.toString())
            ProfileInfoRow("Deduction", "$${formatMoney(deduction)}")
            ProfileInfoRow("Final Salary", "$${formatMoney(finalSalary)}")
        }
    }

    Spacer(modifier = Modifier.height(18.dp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Text(
            text = "Formula: Final Salary = Base Salary - (Absent Days × Daily Salary)",
            modifier = Modifier.padding(18.dp),
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}