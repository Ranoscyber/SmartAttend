package com.example.smartattend.ui.hr

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
import com.example.smartattend.data.model.FakeLocationAlert
import com.example.smartattend.viewmodel.HrViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HrReportsScreen(
    viewModel: HrViewModel
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
            .padding(20.dp)
            .padding(bottom = 32.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Reports",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Attendance records and fake GPS alerts.",
                    modifier = Modifier.padding(top = 6.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            TextButton(
                onClick = {
                    viewModel.loadReportData()
                }
            ) {
                Text("Refresh")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            edgePadding = 12.dp
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Text(
                        text = "Attendance",
                        maxLines = 1
                    )
                }
            )

            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Text(
                        text = "Alerts (${uiState.fakeLocationAlerts.count { it.status == "unread" }})",
                        maxLines = 1
                    )
                }
            )

            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = {
                    Text(
                        text = "Salary",
                        maxLines = 1
                    )
                }
            )

            Tab(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                text = {
                    Text(
                        text = "Requests (${uiState.profileUpdateRequests.count { it.status == "pending" }})",
                        maxLines = 1
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
                0 -> AttendanceReportList(
                    reports = uiState.attendanceReports
                )

                1 -> FakeLocationAlertList(
                    alerts = uiState.fakeLocationAlerts,
                    onMarkRead = { alertId ->
                        viewModel.markAlertAsRead(alertId)
                    }
                )

                2 -> HrSalaryReportContent(
                    reports = uiState.salaryReports
                )

                3 -> HrProfileRequestsContent(
                    requests = uiState.profileUpdateRequests,
                    isLoading = uiState.isLoading,
                    onApprove = { request ->
                        viewModel.approveEmployeeProfileRequest(request)
                    },
                    onReject = { requestId, reason ->
                        viewModel.rejectEmployeeProfileRequest(
                            requestId = requestId,
                            rejectReason = reason
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun AttendanceReportList(
    reports: List<Attendance>
) {
    if (reports.isEmpty()) {
        EmptyReportCard(
            emoji = "🕘",
            title = "No attendance records",
            subtitle = "Employee check-in records will appear here."
        )
        return
    }

    reports.forEach { attendance ->
        AttendanceReportItem(attendance = attendance)
    }
}

@Composable
private fun AttendanceReportItem(
    attendance: Attendance
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
                        text = attendance.employeeName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "${attendance.employeeId} • ${attendance.date}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }

                AssistChip(
                    onClick = {},
                    label = {
                        Text(attendance.status.ifBlank { "-" })
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            ReportInfoRow("Workplace", attendance.workplaceName.ifBlank { "-" })
            ReportInfoRow("Check-in", attendance.checkInTime.ifBlank { "-" })
            ReportInfoRow("Check-out", attendance.checkOutTime.ifBlank { "-" })
            ReportInfoRow("Check-in Distance", "${attendance.distanceMeter.toInt()} m")

            if (attendance.checkOutTime.isNotBlank()) {
                ReportInfoRow("Check-out Distance", "${attendance.checkOutDistanceMeter.toInt()} m")
            }
        }
    }
}

@Composable
private fun FakeLocationAlertList(
    alerts: List<FakeLocationAlert>,
    onMarkRead: (String) -> Unit
) {
    if (alerts.isEmpty()) {
        EmptyReportCard(
            emoji = "✅",
            title = "No fake GPS alerts",
            subtitle = "Fake location attempts will appear here."
        )
        return
    }

    alerts.forEach { alert ->
        FakeLocationAlertItem(
            alert = alert,
            onMarkRead = onMarkRead
        )
    }
}

@Composable
private fun FakeLocationAlertItem(
    alert: FakeLocationAlert,
    onMarkRead: (String) -> Unit
) {
    val isUnread = alert.status == "unread"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnread) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
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
                        text = alert.employeeName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isUnread) {
                            MaterialTheme.colorScheme.onErrorContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )

                    Text(
                        text = "${alert.employeeId} • ${formatTimestamp(alert.createdAt)}",
                        color = if (isUnread) {
                            MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        fontSize = 14.sp
                    )
                }

                AssistChip(
                    onClick = {},
                    label = {
                        Text(alert.status)
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            AlertInfoRow("Workplace", alert.workplaceName)
            AlertLongInfoBox("Reason", alert.reason)
            AlertInfoRow("Latitude", alert.latitude.toString())
            AlertInfoRow("Longitude", alert.longitude.toString())

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
                    Text("Mark as Read")
                }
            }
        }
    }
}

@Composable
private fun ReportInfoRow(
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
private fun AlertInfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = value,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            modifier = Modifier.weight(1.4f),
            maxLines = 2
        )
    }
}

@Composable
private fun AlertLongInfoBox(
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
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.65f)
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
    emoji: String,
    title: String,
    subtitle: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = emoji,
                fontSize = 44.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

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
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp)
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