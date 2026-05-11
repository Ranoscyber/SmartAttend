package com.example.smartattend.ui.employee

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.EventAvailable
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.smartattend.viewmodel.AttendanceViewModel
import com.example.smartattend.viewmodel.EmployeeViewModel
import com.example.smartattend.viewmodel.AppSettingsViewModel

private enum class EmployeeTab(
    val label: String,
    val icon: ImageVector
) {
    HOME("Home", Icons.Rounded.Home),
    SCAN("Scan", Icons.Rounded.QrCodeScanner),
    ATTENDANCE("Attend", Icons.Rounded.EventAvailable),
    SALARY("Salary", Icons.Rounded.AttachMoney),
    PROFILE("Profile", Icons.Rounded.Person)
}

@Composable
fun EmployeeRootScreen(
    employeeViewModel: EmployeeViewModel,
    attendanceViewModel: AttendanceViewModel,
    appSettingsViewModel: AppSettingsViewModel,
    onLogoutClick: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(EmployeeTab.HOME) }
    var showProfileUpdateRequest by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        employeeViewModel.loadEmployeeProfile()
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                EmployeeTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab && !showProfileUpdateRequest,
                        onClick = {
                            showProfileUpdateRequest = false
                            selectedTab = tab
                        },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label
                            )
                        },
                        label = {
                            Text(tab.label)
                        }
                    )
                }
            }
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(paddingValues)
        ) {
            if (showProfileUpdateRequest) {
                EmployeeProfileUpdateRequestScreen(
                    viewModel = employeeViewModel,
                    onBack = {
                        showProfileUpdateRequest = false
                    }
                )
                return@Box
            }

            when (selectedTab) {
                EmployeeTab.HOME -> {
                    EmployeeHomeScreen(
                        viewModel = employeeViewModel,
                        onScanClick = {
                            selectedTab = EmployeeTab.SCAN
                        }
                    )
                }

                EmployeeTab.SCAN -> {
                    EmployeeScanScreen(
                        attendanceViewModel = attendanceViewModel
                    )
                }

                EmployeeTab.ATTENDANCE -> {
                    EmployeeAttendanceScreen(
                        attendanceViewModel = attendanceViewModel
                    )
                }

                EmployeeTab.SALARY -> {
                    EmployeeSalaryScreen(
                        employeeViewModel = employeeViewModel,
                        attendanceViewModel = attendanceViewModel
                    )
                }

                EmployeeTab.PROFILE -> {
                    EmployeeProfileScreen(
                        viewModel = employeeViewModel,
                        appSettingsViewModel = appSettingsViewModel,
                        onRequestUpdateClick = {
                            showProfileUpdateRequest = true
                        },
                        onLogoutConfirmed = onLogoutClick
                    )
                }
            }
        }
    }
}