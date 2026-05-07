package com.example.smartattend.ui.hr

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartattend.data.model.SalaryReport

@Composable
fun HrSalaryReportContent(
    reports: List<SalaryReport>
) {
    if (reports.isEmpty()) {
        SalaryEmptyCard()
        return
    }

    val totalBaseSalary = reports.sumOf { it.baseSalary }
    val totalDeduction = reports.sumOf { it.deduction }
    val totalFinalSalary = reports.sumOf { it.finalSalary }

    SalarySummaryCard(
        totalEmployees = reports.size,
        totalBaseSalary = totalBaseSalary,
        totalDeduction = totalDeduction,
        totalFinalSalary = totalFinalSalary
    )

    Spacer(modifier = Modifier.height(16.dp))

    reports.forEach { report ->
        SalaryReportItem(report = report)
    }
}

@Composable
private fun SalarySummaryCard(
    totalEmployees: Int,
    totalBaseSalary: Double,
    totalDeduction: Double,
    totalFinalSalary: Double
) {
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
                text = "Monthly Salary Summary",
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Calculated from attendance records.",
                modifier = Modifier.padding(top = 6.dp),
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
            )

            Spacer(modifier = Modifier.height(18.dp))

            SalarySummaryRow("Employees", totalEmployees.toString())
            SalarySummaryRow("Total Base Salary", "$${formatMoney(totalBaseSalary)}")
            SalarySummaryRow("Total Deduction", "$${formatMoney(totalDeduction)}")
            SalarySummaryRow("Total Final Salary", "$${formatMoney(totalFinalSalary)}")
        }
    }
}

@Composable
private fun SalarySummaryRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
        )

        Text(
            text = value,
            color = MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SalaryReportItem(
    report: SalaryReport
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
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = report.employeeName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "${report.employeeId} • ${report.month}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }

                AssistChip(
                    onClick = {},
                    label = {
                        Text("$${formatMoney(report.finalSalary)}")
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            SalaryInfoRow("Department", report.departmentName.ifBlank { "-" })
            SalaryInfoRow("Position", report.position.ifBlank { "-" })
            SalaryInfoRow("Base Salary", "$${formatMoney(report.baseSalary)}")
            SalaryInfoRow("Work Days", report.workDaysPerMonth.toString())
            SalaryInfoRow("Attended Days", report.attendedDays.toString())
            SalaryInfoRow("Absent Days", report.absentDays.toString())
            SalaryInfoRow("Late Days", report.lateDays.toString())
            SalaryInfoRow("Daily Salary", "$${formatMoney(report.dailySalary)}")
            SalaryInfoRow("Deduction", "$${formatMoney(report.deduction)}")
            SalaryInfoRow("Final Salary", "$${formatMoney(report.finalSalary)}")
        }
    }
}

@Composable
private fun SalaryInfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
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
private fun SalaryEmptyCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "💵",
                fontSize = 44.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "No salary reports",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Create employees first, then attendance records will be used to calculate salary.",
                modifier = Modifier.padding(top = 6.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatMoney(value: Double): String {
    return String.format("%.2f", value)
}