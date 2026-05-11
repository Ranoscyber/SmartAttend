package com.example.smartattend.ui.hr

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apartment
import androidx.compose.material.icons.rounded.Assessment
import androidx.compose.material.icons.rounded.Business
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.smartattend.viewmodel.AppSettingsViewModel
import com.example.smartattend.viewmodel.HrViewModel

private enum class HrTab(
    val label: String,
    val icon: ImageVector
) {
    DASHBOARD("Home", Icons.Rounded.Dashboard),
    WORKPLACE("Work", Icons.Rounded.Business),
    DEPARTMENTS("Dept", Icons.Rounded.Apartment),
    EMPLOYEES("Staff", Icons.Rounded.Groups),
    REPORTS("Reports", Icons.Rounded.Assessment),
    PROFILE("Profile", Icons.Rounded.Person)
}

@Composable
fun HrRootScreen(
    viewModel: HrViewModel,
    appSettingsViewModel: AppSettingsViewModel,
    onLogoutClick: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(HrTab.DASHBOARD) }

    LaunchedEffect(Unit) {
        viewModel.loadHrData()
    }

    Scaffold(
        bottomBar = {
            HrBottomBar(
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
                HrTab.DASHBOARD -> {
                    HrDashboardScreen(viewModel = viewModel)
                }

                HrTab.WORKPLACE -> {
                    HrWorkplaceScreen(viewModel = viewModel)
                }

                HrTab.DEPARTMENTS -> {
                    HrDepartmentsScreen(viewModel = viewModel)
                }

                HrTab.EMPLOYEES -> {
                    HrEmployeesScreen(viewModel = viewModel)
                }

                HrTab.REPORTS -> {
                    HrReportsScreen(viewModel = viewModel)
                }

                HrTab.PROFILE -> {
                    HrProfileScreen(
                        appSettingsViewModel = appSettingsViewModel,
                        onLogoutConfirmed = onLogoutClick
                    )
                }
            }
        }
    }
}

@Composable
private fun HrBottomBar(
    selectedTab: HrTab,
    onTabSelected: (HrTab) -> Unit
) {
    NavigationBar(
        modifier = Modifier.navigationBarsPadding(),
        tonalElevation = 8.dp
    ) {
        HrTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = selectedTab == tab,
                onClick = {
                    onTabSelected(tab)
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