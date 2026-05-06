package com.example.smartattend.ui.employee

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.smartattend.viewmodel.AttendanceViewModel
import com.example.smartattend.viewmodel.EmployeeViewModel

private enum class EmployeeTab(
    val label: String,
    val icon: String
) {
    HOME("Home", "🏠"),
    SCAN("Scan", "📷"),
    ATTENDANCE("Attendance", "🕘"),
    SALARY("Salary", "💵"),
    PROFILE("Profile", "👤")
}

@Composable
fun EmployeeRootScreen(
    employeeViewModel: EmployeeViewModel,
    attendanceViewModel: AttendanceViewModel,
    onLogoutClick: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(EmployeeTab.HOME) }

    LaunchedEffect(Unit) {
        employeeViewModel.loadEmployeeProfile()
    }

    Scaffold(
        bottomBar = {
            EmployeeBottomBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                EmployeeTab.HOME -> {
                    EmployeeHomeScreen(viewModel = employeeViewModel)
                }

                EmployeeTab.SCAN -> {
                    EmployeeScanScreen(attendanceViewModel = attendanceViewModel)
                }

                EmployeeTab.ATTENDANCE -> {
                    EmployeeAttendanceScreen(viewModel = employeeViewModel)
                }

                EmployeeTab.SALARY -> {
                    EmployeeSalaryScreen(viewModel = employeeViewModel)
                }

                EmployeeTab.PROFILE -> {
                    EmployeeProfileScreen(
                        viewModel = employeeViewModel,
                        onLogoutConfirmed = onLogoutClick
                    )
                }
            }
        }
    }
}

@Composable
private fun EmployeeBottomBar(
    selectedTab: EmployeeTab,
    onTabSelected: (EmployeeTab) -> Unit
) {
    NavigationBar(
        modifier = Modifier.navigationBarsPadding(),
        tonalElevation = 8.dp
    ) {
        EmployeeTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                icon = {
                    Surface(
                        shape = CircleShape,
                        color = if (selectedTab == tab) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    ) {
                        Text(
                            text = tab.icon,
                            modifier = Modifier.padding(6.dp)
                        )
                    }
                },
                label = {
                    Text(tab.label)
                }
            )
        }
    }
}