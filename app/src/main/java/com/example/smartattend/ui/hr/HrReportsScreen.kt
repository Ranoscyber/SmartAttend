package com.example.smartattend.ui.hr

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
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.RequestPage
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
            .padding(horizontal = 20.dp, vertical = 18.dp)
            .padding(bottom = 32.dp)
    ) {
        ReportsHeader(
            onRefresh = {
                viewModel.loadReportData()
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        SummaryCards(
            attendanceCount = uiState.attendanceReports.size,
            unreadAlerts = uiState.fakeLocationAlerts.count { it.status == "unread" },
            pendingRequests = uiState.profileUpdateRequests.count { it.status == "pending" }
        )

        Spacer(modifier = Modifier.height(18.dp))

        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            edgePadding = 0.dp
        ) {
            ReportTab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                icon = Icons.Rounded.CalendarMonth,
                text = "Attend"
            )

            ReportTab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                icon = Icons.Rounded.NotificationsActive,
                text = "Alerts"
            )

            ReportTab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                icon = Icons.Rounded.AttachMoney,
                text = "Salary"
            )

            ReportTab(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                icon = Icons.Rounded.RequestPage,
                text = "Requests"
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
private fun ReportsHeader(
    onRefresh: () -> Unit
) {
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
                    imageVector = Icons.Rounded.CalendarMonth,
                    contentDescription = "Reports",
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
                text = "Reports",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Attendance, fake GPS alerts, salary, and requests.",
                modifier = Modifier.padding(top = 4.dp),
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
private fun SummaryCards(
    attendanceCount: Int,
    unreadAlerts: Int,
    pendingRequests: Int
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
            value = unreadAlerts.toString(),
            icon = Icons.Rounded.Warning,
            modifier = Modifier.weight(1f)
        )

        MiniSummaryCard(
            title = "Requests",
            value = pendingRequests.toString(),
            icon = Icons.Rounded.RequestPage,
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
private fun ReportTab(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    text: String
) {
    Tab(
        selected = selected,
        onClick = onClick,
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = text
            )
        },
        text = {
            Text(
                text = text,
                maxLines = 1,
                fontSize = 14.sp
            )
        }
    )
}

@Composable
private fun AttendanceReportList(
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
                    modifier = Modifier.size(48.dp),
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

                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            text = attendance.status.ifBlank { "-" },
                            fontSize = 12.sp
                        )
                    }
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
private fun FakeLocationAlertList(
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
                    modifier = Modifier.size(48.dp),
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

                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            text = alert.status.ifBlank { "unread" },
                            fontSize = 12.sp
                        )
                    }
                )
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
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
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
                modifier = Modifier.size(54.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = subtitle,
                modifier = Modifier.padding(top = 6.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
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
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(14.dp),
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun ErrorBox(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.errorContainer
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(14.dp),
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