package com.example.smartattend.ui.employee

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartattend.viewmodel.EmployeeViewModel

@Composable
fun EmployeeAttendanceScreen(
    viewModel: EmployeeViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val employee = uiState.employee

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .statusBarsPadding()
            .padding(20.dp)
    ) {
        Text(
            text = "My Attendance",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Your check-in and check-out history will appear here.",
            modifier = Modifier.padding(top = 6.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🕘",
                    fontSize = 48.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Attendance Coming Soon",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = if (employee != null) {
                        "Next module will add QR check-in and check-out for ${employee.fullName}."
                    } else {
                        "Next module will add QR check-in and check-out."
                    },
                    modifier = Modifier.padding(top = 8.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}