package com.example.smartattend.ui.hr

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.smartattend.viewmodel.HrViewModel

private enum class HrTab(
    val label: String,
    val icon: String
) {
    DASHBOARD("Home", "🏠"),
    WORKPLACE("Work", "📍"),
    DEPARTMENTS("Dept", "🏢"),
    EMPLOYEES("Staff", "👥"),
    REPORTS("Reports", "📊"),
    PROFILE("Profile", "👤")
}

@Composable
fun HrRootScreen(
    viewModel: HrViewModel,
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