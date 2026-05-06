package com.example.smartattend.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.smartattend.viewmodel.AdminViewModel

private enum class AdminTab(
    val label: String,
    val icon: String
) {
    DASHBOARD("Dashboard", "🏠"),
    HR("HR", "👥"),
    REPORTS("Reports", "📊"),
    REQUESTS("Requests", "🔔"),
    PROFILE("Profile", "👤")
}

@Composable
fun AdminRootScreen(
    viewModel: AdminViewModel,
    onAddHrClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(AdminTab.DASHBOARD) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            AdminBottomBar(
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
                AdminTab.DASHBOARD -> {
                    AdminDashboardScreen(
                        viewModel = viewModel,
                        onAddHrClick = onAddHrClick
                    )
                }

                AdminTab.HR -> {
                    AdminHrTabScreen(
                        onAddHrClick = onAddHrClick
                    )
                }

                AdminTab.REPORTS -> {
                    AdminPlaceholderScreen(
                        title = "Reports",
                        subtitle = "Attendance and salary reports will appear here.",
                        emoji = "📊"
                    )
                }

                AdminTab.REQUESTS -> {
                    AdminPlaceholderScreen(
                        title = "Requests",
                        subtitle = "HR profile update requests will appear here.",
                        emoji = "🔔"
                    )
                }

                AdminTab.PROFILE -> {
                    AdminProfileScreen(
                        onLogoutConfirmed = onLogoutClick
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminBottomBar(
    selectedTab: AdminTab,
    onTabSelected: (AdminTab) -> Unit
) {
    NavigationBar(
        modifier = Modifier.navigationBarsPadding(),
        tonalElevation = 8.dp
    ) {
        AdminTab.entries.forEach { tab ->
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