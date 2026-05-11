package com.example.smartattend.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartattend.data.model.Attendance
import com.example.smartattend.data.model.FakeLocationAlert
import com.example.smartattend.data.model.SalaryReport
import com.example.smartattend.viewmodel.AdminViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminReportsScreen(
    viewModel: AdminViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        viewModel.loadReportData()
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
        ReportsHeader(
            onRefresh = {
                viewModel.loadReportData()
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        ReportSummaryRow(
            attendanceCount = uiState.attendanceReports.size,
            alertCount = uiState.fakeLocationAlerts.count { it.status == "unread" },
            salaryCount = uiState.salaryReports.size
        )

        Spacer(modifier = Modifier.height(18.dp))

        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            edgePadding = 0.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Text(
                        text = "Attend",
                        maxLines = 1,
                        fontSize = 14.sp
                    )
                },
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.CalendarMonth,
                        contentDescription = "Attendance"
                    )
                }
            )

            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Text(
                        text = "Alerts",
                        maxLines = 1,
                        fontSize = 14.sp
                    )
                },
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.NotificationsActive,
                        contentDescription = "Alerts"
                    )
                }
            )

            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = {
                    Text(
                        text = "Salary",
                        maxLines = 1,
                        fontSize = 14.sp
                    )
                },
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.AttachMoney,
                        contentDescription = "Salary"
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        uiState.successMessage?.let {
            SuccessBox(message = it)
            Spacer(modifier = Modifier.height(12.dp))
        }

        uiState.errorMessage?.let {
            ErrorBox(message = it)
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (uiState.isLoading) {
            LoadingCard()
        } else {
            when (selectedTab) {
                0 -> AttendanceReportContent(
                    reports = uiState.attendanceReports
                )

                1 -> FakeLocationAlertContent(
                    alerts = uiState.fakeLocationAlerts,
                    onMarkRead = { alertId ->
                        viewModel.markAlertAsRead(alertId)
                    }
                )

                2 -> SalaryReportContent(
                    reports = uiState.salaryReports
                )
            }
        }
    }
}

@Composable
private fun ReportsHeader(
    onRefresh: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Reports",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Review attendance, alerts, and payroll summaries.",
                modifier = Modifier.padding(top = 6.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
        }

        Surface(
            modifier = Modifier.size(44.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            IconButton(
                onClick = onRefresh
            ) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = "Refresh",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun ReportSummaryRow(
    attendanceCount: Int,
    alertCount: Int,
    salaryCount: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MiniSummaryCard(
            title = "Records",
            value = attendanceCount.toString(),
            icon = Icons.Rounded.CalendarMonth,
            modifier = Modifier.weight(1f)
        )

        MiniSummaryCard(
            title = "Alerts",
            value = alertCount.toString(),
            icon = Icons.Rounded.Warning,
            modifier = Modifier.weight(1f)
        )

        MiniSummaryCard(
            title = "Salary",
            value = salaryCount.toString(),
            icon = Icons.Rounded.AttachMoney,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MiniSummaryCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(108.dp),
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
private fun AttendanceReportContent(
    reports: List<Attendance>
) {
    if (reports.isEmpty()) {
        EmptyReportCard(
            icon = Icons.Rounded.CalendarMonth,
            title = "No attendance records",
            subtitle = "Employee check-in records will appear here."
        )
        return
    }

    reports.forEach { attendance ->
        AttendanceReportCard(attendance = attendance)
    }
}

@Composable
private fun AttendanceReportCard(
    attendance: Attendance
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(46.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = "Employee",
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
                        text = attendance.employeeName.ifBlank { "Unknown Employee" },
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )

                    Text(
                        text = "${attendance.employeeId} • ${attendance.date}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        maxLines = 1
                    )
                }

                StatusChip(
                    text = attendance.status.ifBlank { "-" }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            DetailRow(
                icon = Icons.Rounded.LocationOn,
                label = "Workplace",
                value = attendance.workplaceName.ifBlank { "-" }
            )

            DetailRow(
                icon = Icons.Rounded.AccessTime,
                label = "Check-in",
                value = attendance.checkInTime.ifBlank { "-" }
            )

            DetailRow(
                icon = Icons.Rounded.AccessTime,
                label = "Check-out",
                value = attendance.checkOutTime.ifBlank { "-" }
            )

            DetailTextRow(
                label = "Check-in Distance",
                value = "${attendance.distanceMeter.toInt()} m"
            )

            if (attendance.checkOutTime.isNotBlank()) {
                DetailTextRow(
                    label = "Check-out Distance",
                    value = "${attendance.checkOutDistanceMeter.toInt()} m"
                )
            }
        }
    }
}

@Composable
private fun FakeLocationAlertContent(
    alerts: List<FakeLocationAlert>,
    onMarkRead: (String) -> Unit
) {
    if (alerts.isEmpty()) {
        EmptyReportCard(
            icon = Icons.Rounded.CheckCircle,
            title = "No fake GPS alerts",
            subtitle = "Fake location attempts will appear here."
        )
        return
    }

    alerts.forEach { alert ->
        FakeLocationAlertCard(
            alert = alert,
            onMarkRead = onMarkRead
        )
    }
}

@Composable
private fun FakeLocationAlertCard(
    alert: FakeLocationAlert,
    onMarkRead: (String) -> Unit
) {
    val isUnread = alert.status == "unread"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnread) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(46.dp),
                    shape = CircleShape,
                    color = if (isUnread) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primaryContainer
                    }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Warning,
                            contentDescription = "Alert",
                            tint = if (isUnread) {
                                MaterialTheme.colorScheme.onError
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = alert.employeeName.ifBlank { "Unknown Employee" },
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isUnread) {
                            MaterialTheme.colorScheme.onErrorContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        maxLines = 1
                    )

                    Text(
                        text = "${alert.employeeId} • ${formatTimestamp(alert.createdAt)}",
                        color = if (isUnread) {
                            MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.75f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        fontSize = 13.sp,
                        maxLines = 1
                    )
                }

                StatusChip(text = alert.status.ifBlank { "unread" })
            }

            Spacer(modifier = Modifier.height(14.dp))

            DetailTextRow(
                label = "Workplace",
                value = alert.workplaceName.ifBlank { "-" }
            )

            LongInfoBox(
                label = "Reason",
                value = alert.reason.ifBlank { "-" }
            )

            DetailTextRow(
                label = "Latitude",
                value = alert.latitude.toString()
            )

            DetailTextRow(
                label = "Longitude",
                value = alert.longitude.toString()
            )

            if (isUnread) {
                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        onMarkRead(alert.alertId)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Mark as Read",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun SalaryReportContent(
    reports: List<SalaryReport>
) {
    if (reports.isEmpty()) {
        EmptyReportCard(
            icon = Icons.Rounded.AttachMoney,
            title = "No salary reports",
            subtitle = "Salary reports will appear after employees are created."
        )
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

    Spacer(modifier = Modifier.height(14.dp))

    reports.forEach { report ->
        SalaryReportCard(report = report)
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
        shape = RoundedCornerShape(26.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(46.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.AttachMoney,
                            contentDescription = "Salary",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Payroll Summary",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Calculated from attendance records.",
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            SalarySummaryRow("Employees", totalEmployees.toString())
            SalarySummaryRow("Base Salary", "$${formatMoney(totalBaseSalary)}")
            SalarySummaryRow("Deduction", "$${formatMoney(totalDeduction)}")
            SalarySummaryRow("Final Salary", "$${formatMoney(totalFinalSalary)}")
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
            .padding(vertical = 5.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.78f),
            fontSize = 14.sp
        )

        Text(
            text = value,
            color = MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun SalaryReportCard(
    report: SalaryReport
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(46.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = "Employee",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = report.employeeName.ifBlank { "Unknown Employee" },
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )

                    Text(
                        text = "${report.employeeId} • ${report.month}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        maxLines = 1
                    )
                }

                AssistChip(
                    onClick = {},
                    label = {
                        Text("$${formatMoney(report.finalSalary)}")
                    }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            DetailTextRow("Department", report.departmentName.ifBlank { "-" })
            DetailTextRow("Position", report.position.ifBlank { "-" })
            DetailTextRow("Base Salary", "$${formatMoney(report.baseSalary)}")
            DetailTextRow("Attended Days", report.attendedDays.toString())
            DetailTextRow("Absent Days", report.absentDays.toString())
            DetailTextRow("Late Days", report.lateDays.toString())
            DetailTextRow("Deduction", "$${formatMoney(report.deduction)}")
        }
    }
}

@Composable
private fun StatusChip(
    text: String
) {
    AssistChip(
        onClick = {},
        label = {
            Text(
                text = text,
                fontSize = 12.sp
            )
        }
    )
}

@Composable
private fun DetailRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

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
private fun LongInfoBox(
    label: String,
    value: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Text(
                text = value,
                modifier = Modifier.padding(12.dp),
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun EmptyReportCard(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Surface(
                modifier = Modifier.size(52.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = subtitle,
                modifier = Modifier.padding(top = 6.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LoadingCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(30.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun SuccessBox(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun ErrorBox(message: String) {
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

private fun formatTimestamp(timestamp: Long): String {
    return try {
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            .format(Date(timestamp))
    } catch (e: Exception) {
        "-"
    }
}

private fun formatMoney(value: Double): String {
    return String.format("%.2f", value)
}