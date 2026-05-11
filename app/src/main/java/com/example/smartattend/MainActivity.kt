package com.example.smartattend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.smartattend.ui.navigation.AppNavGraph
import com.example.smartattend.ui.theme.SmartAttendTheme
import com.example.smartattend.viewmodel.AppSettingsViewModel

class MainActivity : ComponentActivity() {

    private val appSettingsViewModel: AppSettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            val isDarkMode by appSettingsViewModel.isDarkMode.collectAsState()

            SmartAttendTheme(
                darkTheme = isDarkMode,
                dynamicColor = false
            ) {
                AppNavGraph(
                    appSettingsViewModel = appSettingsViewModel
                )
            }
        }
    }
}